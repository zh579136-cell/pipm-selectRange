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
            <el-table-column label="命中规则档位" min-width="140">
              <template slot-scope="{ row }">{{ matchedLevelLabel(row) }}</template>
            </el-table-column>
            <el-table-column label="范围类型" min-width="160">
              <template slot-scope="{ row }">{{ scopeTypeLabel(row.scopeType) }}</template>
            </el-table-column>
            <el-table-column label="根节点类型" min-width="140">
              <template slot-scope="{ row }">{{ row.resolvedScopeLabel || '-' }}</template>
            </el-table-column>
            <el-table-column prop="matchedBy" label="命中原因" min-width="160" />
            <el-table-column label="命中根节点" min-width="220">
              <template slot-scope="{ row }">
                {{ formatResolvedOrg(row) }}
                <div v-if="row.resolvedScopeNote" class="muted-text" style="font-size:12px; margin-top: 4px;">
                  {{ row.resolvedScopeNote }}
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div>
          <div style="font-weight: 600; margin-bottom: 10px;">最终可见机构树</div>
          <div v-if="visibleOrgTreeData.length" style="border:1px solid rgba(15,23,42,0.08); border-radius: 14px; padding: 10px; background:#fbfdff;">
            <el-tree
              :data="visibleOrgTreeData"
              node-key="orgId"
              default-expand-all
              :props="{ label: 'orgName', children: 'children' }"
            >
              <span slot-scope="{ data }">
                {{ data.orgName }}
                <span class="muted-text" style="font-size:12px;">({{ data.orgId }})</span>
              </span>
            </el-tree>
          </div>
          <div v-else class="muted-text">暂无可见机构</div>
          <div style="margin-top: 16px; font-weight: 600;">SQL 预览</div>
          <div class="sql-preview">{{ scope.sqlPreview || '暂无' }}</div>
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
import state from '../store/appState';

function buildVisibleOrgTree(orgs, visibleOrgIds) {
  const visibleSet = new Set(visibleOrgIds || []);
  const sourceMap = new Map((orgs || []).map(org => [org.orgId, { ...org, children: [] }]));
  const keepSet = new Set();

  sourceMap.forEach(org => {
    if (visibleSet.has(org.orgId)) {
      let current = org;
      while (current) {
        keepSet.add(current.orgId);
        current = current.parentOrgId ? sourceMap.get(current.parentOrgId) : null;
      }
    }
  });

  const keptMap = new Map();
  keepSet.forEach(orgId => {
    keptMap.set(orgId, { ...sourceMap.get(orgId), children: [] });
  });

  const roots = [];
  keptMap.forEach(node => {
    if (node.parentOrgId && keptMap.has(node.parentOrgId)) {
      keptMap.get(node.parentOrgId).children.push(node);
    } else {
      roots.push(node);
    }
  });

  return roots;
}

export default {
  name: 'DemoReportPage',
  data() {
    return {
      state,
      form: {
        customerName: '',
        metricName: ''
      },
      scope: {},
      rows: []
    };
  },
  computed: {
    visibleOrgTreeData() {
      return buildVisibleOrgTree(this.state.options.orgs, this.scope.visibleOrgIdsPreview);
    }
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
    },
    scopeTypeLabel(scopeType) {
      const labelMap = {
        CURRENT_ORG_ONLY: '仅当前机构',
        FIRST_DEPT_AND_DESCENDANTS: '所属一级部门下',
        OWNER_BANK_AND_DESCENDANTS: '所属行',
        CURRENT_ORG_AND_DESCENDANTS: '当前机构下'
      };
      return labelMap[scopeType] || (scopeType || '个人覆盖根节点');
    },
    formatResolvedOrg(row) {
      if (!row) {
        return '-';
      }
      if (!row.resolvedOrgName) {
        return '-';
      }
      return `${row.resolvedOrgName} (${row.resolvedOrgId})`;
    },
    matchedLevelLabel(row) {
      if (row.ruleSource === 'USER_OVERRIDE') {
        return `个人覆盖 / ${this.levelLabel(row.matchedUserOrgLevel)}`;
      }
      return this.levelLabel(row.matchedUserOrgLevel);
    },
    levelLabel(level) {
      const labelMap = {
        HEAD_OFFICE: '总行规则',
        BRANCH: '分行规则',
        SUB_BRANCH: '支行规则'
      };
      return labelMap[level] || (level || '-');
    }
  }
};
</script>
