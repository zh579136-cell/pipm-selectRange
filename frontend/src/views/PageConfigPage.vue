<template>
  <div class="page-shell">
    <div class="page-card">
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div>
          <div style="font-size: 18px; font-weight: 600;">页面白名单管理</div>
          <div class="muted-text" style="margin-top: 6px;">只有登记并启用的页面才会触发数据范围控制。</div>
        </div>
        <el-button type="primary" @click="openCreate">新增页面</el-button>
      </div>
    </div>

    <div class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="pageCode" label="pageCode" min-width="180" />
        <el-table-column prop="pageName" label="页面名称" min-width="180" />
        <el-table-column label="是否启用" min-width="120">
          <template slot-scope="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '未启用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="dialogVisible" :title="editing ? '编辑页面' : '新增页面'" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="pageCode">
          <el-input v-model="form.pageCode" :disabled="editing" />
        </el-form-item>
        <el-form-item label="页面名称">
          <el-input v-model="form.pageName" />
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
import { fetchPageConfigs, savePageConfig, updatePageConfig, fetchRuntimeOptions } from '../api';
import state from '../store/appState';

export default {
  name: 'PageConfigPage',
  data() {
    return {
      state,
      rows: [],
      dialogVisible: false,
      editing: false,
      form: {
        pageCode: '',
        pageName: '',
        enabled: true
      }
    };
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      const { data } = await fetchPageConfigs();
      this.rows = data;
    },
    openCreate() {
      this.editing = false;
      this.form = { pageCode: '', pageName: '', enabled: true };
      this.dialogVisible = true;
    },
    openEdit(row) {
      this.editing = true;
      this.form = { ...row };
      this.dialogVisible = true;
    },
    async submit() {
      if (this.editing) {
        await updatePageConfig(this.form.pageCode, this.form);
      } else {
        await savePageConfig(this.form);
      }
      await this.loadData();
      const { data } = await fetchRuntimeOptions();
      this.state.options = data;
      this.dialogVisible = false;
      this.$message.success('页面配置已保存');
    }
  }
};
</script>
