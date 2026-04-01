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

    public DataScopeResult resolve(String pageCode, DemoUser user) {
        DataScopeResult result = new DataScopeResult();
        result.setPageCode(pageCode);

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
        Map<String, OrgNode> orgMap = buildOrgMap(orgRepository.findAll());
        OrgNode anchorOrg = resolveAnchorOrg(user, orgMap);

        Set<String> exactOrgIds = new LinkedHashSet<String>();
        Set<String> subtreeOrgIds = new LinkedHashSet<String>();
        boolean allOrgs = false;

        for (String roleCode : user.getRoleCodes()) {
            RoleResolution roleResolution = resolveRole(pageCode, user, roleCode, anchorOrg, orgMap);
            if (roleResolution == null) {
                continue;
            }

            result.getMatchedRoleRules().add(roleResolution.ruleMatch);
            if (roleResolution.scopeType == ScopeType.ALL_ORGS) {
                allOrgs = true;
                break;
            }
            if ("EXACT".equals(roleResolution.matchMode)) {
                exactOrgIds.add(roleResolution.targetOrg.getOrgId());
            } else {
                subtreeOrgIds.add(roleResolution.targetOrg.getOrgId());
            }
            result.getResolvedScopeRoots().add(createScopeRoot(roleResolution.targetOrg, roleResolution.matchMode));
        }

        if (allOrgs) {
            result.setScopeMode(DataScopeMode.ALL);
            result.setMessage("命中全行规则。");
            result.setVisibleOrgIdsPreview(extractAllOrgIds(orgMap.values()));
        } else if (result.getMatchedRoleRules().isEmpty()) {
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

        SqlCondition condition = dataScopeSqlBuilder.build(result);
        result.setSqlPreview(condition.getPreview());
        return result;
    }

    private RoleResolution resolveRole(String pageCode,
                                       DemoUser user,
                                       String roleCode,
                                       OrgNode anchorOrg,
                                       Map<String, OrgNode> orgMap) {
        List<UserOverrideRule> overrides = userOverrideRuleRepository.findEnabledRules(user.getUserId(), pageCode, roleCode);
        if (overrides.size() > 1) {
            throw new ConfigConflictException("用户个人覆盖规则冲突，用户=" + user.getUserId() + "，角色=" + roleCode);
        }
        if (!overrides.isEmpty()) {
            return toRoleResolution(roleCode, overrides.get(0).getId(), RuleSource.USER_OVERRIDE, overrides.get(0).getScopeType(), "个人覆盖", anchorOrg, orgMap);
        }

        List<DefaultRule> defaults = defaultRuleRepository.findEnabledRules(pageCode, roleCode, user.getUserOrgLevel());
        List<DefaultRule> withJobKeyword = new ArrayList<DefaultRule>();
        List<DefaultRule> plain = new ArrayList<DefaultRule>();
        for (DefaultRule rule : defaults) {
            if (rule.getJobKeyword() != null && !rule.getJobKeyword().trim().isEmpty()) {
                if (matchesJobKeyword(user.getPositionName(), rule.getJobKeyword())) {
                    withJobKeyword.add(rule);
                }
            } else {
                plain.add(rule);
            }
        }

        if (withJobKeyword.size() > 1) {
            throw new ConfigConflictException("默认规则冲突，页面=" + pageCode + "，角色=" + roleCode + " 存在多条职位关键字规则。");
        }
        if (plain.size() > 1) {
            throw new ConfigConflictException("默认规则冲突，页面=" + pageCode + "，角色=" + roleCode + " 存在多条基础规则。");
        }
        if (!withJobKeyword.isEmpty()) {
            DefaultRule rule = withJobKeyword.get(0);
            return toRoleResolution(roleCode, rule.getId(), RuleSource.DEFAULT_WITH_JOB, rule.getScopeType(), "职位匹配：" + rule.getJobKeyword(), anchorOrg, orgMap);
        }
        if (!plain.isEmpty()) {
            DefaultRule rule = plain.get(0);
            return toRoleResolution(roleCode, rule.getId(), RuleSource.DEFAULT, rule.getScopeType(), "默认规则", anchorOrg, orgMap);
        }
        return null;
    }

    private RoleResolution toRoleResolution(String roleCode,
                                            Long ruleId,
                                            RuleSource source,
                                            ScopeType scopeType,
                                            String matchedBy,
                                            OrgNode anchorOrg,
                                            Map<String, OrgNode> orgMap) {
        RuleMatch match = new RuleMatch();
        match.setRoleCode(roleCode);
        match.setRuleId(ruleId);
        match.setRuleSource(source);
        match.setScopeType(scopeType);
        match.setMatchedBy(matchedBy);

        OrgNode targetOrg = anchorOrg;
        String matchMode = "SUBTREE";
        if (scopeType == ScopeType.SELF_ONLY) {
            matchMode = "EXACT";
        } else if (scopeType == ScopeType.PARENT_AND_DESCENDANTS) {
            OrgNode parent = orgMap.get(anchorOrg.getParentOrgId());
            if (parent != null) {
                targetOrg = parent;
            }
            matchMode = "SUBTREE";
        } else if (scopeType == ScopeType.ALL_ORGS) {
            targetOrg = null;
            matchMode = "ALL";
        }

        if (targetOrg != null) {
            match.setResolvedOrgId(targetOrg.getOrgId());
            match.setResolvedOrgName(targetOrg.getOrgName());
        } else {
            match.setResolvedOrgId("ALL");
            match.setResolvedOrgName("全行");
        }

        RoleResolution resolution = new RoleResolution();
        resolution.ruleMatch = match;
        resolution.targetOrg = targetOrg;
        resolution.matchMode = matchMode;
        resolution.scopeType = scopeType;
        return resolution;
    }

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
        return ("/" + orgPathId + "/").contains("/" + orgId + "/");
    }

    private OrgNode resolveAnchorOrg(DemoUser user, Map<String, OrgNode> orgMap) {
        String[] pathIds = user.getOrgPathId().split("/");
        OrgLevel targetLevel = OrgLevel.valueOf(user.getUserOrgLevel().name());
        for (String pathId : pathIds) {
            OrgNode org = orgMap.get(pathId);
            if (org != null && org.getOrgLevel() == targetLevel) {
                return org;
            }
        }
        OrgNode direct = orgMap.get(user.getOrgId());
        return direct == null ? firstOf(orgMap.values()) : direct;
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
        private RuleMatch ruleMatch;
        private OrgNode targetOrg;
        private String matchMode;
        private ScopeType scopeType;
    }
}
