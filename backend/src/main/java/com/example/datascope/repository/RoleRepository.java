package com.example.datascope.repository;

import com.example.datascope.model.RoleInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<RoleInfo> rowMapper = (rs, rowNum) -> {
        RoleInfo role = new RoleInfo();
        role.setRoleCode(rs.getString("role_code"));
        role.setRoleName(rs.getString("role_name"));
        return role;
    };

    public RoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RoleInfo> findAll() {
        return jdbcTemplate.query("select role_code, role_name from sys_role order by role_code", rowMapper);
    }
}
