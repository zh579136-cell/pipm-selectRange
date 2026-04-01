package com.example.datascope.repository;

import com.example.datascope.domain.OrgLevel;
import com.example.datascope.model.OrgNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrgRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<OrgNode> rowMapper = (rs, rowNum) -> {
        OrgNode org = new OrgNode();
        org.setOrgId(rs.getString("org_id"));
        org.setOrgName(rs.getString("org_name"));
        org.setParentOrgId(rs.getString("parent_org_id"));
        org.setOrgLevel(OrgLevel.valueOf(rs.getString("org_level")));
        org.setOrgPathId(rs.getString("org_pathid"));
        return org;
    };

    public OrgRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrgNode> findAll() {
        return jdbcTemplate.query("select org_id, org_name, parent_org_id, org_level, org_pathid from sys_org order by org_pathid", rowMapper);
    }
}
