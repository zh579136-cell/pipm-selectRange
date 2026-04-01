<template>
  <div class="page-shell">
    <div class="page-card">
      <div class="toolbar">
        <el-input v-model="form.customerName" clearable placeholder="按客户名筛选" @keyup.enter.native="loadData" />
        <el-input v-model="form.metricName" clearable placeholder="按指标名筛选" @keyup.enter.native="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetForm">重置</el-button>
        <span class="muted-text">固定 pageCode: <strong>DEMO_REPORT_QUERY</strong></span>
      </div>
    </div>

    <div class="page-card">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 16px;">
        <div>
          <div style="font-size: 18px; font-weight: 600;">本次数据范围计算结果</div>
          <div class="muted-text" style="margin-top: 6px;">{{ scope.message || '暂无说明' }}</div>
        </div>
        <el-tag :type="scope.accessDenied ? 'danger' : 'success'">{{ scope.scopeMode || 'N/A' }}</el-tag>
      </div>

      <div class="result-grid">
        <div>
          <div style="font-weight: 600; margin-bottom: 10px;">命中的角色规则</div>
          <el-table :data="scope.matchedRoleRules || []" size="small" border>
            <el-table-column prop="roleCode" label="角色" min-width="120" />
            <el-table-column prop="ruleSource" label="规则来源" min-width="120" />
            <el-table-column prop="scopeType" label="范围类型" min-width="160" />
            <el-table-column prop="matchedBy" label="命中原因" min-width="160" />
            <el-table-column prop="resolvedOrgName" label="锚点机构" min-width="160" />
          </el-table>
        </div>
        <div>
          <div style="font-weight: 600; margin-bottom: 10px;">最终可见机构</div>
          <div>
            <span
              v-for="root in scope.resolvedScopeRoots || []"
              :key="`${root.orgId}-${root.matchMode}`"
              class="code-chip"
            >
              {{ root.orgName }} / {{ root.matchMode }}
            </span>
            <span v-if="scope.scopeMode === 'ALL'" class="code-chip">全行</span>
          </div>
          <div style="margin-top: 16px; font-weight: 600;">SQL 预览</div>
          <div class="sql-preview">{{ scope.sqlPreview || '暂无' }}</div>
          <div style="margin-top: 16px; font-weight: 600;">可见机构 ID 预览</div>
          <div>
            <span v-for="orgId in scope.visibleOrgIdsPreview || []" :key="orgId" class="code-chip">{{ orgId }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="page-card">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px;">
        <div>
          <div style="font-size: 18px; font-weight: 600;">模拟查询结果</div>
          <div class="muted-text" style="margin-top: 6px;">业务表中保留了 `orgid` 与 `orgpathid`，后端会按数据范围自动过滤。</div>
        </div>
        <el-tag>{{ rows.length }} 条</el-tag>
      </div>
      <el-table :data="rows" border>
        <el-table-column prop="customerName" label="客户/主题" min-width="180" />
        <el-table-column prop="metricName" label="指标" min-width="120" />
        <el-table-column prop="metricValue" label="值" min-width="100" />
        <el-table-column prop="orgName" label="所属机构" min-width="160" />
        <el-table-column prop="orgId" label="orgid" min-width="120" />
        <el-table-column prop="orgPathId" label="orgpathid" min-width="220" />
      </el-table>
    </div>
  </div>
</template>

<script>
import { queryDemoReport } from '../api';

export default {
  name: 'DemoReportPage',
  data() {
    return {
      form: {
        customerName: '',
        metricName: ''
      },
      scope: {},
      rows: []
    };
  },
  created() {
    this.loadData();
    this.$root.$on('demo-user-changed', this.loadData);
  },
  beforeDestroy() {
    this.$root.$off('demo-user-changed', this.loadData);
  },
  methods: {
    async loadData() {
      const { data } = await queryDemoReport({
        pageCode: 'DEMO_REPORT_QUERY',
        customerName: this.form.customerName,
        metricName: this.form.metricName
      });
      this.scope = data.scope || {};
      this.rows = data.rows || [];
    },
    resetForm() {
      this.form.customerName = '';
      this.form.metricName = '';
      this.loadData();
    }
  }
};
</script>
