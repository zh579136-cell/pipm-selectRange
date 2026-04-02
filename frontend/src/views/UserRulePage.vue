<template>
  <div class="page-shell">
    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="filters.userId" clearable placeholder="用户">
          <el-option v-for="user in state.options.users" :key="user.userId" :label="`${user.userName} (${user.userId})`" :value="user.userId" />
        </el-select>
        <el-select v-model="filters.pageCode" clearable placeholder="页面">
          <el-option v-for="page in state.options.pageConfigs" :key="page.pageCode" :label="page.pageName" :value="page.pageCode" />
        </el-select>
        <el-select v-model="filters.roleCode" clearable placeholder="角色">
          <el-option v-for="role in state.options.roles" :key="role.roleCode" :label="role.roleName" :value="role.roleCode" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="success" @click="openCreate">新增个人覆盖</el-button>
      </div>
    </div>

    <div class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户号" min-width="120" />
        <el-table-column prop="pageCode" label="pageCode" min-width="180" />
        <el-table-column prop="roleCode" label="角色" min-width="140" />
        <el-table-column prop="scopeType" label="范围类型" min-width="180" />
        <el-table-column label="启用" width="90">
          <template slot-scope="{ row }">
            <el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" @click="openPreview(row.userId, row.pageCode)">试算</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="dialogVisible" :title="editing ? '编辑个人覆盖规则' : '新增个人覆盖规则'" width="560px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="用户">
          <el-select v-model="form.userId" style="width:100%;">
            <el-option v-for="user in state.options.users" :key="user.userId" :label="`${user.userName} (${user.userId})`" :value="user.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="页面">
          <el-select v-model="form.pageCode" style="width:100%;">
            <el-option v-for="page in state.options.pageConfigs" :key="page.pageCode" :label="`${page.pageName} (${page.pageCode})`" :value="page.pageCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode" style="width:100%;">
            <el-option v-for="role in state.options.roles" :key="role.roleCode" :label="`${role.roleName} (${role.roleCode})`" :value="role.roleCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="范围类型">
          <el-select v-model="form.scopeType" style="width:100%;">
            <el-option v-for="scopeType in state.options.scopeTypes" :key="scopeType" :label="scopeType" :value="scopeType" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="previewVisible" title="规则试算结果" width="760px">
      <div v-if="preview.scopeMode">
        <p><strong>结果模式：</strong>{{ preview.scopeMode }}</p>
        <p><strong>说明：</strong>{{ preview.message }}</p>
        <p><strong>SQL：</strong></p>
        <div class="sql-preview">{{ preview.sqlPreview }}</div>
        <p style="margin-top: 12px;"><strong>可见机构：</strong></p>
        <div>
          <span v-for="orgId in preview.visibleOrgIdsPreview || []" :key="orgId" class="code-chip">{{ orgId }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { fetchUserRules, saveUserRule, updateUserRule, resolvePreview } from '../api';
import state from '../store/appState';

const createEmptyForm = () => ({
  id: null,
  userId: '',
  pageCode: 'DEMO_REPORT_QUERY',
  roleCode: '',
  scopeType: 'CURRENT_ORG_ONLY',
  enabled: true
});

export default {
  name: 'UserRulePage',
  data() {
    return {
      state,
      filters: {
        userId: '',
        pageCode: '',
        roleCode: ''
      },
      rows: [],
      dialogVisible: false,
      previewVisible: false,
      editing: false,
      form: createEmptyForm(),
      preview: {}
    };
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      const { data } = await fetchUserRules(this.filters);
      this.rows = data;
    },
    resetFilters() {
      this.filters = { userId: '', pageCode: '', roleCode: '' };
      this.loadData();
    },
    openCreate() {
      this.editing = false;
      this.form = createEmptyForm();
      this.dialogVisible = true;
    },
    openEdit(row) {
      this.editing = true;
      this.form = { ...row };
      this.dialogVisible = true;
    },
    async submit() {
      const payload = { ...this.form };
      if (this.editing) {
        await updateUserRule(this.form.id, payload);
      } else {
        await saveUserRule(payload);
      }
      await this.loadData();
      this.dialogVisible = false;
      this.$message.success('个人覆盖规则已保存');
    },
    async openPreview(userId, pageCode) {
      const { data } = await resolvePreview({ userId, pageCode });
      this.preview = data;
      this.previewVisible = true;
    }
  }
};
</script>
