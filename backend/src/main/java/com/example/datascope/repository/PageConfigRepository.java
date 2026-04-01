package com.example.datascope.repository;

import com.example.datascope.model.PageConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PageConfigRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PageConfig> rowMapper = (rs, rowNum) -> {
        PageConfig config = new PageConfig();
        config.setPageCode(rs.getString("page_code"));
        config.setPageName(rs.getString("page_name"));
        config.setEnabled(rs.getBoolean("enabled"));
        return config;
    };

    public PageConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PageConfig> findAll() {
        return jdbcTemplate.query("select page_code, page_name, enabled from ds_page_config order by page_code", rowMapper);
    }

    public PageConfig findByPageCode(String pageCode) {
        List<PageConfig> list = jdbcTemplate.query(
            "select page_code, page_name, enabled from ds_page_config where page_code = ?",
            rowMapper,
            pageCode
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public void save(PageConfig config) {
        PageConfig exists = findByPageCode(config.getPageCode());
        if (exists == null) {
            jdbcTemplate.update(
                "insert into ds_page_config(page_code, page_name, enabled) values (?, ?, ?)",
                config.getPageCode(),
                config.getPageName(),
                config.isEnabled()
            );
            return;
        }
        jdbcTemplate.update(
            "update ds_page_config set page_name = ?, enabled = ? where page_code = ?",
            config.getPageName(),
            config.isEnabled(),
            config.getPageCode()
        );
    }
}
