package com.example.datascope.service;

import com.example.datascope.domain.DataScopeMode;
import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.SqlCondition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataScopeSqlBuilder {

    public SqlCondition build(DataScopeResult result) {
        SqlCondition condition = new SqlCondition();
        if (!result.isPageEnabled() || result.getScopeMode() == DataScopeMode.ALL) {
            condition.setWhereClause("1=1");
            condition.setPreview("1=1");
            return condition;
        }
        if (result.isAccessDenied() || result.getScopeMode() == DataScopeMode.EMPTY) {
            condition.setWhereClause("1=0");
            condition.setPreview("1=0");
            return condition;
        }

        List<String> parts = new ArrayList<String>();
        List<String> previewParts = new ArrayList<String>();
        List<Object> args = new ArrayList<Object>();

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

        for (String orgId : result.getSubtreeOrgIds()) {
            parts.add("concat('/', r.org_pathid, '/') like ?");
            previewParts.add("concat('/', r.org_pathid, '/') like '%/" + orgId + "/%'");
            args.add("%/" + orgId + "/%");
        }

        String where = "(" + String.join(" or ", parts) + ")";
        condition.setWhereClause(where);
        condition.setArgs(args);
        condition.setPreview("(" + String.join(" or ", previewParts) + ")");
        return condition;
    }
}
