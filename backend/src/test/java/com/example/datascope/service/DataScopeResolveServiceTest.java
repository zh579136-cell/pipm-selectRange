package com.example.datascope.service;

import com.example.datascope.domain.DataScopeMode;
import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.DemoReportQueryResponse;
import com.example.datascope.model.DemoUser;
import com.example.datascope.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DataScopeResolveServiceTest {

    @Autowired
    private DataScopeResolveService dataScopeResolveService;

    @Autowired
    private DemoReportQueryService demoReportQueryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldReturnAllWhenPageDisabled() {
        DemoUser user = userRepository.findByUserId("U100");
        DataScopeResult result = dataScopeResolveService.resolve("LEGACY_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.ALL);
        assertThat(result.isPageEnabled()).isFalse();
    }

    @Test
    void shouldResolveHeadOfficeUserToHeadOfficeTree() {
        DemoUser user = userRepository.findByUserId("U100");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.SUBTREE);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100001");
        assertThat(result.getVisibleOrgIdsPreview()).contains("100101", "100102", "100402");
    }

    @Test
    void shouldResolveBranchManagerToBranchTree() {
        DemoUser user = userRepository.findByUserId("U101");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100101");
        assertThat(result.getVisibleOrgIdsPreview()).contains("100201", "100202", "100501");
    }

    @Test
    void shouldResolveSubBranchViewerToSelfOnly() {
        DemoUser user = userRepository.findByUserId("U102");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.EXACT);
        assertThat(result.getExactOrgIds()).containsExactly("100201");
    }

    @Test
    void shouldResolveAuditorByJobKeywordToParentBranch() {
        DemoUser user = userRepository.findByUserId("U103");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100101");
    }

    @Test
    void shouldMergeMultipleRolesByUnion() {
        DemoUser user = userRepository.findByUserId("U104");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.MIXED);
        assertThat(result.getExactOrgIds()).contains("100301");
        assertThat(result.getSubtreeOrgIds()).contains("100102");
    }

    @Test
    void shouldApplyUserOverrideBeforeDefaultRule() {
        DemoUser user = userRepository.findByUserId("U105");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.ALL);
        assertThat(result.getMatchedRoleRules().get(0).getRuleSource().name()).isEqualTo("USER_OVERRIDE");
    }

    @Test
    void shouldReturnEmptyWhenNoRuleMatched() {
        jdbcTemplate.update("delete from ds_default_rule where role_code = 'AUDITOR'");
        DemoUser user = userRepository.findByUserId("U103");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.isAccessDenied()).isTrue();
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.EMPTY);
    }

    @Test
    void shouldUseSafePathMatchWithoutFalsePositive() {
        jdbcTemplate.update(
            "insert into sys_org(org_id, org_name, parent_org_id, org_level, org_pathid) values (?, ?, ?, ?, ?)",
            "1002019", "测试机构", "100101", "SUB_BRANCH", "100000/100001/100101/1002019"
        );
        jdbcTemplate.update(
            "insert into biz_demo_report(customer_name, metric_name, metric_value, org_id, org_pathid) values (?, ?, ?, ?, ?)",
            "误匹配样本", "客户数", "999", "1002019", "100000/100001/100101/1002019"
        );
        DemoUser user = userRepository.findByUserId("U102");
        DemoReportQueryResponse response = demoReportQueryService.query("DEMO_REPORT_QUERY", null, null, user);
        assertThat(response.getRows()).extracting("orgId").doesNotContain("1002019");
    }

    @Test
    void shouldRejectDuplicatePlainDefaultRule() {
        jdbcTemplate.update(
            "insert into ds_default_rule(page_code, role_code, user_org_level, job_keyword, scope_type, enabled) values (?, ?, ?, ?, ?, ?)",
            "DEMO_REPORT_QUERY", "REPORT_VIEWER", "SUB_BRANCH", null, "SELF_ONLY", true
        );
        DemoUser user = userRepository.findByUserId("U102");
        assertThatThrownBy(() -> dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user))
            .hasMessageContaining("默认规则冲突");
    }
}
