package com.example.datascope.service;

import com.example.datascope.domain.DataScopeMode;
import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.DemoReportQueryResponse;
import com.example.datascope.model.DemoUser;
import com.example.datascope.repository.UserRepository;
import com.example.datascope.web.request.UserOverrideRuleRequest;
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
    private DefaultRuleService defaultRuleService;

    @Autowired
    private UserOverrideRuleService userOverrideRuleService;

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
    void shouldResolveHeadOfficeUserToFirstDepartmentTree() {
        DemoUser user = userRepository.findByUserId("U100");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.SUBTREE);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100401");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeLabel()).isEqualTo("所属一级部门");
        assertThat(result.getVisibleOrgIdsPreview()).contains("100401", "100402");
    }

    @Test
    void shouldResolveHeadOfficeOwnerBankToHeadOfficeNode() {
        jdbcTemplate.update(
            "update ds_default_rule set scope_type = ? where page_code = ? and role_code = ? and user_org_level = ? and job_keyword is null",
            "OWNER_BANK_AND_DESCENDANTS", "DEMO_REPORT_QUERY", "REPORT_VIEWER", "HEAD_OFFICE"
        );

        DemoUser user = userRepository.findByUserId("U100");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100001");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeLabel()).isEqualTo("所属行");
    }

    @Test
    void shouldResolveFirstLevelBranchManagerToBranchTree() {
        DemoUser user = userRepository.findByUserId("U101");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100101");
        assertThat(result.getVisibleOrgIdsPreview()).contains("100111", "100201", "100202", "100501", "100511");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeLabel()).isEqualTo("所属行");
    }

    @Test
    void shouldResolveSecondLevelBranchManagerToNearestBranchTree() {
        DemoUser user = userRepository.findByUserId("U106");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100111");
        assertThat(result.getVisibleOrgIdsPreview()).contains("100111", "100511");
        assertThat(result.getVisibleOrgIdsPreview()).doesNotContain("100201", "100202");
    }

    @Test
    void shouldResolveSubBranchViewerToCurrentOrgOnly() {
        DemoUser user = userRepository.findByUserId("U102");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.EXACT);
        assertThat(result.getExactOrgIds()).containsExactly("100201");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeLabel()).isEqualTo("当前机构");
    }

    @Test
    void shouldResolveFirstLevelSubBranchAuditorToOwnerBankTree() {
        DemoUser user = userRepository.findByUserId("U103");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100201");
        assertThat(result.getVisibleOrgIdsPreview()).contains("100201", "100211", "100521", "100531");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeLabel()).isEqualTo("所属行");
    }

    @Test
    void shouldResolveSecondLevelSubBranchAuditorToNearestSubBranchTree() {
        DemoUser user = userRepository.findByUserId("U107");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100211");
        assertThat(result.getVisibleOrgIdsPreview()).contains("100211", "100531");
        assertThat(result.getVisibleOrgIdsPreview()).doesNotContain("100202", "100521");
    }

    @Test
    void shouldFallbackFirstDeptToOwnerBankWhenNoNextPathNode() {
        jdbcTemplate.update("update sys_user set org_id = ? where user_id = ?", "100101", "U101");
        jdbcTemplate.update(
            "update ds_default_rule set scope_type = ? where page_code = ? and role_code = ? and user_org_level = ? and job_keyword is null",
            "FIRST_DEPT_AND_DESCENDANTS", "DEMO_REPORT_QUERY", "BRANCH_MANAGER", "BRANCH"
        );

        DemoUser user = userRepository.findByUserId("U101");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100101");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeLabel()).isEqualTo("所属一级部门");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeNote()).contains("回退到所属行");
    }

    @Test
    void shouldResolveBranchViewerToCurrentOrgAndDescendants() {
        DemoUser user = userRepository.findByUserId("U108");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.SUBTREE);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100501");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeLabel()).isEqualTo("当前机构下");
    }

    @Test
    void shouldMergeMultipleRolesByUnion() {
        DemoUser user = userRepository.findByUserId("U104");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.MIXED);
        assertThat(result.getExactOrgIds()).contains("100301");
        assertThat(result.getSubtreeOrgIds()).contains("100301");
    }

    @Test
    void shouldApplyUserOverrideBeforeDefaultRule() {
        DemoUser user = userRepository.findByUserId("U105");
        DataScopeResult result = dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user);
        assertThat(result.getScopeMode()).isEqualTo(DataScopeMode.SUBTREE);
        assertThat(result.getSubtreeOrgIds()).containsExactly("100000");
        assertThat(result.getVisibleOrgIdsPreview()).contains("100001", "100101", "100102", "100211");
        assertThat(result.getMatchedRoleRules().get(0).getRuleSource().name()).isEqualTo("USER_OVERRIDE");
        assertThat(result.getMatchedRoleRules().get(0).getResolvedScopeLabel()).isEqualTo("个人覆盖根节点");
    }

    @Test
    void shouldRejectMultipleCustomOrgIdsInUserOverrideRequest() {
        UserOverrideRuleRequest request = new UserOverrideRuleRequest();
        request.setUserId("U105");
        request.setPageCode("DEMO_REPORT_QUERY");
        request.setRoleCode("REPORT_VIEWER");
        request.setCustomOrgIds(java.util.Arrays.asList("100101", "100102"));
        request.setEnabled(true);

        assertThatThrownBy(() -> userOverrideRuleService.create(request))
            .hasMessageContaining("个人覆盖只能选择一个机构根节点");
    }

    @Test
    void shouldRejectDirtyUserOverrideWithMultipleRootNodesAtResolveTime() {
        jdbcTemplate.update(
            "update ds_user_override_rule set custom_org_ids = ? where user_id = ? and page_code = ? and role_code = ?",
            "100101,100102", "U105", "DEMO_REPORT_QUERY", "REPORT_VIEWER"
        );

        DemoUser user = userRepository.findByUserId("U105");
        assertThatThrownBy(() -> dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user))
            .hasMessageContaining("个人覆盖只能配置一个机构根节点");
    }

    @Test
    void shouldDeleteKeywordRule() {
        Long keywordRuleId = jdbcTemplate.queryForObject(
            "select id from ds_default_rule where page_code = ? and role_code = ? and user_org_level = ? and job_keyword = ?",
            Long.class,
            "DEMO_REPORT_QUERY",
            "AUDITOR",
            "SUB_BRANCH",
            "审计"
        );

        defaultRuleService.delete(keywordRuleId);

        Integer count = jdbcTemplate.queryForObject(
            "select count(1) from ds_default_rule where id = ?",
            Integer.class,
            keywordRuleId
        );
        assertThat(count).isZero();
    }

    @Test
    void shouldDeleteUserOverrideRule() {
        Long ruleId = jdbcTemplate.queryForObject(
            "select id from ds_user_override_rule where user_id = ? and page_code = ? and role_code = ?",
            Long.class,
            "U105",
            "DEMO_REPORT_QUERY",
            "REPORT_VIEWER"
        );

        userOverrideRuleService.delete(ruleId);

        Integer count = jdbcTemplate.queryForObject(
            "select count(1) from ds_user_override_rule where id = ?",
            Integer.class,
            ruleId
        );
        assertThat(count).isZero();
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
        DemoUser user = userRepository.findByUserId("U103");
        DemoReportQueryResponse response = demoReportQueryService.query("DEMO_REPORT_QUERY", null, null, user);
        assertThat(response.getRows()).extracting("orgId").doesNotContain("1002019");
    }

    @Test
    void shouldRejectDuplicatePlainDefaultRule() {
        jdbcTemplate.update(
            "insert into ds_default_rule(page_code, role_code, user_org_level, job_keyword, scope_type, enabled) values (?, ?, ?, ?, ?, ?)",
            "DEMO_REPORT_QUERY", "REPORT_VIEWER", "SUB_BRANCH", null, "CURRENT_ORG_ONLY", true
        );
        DemoUser user = userRepository.findByUserId("U102");
        assertThatThrownBy(() -> dataScopeResolveService.resolve("DEMO_REPORT_QUERY", user))
            .hasMessageContaining("默认规则冲突");
    }
}
