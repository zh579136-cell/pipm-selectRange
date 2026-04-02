package com.example.datascope.repository;

import com.example.datascope.domain.ScopeType;
import com.example.datascope.model.UserOverrideRule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserOverrideRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<UserOverrideRule> rowMapper = (rs, rowNum) -> {
        UserOverrideRule rule = new UserOverrideRule();
        rule.setId(rs.getLong("id"));
        rule.setUserId(rs.getString("user_id"));
        rule.setPageCode(rs.getString("page_code"));
        rule.setRoleCode(rs.getString("role_code"));
        String scopeType = rs.getString("scope_type");
        if (scopeType != null && !scopeType.trim().isEmpty()) {
            rule.setScopeType(ScopeType.valueOf(scopeType));
        }
        rule.setCustomOrgIds(parseCustomOrgIds(rs.getString("custom_org_ids")));
        rule.setEnabled(rs.getBoolean("enabled"));
        return rule;
    };

    public UserOverrideRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserOverrideRule> findAll(String userId, String pageCode, String roleCode) {
        StringBuilder sql = new StringBuilder("select id, user_id, page_code, role_code, scope_type, custom_org_ids, enabled from ds_user_override_rule where 1=1");
        List<Object> args = new ArrayList<Object>();
        if (userId != null && !userId.trim().isEmpty()) {
            sql.append(" and user_id = ?");
            args.add(userId.trim());
        }
        if (pageCode != null && !pageCode.trim().isEmpty()) {
            sql.append(" and page_code = ?");
            args.add(pageCode.trim());
        }
        if (roleCode != null && !roleCode.trim().isEmpty()) {
            sql.append(" and role_code = ?");
            args.add(roleCode.trim());
        }
        sql.append(" order by user_id, page_code, role_code, id");
        return jdbcTemplate.query(sql.toString(), rowMapper, args.toArray());
    }

    public List<UserOverrideRule> findEnabledRules(String userId, String pageCode, String roleCode) {
        return jdbcTemplate.query(
            "select id, user_id, page_code, role_code, scope_type, custom_org_ids, enabled from ds_user_override_rule "
                + "where enabled = true and user_id = ? and page_code = ? and role_code = ? order by id",
            rowMapper,
            userId,
            pageCode,
            roleCode
        );
    }

    public UserOverrideRule save(UserOverrideRule rule) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into ds_user_override_rule(user_id, page_code, role_code, scope_type, custom_org_ids, enabled) values (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, rule.getUserId());
            ps.setString(2, rule.getPageCode());
            ps.setString(3, rule.getRoleCode());
            ps.setString(4, rule.getScopeType() == null ? null : rule.getScopeType().name());
            ps.setString(5, joinCustomOrgIds(rule.getCustomOrgIds()));
            ps.setBoolean(6, rule.isEnabled());
            return ps;
        }, keyHolder);
        rule.setId(keyHolder.getKey().longValue());
        return rule;
    }

    public void update(UserOverrideRule rule) {
        jdbcTemplate.update(
            "update ds_user_override_rule set user_id = ?, page_code = ?, role_code = ?, scope_type = ?, custom_org_ids = ?, enabled = ? where id = ?",
            rule.getUserId(),
            rule.getPageCode(),
            rule.getRoleCode(),
            rule.getScopeType() == null ? null : rule.getScopeType().name(),
            joinCustomOrgIds(rule.getCustomOrgIds()),
            rule.isEnabled(),
            rule.getId()
        );
    }

    public UserOverrideRule findById(Long id) {
        List<UserOverrideRule> list = jdbcTemplate.query(
            "select id, user_id, page_code, role_code, scope_type, custom_org_ids, enabled from ds_user_override_rule where id = ?",
            rowMapper,
            id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("delete from ds_user_override_rule where id = ?", id);
    }

    public List<UserOverrideRule> findByUniqueKey(String userId, String pageCode, String roleCode) {
        return jdbcTemplate.query(
            "select id, user_id, page_code, role_code, scope_type, custom_org_ids, enabled from ds_user_override_rule where user_id = ? and page_code = ? and role_code = ?",
            rowMapper,
            userId,
            pageCode,
            roleCode
        );
    }

    private List<String> parseCustomOrgIds(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<String>();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .collect(Collectors.toList());
    }

    private String joinCustomOrgIds(List<String> customOrgIds) {
        if (customOrgIds == null || customOrgIds.isEmpty()) {
            return null;
        }
        return customOrgIds.stream()
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .collect(Collectors.joining(","));
    }
}
