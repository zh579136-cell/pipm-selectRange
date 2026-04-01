package com.example.datascope.repository;

import com.example.datascope.domain.UserOrgLevel;
import com.example.datascope.model.DemoUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DemoUser> rowMapper = (rs, rowNum) -> {
        DemoUser user = new DemoUser();
        user.setUserId(rs.getString("user_id"));
        user.setUserName(rs.getString("user_name"));
        user.setLoginName(rs.getString("login_name"));
        user.setOrgId(rs.getString("org_id"));
        user.setOrgName(rs.getString("org_name"));
        user.setOrgPathId(rs.getString("org_pathid"));
        user.setUserOrgLevel(UserOrgLevel.valueOf(rs.getString("org_level_for_scope")));
        user.setPositionName(rs.getString("position_name"));
        return user;
    };

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DemoUser findByUserId(String userId) {
        List<DemoUser> users = jdbcTemplate.query(
            "select u.user_id, u.user_name, u.login_name, u.org_id, u.org_level_for_scope, u.position_name, "
                + "o.org_name, o.org_pathid "
                + "from sys_user u join sys_org o on u.org_id = o.org_id where u.user_id = ?",
            rowMapper,
            userId
        );
        if (users.isEmpty()) {
            return null;
        }
        DemoUser user = users.get(0);
        user.setRoleCodes(findRoleCodes(userId));
        return user;
    }

    public List<DemoUser> findAll() {
        List<DemoUser> users = jdbcTemplate.query(
            "select u.user_id, u.user_name, u.login_name, u.org_id, u.org_level_for_scope, u.position_name, "
                + "o.org_name, o.org_pathid "
                + "from sys_user u join sys_org o on u.org_id = o.org_id order by u.user_id",
            rowMapper
        );
        Map<String, List<String>> roleMap = findAllRoleCodes();
        for (DemoUser user : users) {
            List<String> roles = roleMap.get(user.getUserId());
            user.setRoleCodes(roles == null ? new ArrayList<String>() : roles);
        }
        return users;
    }

    private List<String> findRoleCodes(String userId) {
        return jdbcTemplate.query(
            "select role_code from sys_user_role where user_id = ? order by role_code",
            (rs, rowNum) -> rs.getString("role_code"),
            userId
        );
    }

    private Map<String, List<String>> findAllRoleCodes() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select user_id, role_code from sys_user_role order by user_id, role_code");
        Map<String, List<String>> roleMap = new LinkedHashMap<String, List<String>>();
        for (Map<String, Object> row : rows) {
            String userId = (String) row.get("user_id");
            String roleCode = (String) row.get("role_code");
            if (!roleMap.containsKey(userId)) {
                roleMap.put(userId, new ArrayList<String>());
            }
            roleMap.get(userId).add(roleCode);
        }
        return roleMap;
    }
}
