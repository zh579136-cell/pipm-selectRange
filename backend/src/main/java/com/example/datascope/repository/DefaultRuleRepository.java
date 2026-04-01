package com.example.datascope.repository;

import com.example.datascope.domain.ScopeType;
import com.example.datascope.domain.UserOrgLevel;
import com.example.datascope.model.DefaultRule;
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
public class DefaultRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DefaultRule> rowMapper = (rs, rowNum) -> {
        DefaultRule rule = new DefaultRule();
        rule.setId(rs.getLong("id"));
        rule.setPageCode(rs.getString("page_code"));
        rule.setRoleCode(rs.getString("role_code"));
        rule.setUserOrgLevel(UserOrgLevel.valueOf(rs.getString("user_org_level")));
        rule.setJobKeyword(rs.getString("job_keyword"));
        rule.setScopeType(ScopeType.valueOf(rs.getString("scope_type")));
        rule.setEnabled(rs.getBoolean("enabled"));
        return rule;
    };

    public DefaultRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DefaultRule> findAll(String pageCode, String roleCode, String userOrgLevel) {
        StringBuilder sql = new StringBuilder("select id, page_code, role_code, user_org_level, job_keyword, scope_type, enabled from ds_default_rule where 1=1");
        List<Object> args = new ArrayList<Object>();
        if (pageCode != null && !pageCode.trim().isEmpty()) {
            sql.append(" and page_code = ?");
            args.add(pageCode.trim());
        }
        if (roleCode != null && !roleCode.trim().isEmpty()) {
            sql.append(" and role_code = ?");
            args.add(roleCode.trim());
        }
        if (userOrgLevel != null && !userOrgLevel.trim().isEmpty()) {
            sql.append(" and user_org_level = ?");
            args.add(userOrgLevel.trim());
        }
        sql.append(" order by page_code, role_code, user_org_level, case when job_keyword is null then 1 else 0 end, job_keyword, id");
        return jdbcTemplate.query(sql.toString(), rowMapper, args.toArray());
    }

    public List<DefaultRule> findEnabledRules(String pageCode, String roleCode, UserOrgLevel userOrgLevel) {
        return jdbcTemplate.query(
            "select id, page_code, role_code, user_org_level, job_keyword, scope_type, enabled "
                + "from ds_default_rule where enabled = true and page_code = ? and role_code = ? and user_org_level = ? order by id",
            rowMapper,
            pageCode,
            roleCode,
            userOrgLevel.name()
        );
    }

    public DefaultRule save(DefaultRule rule) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into ds_default_rule(page_code, role_code, user_org_level, job_keyword, scope_type, enabled) values (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, rule.getPageCode());
            ps.setString(2, rule.getRoleCode());
            ps.setString(3, rule.getUserOrgLevel().name());
            ps.setString(4, rule.getJobKeyword());
            ps.setString(5, rule.getScopeType().name());
            ps.setBoolean(6, rule.isEnabled());
            return ps;
        }, keyHolder);
        rule.setId(keyHolder.getKey().longValue());
        return rule;
    }

    public void update(DefaultRule rule) {
        jdbcTemplate.update(
            "update ds_default_rule set page_code = ?, role_code = ?, user_org_level = ?, job_keyword = ?, scope_type = ?, enabled = ? where id = ?",
            rule.getPageCode(),
            rule.getRoleCode(),
            rule.getUserOrgLevel().name(),
            rule.getJobKeyword(),
            rule.getScopeType().name(),
            rule.isEnabled(),
            rule.getId()
        );
    }

    public DefaultRule findById(Long id) {
        List<DefaultRule> list = jdbcTemplate.query(
            "select id, page_code, role_code, user_org_level, job_keyword, scope_type, enabled from ds_default_rule where id = ?",
            rowMapper,
            id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public List<DefaultRule> findByUniqueKey(String pageCode, String roleCode, UserOrgLevel userOrgLevel) {
        return jdbcTemplate.query(
            "select id, page_code, role_code, user_org_level, job_keyword, scope_type, enabled "
                + "from ds_default_rule where page_code = ? and role_code = ? and user_org_level = ?",
            rowMapper,
            pageCode,
            roleCode,
            userOrgLevel.name()
        );
    }
}
