package com.example.datascope.repository;

import com.example.datascope.model.DemoReportRow;
import com.example.datascope.model.SqlCondition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DemoReportRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DemoReportRow> rowMapper = (rs, rowNum) -> {
        DemoReportRow row = new DemoReportRow();
        row.setId(rs.getLong("id"));
        row.setCustomerName(rs.getString("customer_name"));
        row.setMetricName(rs.getString("metric_name"));
        row.setMetricValue(rs.getString("metric_value"));
        row.setOrgId(rs.getString("org_id"));
        row.setOrgPathId(rs.getString("org_pathid"));
        row.setOrgName(rs.getString("org_name"));
        return row;
    };

    public DemoReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DemoReportRow> query(String customerName, String metricName, SqlCondition condition) {
        StringBuilder sql = new StringBuilder(
            "select r.id, r.customer_name, r.metric_name, r.metric_value, r.org_id, r.org_pathid, o.org_name "
                + "from biz_demo_report r join sys_org o on r.org_id = o.org_id where 1=1"
        );
        List<Object> args = new ArrayList<Object>();
        if (customerName != null && !customerName.trim().isEmpty()) {
            sql.append(" and upper(r.customer_name) like ?");
            args.add("%" + customerName.trim().toUpperCase() + "%");
        }
        if (metricName != null && !metricName.trim().isEmpty()) {
            sql.append(" and upper(r.metric_name) like ?");
            args.add("%" + metricName.trim().toUpperCase() + "%");
        }
        if (condition.getWhereClause() != null && !condition.getWhereClause().trim().isEmpty()) {
            sql.append(" and ").append(condition.getWhereClause());
            args.addAll(condition.getArgs());
        }
        sql.append(" order by r.id");
        return jdbcTemplate.query(sql.toString(), rowMapper, args.toArray());
    }
}
