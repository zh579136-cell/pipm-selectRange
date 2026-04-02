package com.example.datascope.service;

import com.example.datascope.domain.DataScopeMode;
import com.example.datascope.domain.OrgLevel;
import com.example.datascope.domain.RuleSource;
import com.example.datascope.domain.ScopeType;
import com.example.datascope.exception.ConfigConflictException;
import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.DefaultRule;
import com.example.datascope.model.DemoUser;
import com.example.datascope.model.OrgNode;
import com.example.datascope.model.PageConfig;
import com.example.datascope.model.RuleMatch;
import com.example.datascope.model.ScopeRoot;
import com.example.datascope.model.SqlCondition;
import com.example.datascope.model.UserOverrideRule;
import com.example.datascope.repository.DefaultRuleRepository;
import com.example.datascope.repository.OrgRepository;
import com.example.datascope.repository.PageConfigRepository;
import com.example.datascope.repository.UserOverrideRuleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
/**
 * 数据范围计算核心服务。
 *
 * 这个类负责回答一个最关键的问题：
 * “某个用户进入某个 pageCode 页面时，最终到底能看到哪些机构的数据？”
 *
 * 它本身不直接查业务数据，而是先把“可见机构范围”计算出来，供后续 SQL 过滤使用。
 * 可以把它理解成数据权限领域里的“规则引擎 + 范围解释器”。
 */
public class DataScopeResolveService {

    private final PageConfigRepository pageConfigRepository;
    private final DefaultRuleRepository defaultRuleRepository;
    private final UserOverrideRuleRepository userOverrideRuleRepository;
    private final OrgRepository orgRepository;
    private final DataScopeSqlBuilder dataScopeSqlBuilder;

    public DataScopeResolveService(PageConfigRepository pageConfigRepository,
                                   DefaultRuleRepository defaultRuleRepository,
                                   UserOverrideRuleRepository userOverrideRuleRepository,
                                   OrgRepository orgRepository,
                                   DataScopeSqlBuilder dataScopeSqlBuilder) {
        this.pageConfigRepository = pageConfigRepository;
        this.defaultRuleRepository = defaultRuleRepository;
        this.userOverrideRuleRepository = userOverrideRuleRepository;
        this.orgRepository = orgRepository;
        this.dataScopeSqlBuilder = dataScopeSqlBuilder;
    }

    /**
     * 数据范围计算主入口。
     *
     * 计算顺序固定为：
     * 1. 先看页面是否启用了数据范围控制，没启用就直接返回全量。
     * 2. 根据用户机构路径同时推导出：
     *    - 当前机构
     *    - 所属行（总行 / 一级分行 / 二级分行 / 一级支行 / 二级支行）
     *    - 所属一级部门（所属行后的下一个路径节点，缺失时回退到所属行）
     * 3. 遍历用户当前所有角色，逐个角色挑出一条最具体的规则。
     * 4. 多角色结果做并集，得到最终可见机构范围。
     */
    public DataScopeResult resolve(String pageCode, DemoUser user) {
        DataScopeResult result = new DataScopeResult();
        result.setPageCode(pageCode);

        // 第一步：先看这个页面是否启用了数据范围控制。
        // 如果没启用，就不再往下做角色/规则计算，直接视为“全量可见”。
        PageConfig pageConfig = pageConfigRepository.findByPageCode(pageCode);
        if (pageConfig == null || !pageConfig.isEnabled()) {
            result.setPageEnabled(false);
            result.setScopeMode(DataScopeMode.ALL);
            result.setMessage("当前页面未启用数据范围控制，返回全量数据。");
            SqlCondition condition = dataScopeSqlBuilder.build(result);
            result.setSqlPreview(condition.getPreview());
            result.setVisibleOrgIdsPreview(extractAllOrgIds(orgRepository.findAll()));
            return result;
        }

        result.setPageEnabled(true);

        // 第二步：把整棵机构树读出来，构造成 map，后面所有“当前机构 / 所属行 / 一级部门”
        // 都依赖这份机构字典来解析。
        Map<String, OrgNode> orgMap = buildOrgMap(orgRepository.findAll());
        UserScopeContext userScopeContext = resolveUserScopeContext(user, orgMap);

        // 多角色最终是“并集”关系：
        // - exactOrgIds 表示精确命中的机构点，只看某个 org_id
        // - subtreeOrgIds 表示以某个机构为根节点看整棵子树
        //
        // 这里使用 LinkedHashSet，是为了：
        // 1. 自动去重
        // 2. 保持相对稳定的展示顺序，便于前端调试和人工核对
        Set<String> exactOrgIds = new LinkedHashSet<String>();
        Set<String> subtreeOrgIds = new LinkedHashSet<String>();

        // 第三步：逐个角色计算命中的规则。
        // 这里不会在角色之间做“谁覆盖谁”的竞争，而是每个角色算完后，结果做并集。
        for (String roleCode : user.getRoleCodes()) {
            RoleResolution roleResolution = resolveRole(pageCode, user, roleCode, userScopeContext, orgMap);
            if (roleResolution == null) {
                // 某个角色如果完全没有配规则，直接跳过，不影响其它角色。
                continue;
            }

            result.getMatchedRoleRules().add(roleResolution.ruleMatch);
            for (OrgNode targetOrg : roleResolution.targetOrgs) {
                if ("EXACT".equals(roleResolution.matchMode)) {
                    exactOrgIds.add(targetOrg.getOrgId());
                } else {
                    subtreeOrgIds.add(targetOrg.getOrgId());
                }
                result.getResolvedScopeRoots().add(createScopeRoot(targetOrg, roleResolution.matchMode));
            }
        }

        // 第四步：汇总整个用户的最终范围。
        // 如果一个角色都没命中，就返回“空权限”；否则把 exact/subtree 两类范围都放进结果对象。
        if (result.getMatchedRoleRules().isEmpty()) {
            result.setScopeMode(DataScopeMode.EMPTY);
            result.setAccessDenied(true);
            result.setMessage("当前页面未配置可用数据范围规则。");
        } else {
            result.setExactOrgIds(new ArrayList<String>(exactOrgIds));
            result.setSubtreeOrgIds(new ArrayList<String>(subtreeOrgIds));
            result.setScopeMode(determineScopeMode(exactOrgIds, subtreeOrgIds));
            result.setMessage("已根据角色和数据范围规则计算可见机构。");
            result.setVisibleOrgIdsPreview(resolveVisibleOrgIds(orgMap.values(), exactOrgIds, subtreeOrgIds));
        }

        // 最后一步：把结果翻译成前端可直接展示的 SQL 预览，方便调试。
        SqlCondition condition = dataScopeSqlBuilder.build(result);
        result.setSqlPreview(condition.getPreview());
        return result;
    }

    /**
     * 在“同一页面 + 同一角色”范围内，挑出最终生效的一条规则。
     *
     * 固定优先级：
     * 1. 个人覆盖
     * 2. 带职位关键词的默认规则
     * 3. 普通默认规则
     *
     * 这里返回的是“角色级别的解析结果”，还没和其它角色做并集。
     */
    private RoleResolution resolveRole(String pageCode,
                                       DemoUser user,
                                       String roleCode,
                                       UserScopeContext userScopeContext,
                                       Map<String, OrgNode> orgMap) {
        // 同一角色内部优先命中个人覆盖；只有没有个人覆盖时才回退到默认规则。
        List<UserOverrideRule> overrides = userOverrideRuleRepository.findEnabledRules(user.getUserId(), pageCode, roleCode);
        if (overrides.size() > 1) {
            throw new ConfigConflictException("用户个人覆盖规则冲突，用户=" + user.getUserId() + "，角色=" + roleCode);
        }
        if (!overrides.isEmpty()) {
            UserOverrideRule override = overrides.get(0);
            if (override.getCustomOrgIds() != null && !override.getCustomOrgIds().isEmpty()) {
                // 当前版本的个人覆盖走“单根节点机构树”模式：
                // 用户只需要选一个机构根节点，然后默认看这个节点整棵子树。
                return toCustomTreeResolution(roleCode, override, user.getUserOrgLevel().name(), orgMap);
            }
            // 兼容历史/演示用法：如果没有自定义机构树，就仍然允许用 scopeType 快捷范围。
            return toRoleResolution(roleCode, override.getId(), RuleSource.USER_OVERRIDE, override.getScopeType(), "个人覆盖", user.getUserOrgLevel().name(), userScopeContext);
        }

        // 没有个人覆盖，才进入默认规则。
        // 这里按“同页面 + 同角色 + 当前用户机构层级”先把规则拿出来，
        // 再在内存里继续区分“带职位关键词”和“不带职位关键词”。
        List<DefaultRule> defaults = defaultRuleRepository.findEnabledRules(pageCode, roleCode, user.getUserOrgLevel());
        List<DefaultRule> withJobKeyword = new ArrayList<DefaultRule>();
        List<DefaultRule> plain = new ArrayList<DefaultRule>();
        for (DefaultRule rule : defaults) {
            // 带职位关键词的规则属于“更具体”的规则，只有职位名称包含关键词时才会参与竞争。
            if (rule.getJobKeyword() != null && !rule.getJobKeyword().trim().isEmpty()) {
                if (matchesJobKeyword(user.getPositionName(), rule.getJobKeyword())) {
                    withJobKeyword.add(rule);
                }
            } else {
                plain.add(rule);
            }
        }

        // 同一层级的规则如果出现多条有效记录，系统会直接报配置冲突。
        // 这样可以避免“到底该命中哪条”这种模糊状态偷偷溜进生产逻辑。
        if (withJobKeyword.size() > 1) {
            throw new ConfigConflictException("默认规则冲突，页面=" + pageCode + "，角色=" + roleCode + " 存在多条职位关键字规则。");
        }
        if (plain.size() > 1) {
            throw new ConfigConflictException("默认规则冲突，页面=" + pageCode + "，角色=" + roleCode + " 存在多条基础规则。");
        }
        if (!withJobKeyword.isEmpty()) {
            // 只要职位关键词命中，就不会再回退到普通默认规则。
            DefaultRule rule = withJobKeyword.get(0);
            return toRoleResolution(roleCode, rule.getId(), RuleSource.DEFAULT_WITH_JOB, rule.getScopeType(), "职位匹配：" + rule.getJobKeyword(), rule.getUserOrgLevel().name(), userScopeContext);
        }
        if (!plain.isEmpty()) {
            DefaultRule rule = plain.get(0);
            return toRoleResolution(roleCode, rule.getId(), RuleSource.DEFAULT, rule.getScopeType(), "默认规则", rule.getUserOrgLevel().name(), userScopeContext);
        }
        return null;
    }

    /**
     * 把“scopeType 快捷范围规则”翻译成真正的命中结果。
     *
     * 这里会把：
     * - 规则来源
     * - 命中的说明
     * - 命中的机构根节点
     * - 命中方式（精确点 / 子树）
     * 一次性封装成前端可展示的 RuleMatch。
     */
    private RoleResolution toRoleResolution(String roleCode,
                                            Long ruleId,
                                            RuleSource source,
                                            ScopeType scopeType,
                                            String matchedBy,
                                            String matchedUserOrgLevel,
                                            UserScopeContext userScopeContext) {
        RuleMatch match = new RuleMatch();
        match.setRoleCode(roleCode);
        match.setRuleId(ruleId);
        match.setRuleSource(source);
        match.setScopeType(scopeType);
        match.setMatchedBy(matchedBy);
        match.setMatchedUserOrgLevel(matchedUserOrgLevel);

        ResolvedTarget target = resolveTarget(scopeType, userScopeContext);
        if (target.org != null) {
            match.setResolvedScopeLabel(target.scopeLabel);
            match.setResolvedScopeNote(target.scopeNote);
            match.setResolvedOrgId(target.org.getOrgId());
            match.setResolvedOrgName(target.org.getOrgName());
            match.setResolvedOrgIds(Collections.singletonList(target.org.getOrgId()));
        }

        RoleResolution resolution = new RoleResolution();
        resolution.ruleMatch = match;
        resolution.targetOrgs.add(target.org);
        resolution.matchMode = target.matchMode;
        resolution.scopeType = scopeType;
        return resolution;
    }

    /**
     * 把“个人覆盖根节点”翻译成角色解析结果。
     *
     * 个人覆盖不再依赖 scopeType，而是直接拿用户勾选的机构根节点作为子树根。
     */
    private RoleResolution toCustomTreeResolution(String roleCode, UserOverrideRule override, String matchedUserOrgLevel, Map<String, OrgNode> orgMap) {
        if (override.getCustomOrgIds() == null || override.getCustomOrgIds().size() != 1) {
            throw new ConfigConflictException("个人覆盖只能配置一个机构根节点，规则ID=" + override.getId());
        }

        RuleMatch match = new RuleMatch();
        match.setRoleCode(roleCode);
        match.setRuleId(override.getId());
        match.setRuleSource(RuleSource.USER_OVERRIDE);
        match.setMatchedBy("个人覆盖（单根节点）");
        match.setMatchedUserOrgLevel(matchedUserOrgLevel);
        match.setResolvedScopeLabel("个人覆盖根节点");
        match.setResolvedOrgIds(new ArrayList<String>(override.getCustomOrgIds()));

        String orgId = override.getCustomOrgIds().get(0);
        OrgNode targetOrg = orgMap.get(orgId);

        if (targetOrg == null) {
            throw new ConfigConflictException("个人覆盖配置了不存在的机构根节点，规则ID=" + override.getId());
        }

        match.setResolvedOrgId(targetOrg.getOrgId());
        match.setResolvedOrgName(targetOrg.getOrgName());

        RoleResolution resolution = new RoleResolution();
        resolution.ruleMatch = match;
        resolution.targetOrgs.add(targetOrg);
        resolution.matchMode = "SUBTREE";
        resolution.scopeType = null;
        return resolution;
    }

    /**
     * 职位关键词匹配策略。
     *
     * 当前实现非常直接：职位名称 contains 关键词，大小写不敏感。
     * 这里不做分词、模糊同义词、角色名匹配等扩展逻辑。
     */
    private boolean matchesJobKeyword(String positionName, String keyword) {
        if (positionName == null || keyword == null) {
            return false;
        }
        return positionName.toLowerCase(Locale.ROOT).contains(keyword.trim().toLowerCase(Locale.ROOT));
    }

    private ScopeRoot createScopeRoot(OrgNode orgNode, String matchMode) {
        ScopeRoot root = new ScopeRoot();
        root.setOrgId(orgNode.getOrgId());
        root.setOrgName(orgNode.getOrgName());
        root.setOrgPathId(orgNode.getOrgPathId());
        root.setMatchMode(matchMode);
        return root;
    }

    private DataScopeMode determineScopeMode(Set<String> exactOrgIds, Set<String> subtreeOrgIds) {
        if (!exactOrgIds.isEmpty() && !subtreeOrgIds.isEmpty()) {
            return DataScopeMode.MIXED;
        }
        if (!subtreeOrgIds.isEmpty()) {
            return DataScopeMode.SUBTREE;
        }
        if (!exactOrgIds.isEmpty()) {
            return DataScopeMode.EXACT;
        }
        return DataScopeMode.EMPTY;
    }

    private List<String> resolveVisibleOrgIds(Collection<OrgNode> orgNodes, Set<String> exactOrgIds, Set<String> subtreeOrgIds) {
        List<String> visible = new ArrayList<String>();
        for (OrgNode orgNode : orgNodes) {
            if (exactOrgIds.contains(orgNode.getOrgId())) {
                visible.add(orgNode.getOrgId());
                continue;
            }
            for (String rootId : subtreeOrgIds) {
                if (containsPathSegment(orgNode.getOrgPathId(), rootId)) {
                    visible.add(orgNode.getOrgId());
                    break;
                }
            }
        }
        return visible;
    }

    private boolean containsPathSegment(String orgPathId, String orgId) {
        // 用带分隔符的匹配避免 100201 错误命中 1002019。
        return ("/" + orgPathId + "/").contains("/" + orgId + "/");
    }

    /**
     * 根据当前登录用户，推导三个后续计算会反复用到的锚点：
     * - currentOrg：当前机构
     * - ownerBankOrg：所属行
     * - firstDeptOrg：所属一级部门
     */
    private UserScopeContext resolveUserScopeContext(DemoUser user, Map<String, OrgNode> orgMap) {
        UserScopeContext context = new UserScopeContext();
        context.currentOrg = resolveCurrentOrg(user, orgMap);
        context.ownerBankOrg = resolveOwnerBankOrg(user, orgMap);
        context.firstDeptOrg = resolveFirstDeptOrg(user, context.ownerBankOrg, orgMap, context);
        return context;
    }

    /**
     * 当前机构的定义最简单：优先取用户表里的 orgId。
     * 如果演示数据异常导致查不到，就退回机构表里的第一条，避免 NPE。
     */
    private OrgNode resolveCurrentOrg(DemoUser user, Map<String, OrgNode> orgMap) {
        OrgNode direct = orgMap.get(user.getOrgId());
        return direct == null ? firstOf(orgMap.values()) : direct;
    }

    /**
     * 所属行的推导规则：
     * - 总行用户：取路径中的最后一个 HEAD_OFFICE 节点
     * - 分行用户：取路径中的最后一个 BRANCH 节点
     * - 支行用户：取路径中的最后一个 SUB_BRANCH 节点
     *
     * 之所以取“最后一个”，是为了兼容一级/二级分行、一级/二级支行同时存在的场景。
     */
    private OrgNode resolveOwnerBankOrg(DemoUser user, Map<String, OrgNode> orgMap) {
        String[] pathIds = user.getOrgPathId().split("/");
        OrgLevel targetLevel = OrgLevel.valueOf(user.getUserOrgLevel().name());
        OrgNode lastMatched = null;
        for (String pathId : pathIds) {
            OrgNode org = orgMap.get(pathId);
            if (org != null && org.getOrgLevel() == targetLevel) {
                // 分行/支行支持一级、二级并存，所以这里取路径里最后一个同层级节点：
                // 一级分行用户命中一级分行，二级分行用户命中二级分行；支行同理。
                lastMatched = org;
            }
        }
        return lastMatched == null ? resolveCurrentOrg(user, orgMap) : lastMatched;
    }

    /**
     * 所属一级部门的定义：
     * “所属行”在路径里后面的下一个节点。
     *
     * 这里故意不要求该节点的 org_level 必须是 DEPT，
     * 因为业务上更关心的是“路径位置上的一级部门”，不是技术枚举值。
     *
     * 如果所属行后面没有节点，说明用户直接挂在总行/分行/支行下，
     * 这时会回退到所属行，并记录一个 fallback 标记给前端展示。
     */
    private OrgNode resolveFirstDeptOrg(DemoUser user,
                                        OrgNode ownerBankOrg,
                                        Map<String, OrgNode> orgMap,
                                        UserScopeContext context) {
        if (ownerBankOrg == null) {
            context.firstDeptFallbackToOwnerBank = true;
            return context.currentOrg;
        }
        String[] pathIds = user.getOrgPathId().split("/");
        for (int i = 0; i < pathIds.length; i++) {
            if (!ownerBankOrg.getOrgId().equals(pathIds[i])) {
                continue;
            }
            if (i + 1 < pathIds.length) {
                OrgNode nextNode = orgMap.get(pathIds[i + 1]);
                if (nextNode != null) {
                    // “所属一级部门下”严格按路径位置定义，不要求节点类型必须是 DEPT。
                    return nextNode;
                }
            }
            // 用户如果直接挂在总行/分行/支行节点下，就没有“所属一级部门”，这里回退到所属行。
            context.firstDeptFallbackToOwnerBank = true;
            return ownerBankOrg;
        }
        context.firstDeptFallbackToOwnerBank = true;
        return ownerBankOrg;
    }

    /**
     * 把 scopeType 解释成真正的“目标机构 + 命中方式”。
     *
     * 命中方式只有两种：
     * - EXACT：只看某一个机构点
     * - SUBTREE：以某个机构为根节点看整棵子树
     */
    private ResolvedTarget resolveTarget(ScopeType scopeType, UserScopeContext userScopeContext) {
        ResolvedTarget target = new ResolvedTarget();
        if (scopeType == ScopeType.CURRENT_ORG_ONLY) {
            target.org = userScopeContext.currentOrg;
            target.matchMode = "EXACT";
            target.scopeLabel = "当前机构";
            return target;
        }
        if (scopeType == ScopeType.CURRENT_ORG_AND_DESCENDANTS) {
            target.org = userScopeContext.currentOrg;
            target.matchMode = "SUBTREE";
            target.scopeLabel = "当前机构下";
            return target;
        }
        if (scopeType == ScopeType.OWNER_BANK_AND_DESCENDANTS) {
            target.org = userScopeContext.ownerBankOrg;
            target.matchMode = "SUBTREE";
            target.scopeLabel = "所属行";
            return target;
        }
        if (scopeType == ScopeType.FIRST_DEPT_AND_DESCENDANTS) {
            target.org = userScopeContext.firstDeptOrg;
            target.matchMode = "SUBTREE";
            target.scopeLabel = "所属一级部门";
            if (userScopeContext.firstDeptFallbackToOwnerBank) {
                target.scopeNote = "所属行后无一级部门，已回退到所属行";
            }
            return target;
        }
        throw new ConfigConflictException("不支持的数据范围类型: " + scopeType);
    }

    private OrgNode firstOf(Collection<OrgNode> orgNodes) {
        return orgNodes.iterator().next();
    }

    private Map<String, OrgNode> buildOrgMap(List<OrgNode> orgNodes) {
        Map<String, OrgNode> map = new LinkedHashMap<String, OrgNode>();
        for (OrgNode orgNode : orgNodes) {
            map.put(orgNode.getOrgId(), orgNode);
        }
        return map;
    }

    private List<String> extractAllOrgIds(Collection<OrgNode> orgNodes) {
        List<String> orgIds = new ArrayList<String>();
        for (OrgNode orgNode : orgNodes) {
            orgIds.add(orgNode.getOrgId());
        }
        return orgIds;
    }

    private static class RoleResolution {
        /**
         * 某一个角色经过规则计算后的中间结果。
         * resolve() 会把多个 RoleResolution 再做并集合并，形成用户最终范围。
         */
        private RuleMatch ruleMatch;
        private List<OrgNode> targetOrgs = new ArrayList<OrgNode>();
        private String matchMode;
        private ScopeType scopeType;
    }

    private static class UserScopeContext {
        /**
         * 为了避免在每条规则里反复解析用户机构路径，
         * 这里把常用锚点一次算好，后续 scopeType 直接引用。
         */
        private OrgNode currentOrg;
        private OrgNode ownerBankOrg;
        private OrgNode firstDeptOrg;
        private boolean firstDeptFallbackToOwnerBank;
    }

    private static class ResolvedTarget {
        private OrgNode org;
        private String matchMode;
        private String scopeLabel;
        private String scopeNote;
    }
}
