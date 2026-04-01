<template>
  <div class="page-shell">
    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="filters.pageCode" clearable placeholder="页面">
          <el-option v-for="page in state.options.pageConfigs" :key="page.pageCode" :label="page.pageName" :value="page.pageCode" />
        </el-select>
        <el-select v-model="filters.roleCode" clearable placeholder="角色">
          <el-option v-for="role in state.options.roles" :key="role.roleCode" :label="role.roleName" :value="role.roleCode" />
        </el-select>
        <el-select v-model="filters.userOrgLevel" clearable placeholder="机构层级">
          <el-option v-for="level in state.options.userOrgLevels" :key="level" :label="level" :value="level" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="success" @click="openCreate">新增规则</el-button>
      </div>
    </div>

    <div class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="pageCode" label="pageCode" min-width="180" />
        <el-table-column prop="roleCode" label="角色" min-width="140" />
        <el-table-column prop="userOrgLevel" label="机构层级" min-width="140" />
        <el-table-column prop="jobKeyword" label="职位关键词" min-width="140" />
        <el-table-column prop="scopeType" label="范围类型" min-width="180" />
        <el-table-column label="启用" width="90">
          <template slot-scope="{ row }">
            <el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="dialogVisible" :title="editing ? '编辑默认规则' : '新增默认规则'" width="560px">
      <el-form :model="form" label-width="120px">
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
        <el-form-item label="机构层级">
          <el-select v-model="form.userOrgLevel" style="width:100%;">
            <el-option v-for="level in state.options.userOrgLevels" :key="level" :label="level" :value="level" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位关键词">
          <el-input v-model="form.jobKeyword" clearable placeholder="可留空" />
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
  </div>
</template>

<script>
import { fetchDefaultRules, saveDefaultRule, updateDefaultRule } from '../api';
import state from '../store/appState';

const createEmptyForm = () => ({
  id: null,
  pageCode: 'DEMO_REPORT_QUERY',
  roleCode: '',
  userOrgLevel: '',
  jobKeyword: '',
  scopeType: 'SELF_ONLY',
  enabled: true
});

export default {
  name: 'DefaultRulePage',
  data() {
    return {
      state,
      filters: {
        pageCode: '',
        roleCode: '',
        userOrgLevel: ''
      },
      rows: [],
      dialogVisible: false,
      editing: false,
      form: createEmptyForm()
    };
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      const { data } = await fetchDefaultRules(this.filters);
      this.rows = data;
    },
    resetFilters() {
      this.filters = { pageCode: '', roleCode: '', userOrgLevel: '' };
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
        await updateDefaultRule(this.form.id, payload);
      } else {
        await saveDefaultRule(payload);
      }
      await this.loadData();
      this.dialogVisible = false;
      this.$message.success('默认规则已保存');
    }
  }
};
</script>
