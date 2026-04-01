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
import java.util.List;

@Repository
public class UserOverrideRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<UserOverrideRule> rowMapper = (rs, rowNum) -> {
        UserOverrideRule rule = new UserOverrideRule();
        rule.setId(rs.getLong("id"));
        rule.setUserId(rs.getString("user_id"));
        rule.setPageCode(rs.getString("page_code"));
        rule.setRoleCode(rs.getString("role_code"));
        rule.setScopeType(ScopeType.valueOf(rs.getString("scope_type")));
        rule.setEnabled(rs.getBoolean("enabled"));
        return rule;
    };

    public UserOverrideRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserOverrideRule> findAll(String userId, String pageCode, String roleCode) {
        StringBuilder sql = new StringBuilder("select id, user_id, page_code, role_code, scope_type, enabled from ds_user_override_rule where 1=1");
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
            "select id, user_id, page_code, role_code, scope_type, enabled from ds_user_override_rule "
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
                "insert into ds_user_override_rule(user_id, page_code, role_code, scope_type, enabled) values (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, rule.getUserId());
            ps.setString(2, rule.getPageCode());
            ps.setString(3, rule.getRoleCode());
            ps.setString(4, rule.getScopeType().name());
            ps.setBoolean(5, rule.isEnabled());
            return ps;
        }, keyHolder);
        rule.setId(keyHolder.getKey().longValue());
        return rule;
    }

    public void update(UserOverrideRule rule) {
        jdbcTemplate.update(
            "update ds_user_override_rule set user_id = ?, page_code = ?, role_code = ?, scope_type = ?, enabled = ? where id = ?",
            rule.getUserId(),
            rule.getPageCode(),
            rule.getRoleCode(),
            rule.getScopeType().name(),
            rule.isEnabled(),
            rule.getId()
        );
    }

    public UserOverrideRule findById(Long id) {
        List<UserOverrideRule> list = jdbcTemplate.query(
            "select id, user_id, page_code, role_code, scope_type, enabled from ds_user_override_rule where id = ?",
            rowMapper,
            id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public List<UserOverrideRule> findByUniqueKey(String userId, String pageCode, String roleCode) {
        return jdbcTemplate.query(
            "select id, user_id, page_code, role_code, scope_type, enabled from ds_user_override_rule where user_id = ? and page_code = ? and role_code = ?",
            rowMapper,
            userId,
            pageCode,
            roleCode
        );
    }
}
