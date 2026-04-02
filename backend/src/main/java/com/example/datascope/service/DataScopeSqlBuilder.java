package com.example.datascope.service;

import com.example.datascope.domain.DataScopeMode;
import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.SqlCondition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
/**
 * 数据范围 SQL 翻译器。
 *
 * DataScopeResolveService 负责“算出能看哪些机构”，
 * 这个类负责“把这些机构范围翻译成 SQL where 条件”。
 *
 * 这样做的好处是：
 * - 规则计算和 SQL 生成解耦
 * - 前端试算时可以直接展示 SQL 预览
 * - 真实业务查询只需要接收一个 SqlCondition，不需要关心规则细节
 */
public class DataScopeSqlBuilder {

    /**
     * 把数据范围结果翻译成可直接挂到查询 SQL 上的 where 条件。
     *
     * 这里约定业务查询主表别名为 r：
     * - “仅当前机构” => r.org_id in (...)
     * - “所属一级部门下 / 所属行 / 当前机构下” => concat('/', r.org_pathid, '/') like '%/机构ID/%'
     */
    public SqlCondition build(DataScopeResult result) {
        SqlCondition condition = new SqlCondition();

        // 页面未启用数据范围控制时，不追加任何限制条件。
        if (!result.isPageEnabled() || result.getScopeMode() == DataScopeMode.ALL) {
            condition.setWhereClause("1=1");
            condition.setPreview("1=1");
            return condition;
        }

        // 没命中规则或显式无权限时，直接生成一个恒 false 条件。
        // 这样业务查询层不需要自己再写一套“无权限”分支。
        if (result.isAccessDenied() || result.getScopeMode() == DataScopeMode.EMPTY) {
            condition.setWhereClause("1=0");
            condition.setPreview("1=0");
            return condition;
        }

        List<String> parts = new ArrayList<String>();
        List<String> previewParts = new ArrayList<String>();
        List<Object> args = new ArrayList<Object>();

        // EXACT 范围对应“只看具体某几个机构点”。
        // 例如 CURRENT_ORG_ONLY 最终就会落到这里。
        if (!result.getExactOrgIds().isEmpty()) {
            StringBuilder placeholders = new StringBuilder();
            StringBuilder preview = new StringBuilder();
            for (int i = 0; i < result.getExactOrgIds().size(); i++) {
                if (i > 0) {
                    placeholders.append(", ");
                    preview.append(", ");
                }
                placeholders.append("?");
                preview.append("'").append(result.getExactOrgIds().get(i)).append("'");
                args.add(result.getExactOrgIds().get(i));
            }
            parts.add("r.org_id in (" + placeholders + ")");
            previewParts.add("r.org_id in (" + preview + ")");
        }

        // SUBTREE 范围对应“以某个机构为根节点看整棵子树”。
        // 这里统一改造成 like '%/机构ID/%' 形式，避免路径片段误命中。
        for (String orgId : result.getSubtreeOrgIds()) {
            // 统一把 orgpathid 包上前后斜杠，防止路径片段误命中。
            parts.add("concat('/', r.org_pathid, '/') like ?");
            previewParts.add("concat('/', r.org_pathid, '/') like '%/" + orgId + "/%'");
            args.add("%/" + orgId + "/%");
        }

        // 多角色并集、多根节点子树最终都用 OR 拼起来。
        String where = "(" + String.join(" or ", parts) + ")";
        condition.setWhereClause(where);
        condition.setArgs(args);
        condition.setPreview("(" + String.join(" or ", previewParts) + ")");
        return condition;
    }
}
