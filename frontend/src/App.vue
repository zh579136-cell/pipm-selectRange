<template>
  <el-container style="min-height: 100vh;">
    <el-aside width="240px" style="background: linear-gradient(180deg, #16324f 0%, #0f172a 100%); color: #fff;">
      <div style="padding: 28px 20px 18px; border-bottom: 1px solid rgba(255,255,255,0.08);">
        <div style="font-size: 22px; font-weight: 600;">数据范围演示</div>
        <div style="margin-top: 8px; font-size: 13px; color: rgba(255,255,255,0.75);">
          银行查询页数据权限控制
        </div>
      </div>
      <el-menu
        :default-active="$route.path"
        background-color="transparent"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
        style="border-right: 0;"
        @select="goTo"
      >
        <el-menu-item index="/demo/report">演示查询页</el-menu-item>
        <el-menu-item index="/configs/workbench">规则配置台</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header height="110px" style="display:flex; align-items:center; justify-content:space-between; background:rgba(255,255,255,0.8); backdrop-filter: blur(12px); border-bottom: 1px solid rgba(15,23,42,0.06);">
        <div>
          <div style="font-size: 24px; font-weight: 600;">{{ pageTitle }}</div>
          <div class="muted-text" style="margin-top: 4px;">切换当前演示用户，即可观察同一页面下的数据范围变化。</div>
        </div>
        <div style="display:flex; align-items:center; gap: 12px;">
          <div v-if="state.currentUser" style="text-align:right; max-width: 620px;">
            <div style="font-size: 14px; font-weight: 600;">{{ state.currentUser.userName }}</div>
            <div class="muted-text" style="font-size: 12px;">{{ state.currentUser.orgName }} / {{ state.currentUser.positionName }}</div>
            <div class="muted-text" style="font-size: 12px; margin-top: 4px;">
              机构路径ID：{{ state.currentUser.orgPathId }}
            </div>
            <div style="margin-top: 6px;">
              <span
                v-for="role in currentUserRoles"
                :key="role.roleCode"
                class="code-chip"
                style="margin-bottom: 0;"
              >
                {{ role.roleName }} ({{ role.roleCode }})
              </span>
            </div>
          </div>
          <el-select
            v-model="localUserId"
            placeholder="切换演示用户"
            style="width: 280px;"
            @change="handleUserChange"
          >
            <el-option
              v-for="user in state.options.users"
              :key="user.userId"
              :label="`${user.userName} (${user.userId})`"
              :value="user.userId"
            />
          </el-select>
        </div>
      </el-header>
      <el-main style="padding: 0;">
        <router-view :refresh-key="refreshKey" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import state, { setCurrentUserId } from './store/appState';
import { fetchCurrentUser, fetchRuntimeOptions } from './api';

export default {
  name: 'App',
  data() {
    return {
      state,
      localUserId: state.currentUserId,
      refreshKey: 0
    };
  },
  computed: {
    pageTitle() {
      return this.$route.meta.title || '数据范围演示';
    },
    currentUserRoles() {
      if (!this.state.currentUser || !this.state.currentUser.roleCodes) {
        return [];
      }
      const roleMap = this.state.options.roles.reduce((acc, role) => {
        acc[role.roleCode] = role;
        return acc;
      }, {});
      return this.state.currentUser.roleCodes.map(roleCode => roleMap[roleCode] || { roleCode, roleName: roleCode });
    }
  },
  watch: {
    '$route.path'() {
      this.localUserId = this.state.currentUserId;
    }
  },
  created() {
    this.bootstrap();
  },
  methods: {
    async bootstrap() {
      await this.loadOptions();
      await this.loadCurrentUser();
    },
    async loadOptions() {
      const { data } = await fetchRuntimeOptions();
      this.state.options = data;
    },
    async loadCurrentUser() {
      const { data } = await fetchCurrentUser();
      this.state.currentUser = data;
      this.localUserId = this.state.currentUserId;
    },
    async handleUserChange(userId) {
      setCurrentUserId(userId);
      await this.loadCurrentUser();
      this.refreshKey += 1;
      this.$root.$emit('demo-user-changed');
    },
    goTo(path) {
      if (path !== this.$route.path) {
        this.$router.push(path);
      }
    }
  }
};
</script>
