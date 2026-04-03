<template>
  <div class="page-shell">
    <div class="page-card" style="padding-bottom: 12px;">
      <div class="workbench-header">
        <div>
          <div style="font-size: 20px; font-weight: 600;">按页面配置数据范围</div>
          <div class="muted-text" style="margin-top: 8px; max-width: 760px;">
            先选页面，再点角色，直接编辑这个角色在总行、分行、支行三类用户下默认能看什么范围。个人覆盖则直接在机构树上勾选可见范围，勾选父机构会自动包含下级。
          </div>
        </div>
        <div class="toolbar">
          <el-button type="primary" @click="openPageDialog()">新增页面</el-button>
          <el-button @click="reloadAll">刷新</el-button>
        </div>
      </div>
    </div>

    <div class="workbench-layout">
      <div v-if="selectedPage" class="workbench-content">
        <div class="summary-grid">
          <div class="page-card summary-card">
            <div class="section-header">
              <div>
                <div style="font-size: 18px; font-weight: 600;">当前页面：{{ selectedPage.pageName }}</div>
                <div class="muted-text" style="margin-top: 8px;">
                  pageCode: {{ selectedPage.pageCode }}。只有启用后，进入这个页面的查询请求才会套用数据范围。
                </div>
              </div>
              <div class="toolbar">
                <span class="code-chip">已配置角色 {{ currentPageStats.configuredRoleCount }}</span>
                <span class="code-chip">个人覆盖 {{ currentPageStats.overrideCount }}</span>
                <el-switch
                  v-model="selectedPageForm.enabled"
                  active-text="启用数据范围"
                  inactive-text="不启用"
                  @change="savePageEnabled"
                />
                <el-button @click="openPageDialog(selectedPage)">编辑页面信息</el-button>
                <el-button type="primary" plain @click="pagePickerVisible = true">切换页面</el-button>
              </div>
            </div>
          </div>

          <div v-if="selectedRole" class="page-card summary-card role-summary-card">
            <div class="section-header">
              <div>
                <div style="font-size: 18px; font-weight: 600;">当前配置角色：{{ currentRoleMeta.roleName }}</div>
                <div class="muted-text" style="margin-top: 8px;">
                  规则编辑区已经切到这个角色。需要换角色时，再打开弹窗切换，避免主区域一直被角色列表挤占。
                </div>
              </div>
              <div class="toolbar">
                <span class="code-chip">{{ currentRoleMeta.roleCode }}</span>
                <span class="code-chip">默认 {{ currentRoleStats.baseRuleCount }}</span>
                <span class="code-chip">关键词 {{ currentRoleStats.keywordRuleCount }}</span>
                <span class="code-chip">个人覆盖 {{ currentRoleStats.overrideCount }}</span>
                <el-button type="primary" plain @click="rolePickerVisible = true">切换角色</el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="selectedRole" class="page-card">
          <div class="section-header">
            <div>
              <div style="font-size: 18px; font-weight: 600;">{{ currentRoleMeta.roleName }} 的默认查看范围</div>
              <div class="muted-text" style="margin-top: 8px;">
                这里只配“常规情况”。每一行分别代表总行用户、分行用户、支行用户访问该页面时的默认可见范围。
              </div>
            </div>
          </div>

          <div class="scope-help-grid" style="margin-top: 16px;">
            <div v-for="scopeType in state.options.scopeTypes" :key="scopeType" class="scope-help-card">
              <div style="font-weight: 600;">{{ scopeTypeLabel(scopeType) }}</div>
              <div class="muted-text" style="margin-top: 6px; line-height: 1.6;">{{ scopeTypeDescription(scopeType) }}</div>
            </div>
          </div>

          <div class="table-shell">
            <el-table :data="baseRuleRows" border style="margin-top: 16px;" class="compact-table">
            <el-table-column prop="levelLabel" label="用户机构层级" min-width="140" />
            <el-table-column label="适用说明" min-width="220">
              <template slot-scope="{ row }">{{ row.description }}</template>
            </el-table-column>
            <el-table-column label="默认查看范围" min-width="260">
              <template slot-scope="{ row }">
                <el-select v-model="row.scopeType" style="width:100%;">
                  <el-option v-for="scopeType in state.options.scopeTypes" :key="scopeType" :label="scopeTypeLabel(scopeType)" :value="scopeType" />
                </el-select>
                <div class="muted-text" style="margin-top: 8px; line-height: 1.6;">
                  {{ scopeTypeDescription(row.scopeType) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="启用" width="100">
              <template slot-scope="{ row }"><el-switch v-model="row.enabled" /></template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template slot-scope="{ row }">
                <el-tag size="small" :type="row.id ? 'success' : 'info'">{{ row.id ? '已存在' : '未配置' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="190">
              <template slot-scope="{ row }">
                <el-button type="primary" size="mini" @click="saveBaseRule(row)">保存</el-button>
                <el-button
                  v-if="row.id"
                  type="danger"
                  plain
                  size="mini"
                  @click="deleteBaseRule(row)"
                >
                  清空
                </el-button>
              </template>
            </el-table-column>
            </el-table>
          </div>
        </div>

        <div v-if="selectedRole" class="page-card">
          <div class="section-header">
            <div>
              <div style="font-size: 18px; font-weight: 600;">职位关键词例外</div>
              <div class="muted-text" style="margin-top: 8px;">
                这里单独管理特殊岗位例外。只有当用户职位名称包含关键词时，才会优先命中这里的规则。
              </div>
            </div>
            <el-button type="primary" plain @click="openKeywordDialog()">新增关键词例外</el-button>
          </div>

          <div class="table-shell">
            <el-table :data="keywordRuleRows" border style="margin-top: 16px;" class="compact-table" empty-text="当前角色暂无职位关键词例外规则">
            <el-table-column prop="userOrgLevel" label="机构层级" min-width="120" />
            <el-table-column prop="jobKeyword" label="职位关键词" min-width="160" />
            <el-table-column label="查看范围" min-width="220">
              <template slot-scope="{ row }">{{ scopeTypeLabel(row.scopeType) }}</template>
            </el-table-column>
            <el-table-column label="启用" width="90">
              <template slot-scope="{ row }">
                <el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template slot-scope="{ row }">
                <el-button type="text" @click="openKeywordDialog(row)">编辑</el-button>
                <el-button type="text" style="color:#dc2626;" @click="deleteKeywordRule(row)">删除</el-button>
              </template>
            </el-table-column>
            </el-table>
          </div>
        </div>

        <div v-if="selectedRole" class="page-card">
          <div class="section-header">
            <div>
              <div style="font-size: 18px; font-weight: 600;">个人覆盖</div>
              <div class="muted-text" style="margin-top: 8px;">
                这里给少量特殊用户单独开范围。请选择一个机构根节点，系统会按这个节点整棵子树授权。
              </div>
            </div>
            <el-button type="primary" plain @click="openOverrideDialog()">新增个人覆盖</el-button>
          </div>

          <div class="table-shell">
            <el-table :data="overrideRows" border style="margin-top: 16px;" class="compact-table" empty-text="当前页面/角色下暂无个人覆盖">
            <el-table-column prop="userId" label="用户号" width="110" />
            <el-table-column label="用户" min-width="160">
              <template slot-scope="{ row }">{{ userNameMap[row.userId] || row.userId }}</template>
            </el-table-column>
            <el-table-column label="所在机构" min-width="180">
              <template slot-scope="{ row }">{{ userOrgMap[row.userId] || '-' }}</template>
            </el-table-column>
            <el-table-column label="个人覆盖根节点" min-width="280">
              <template slot-scope="{ row }">
                <span v-if="row.customOrgIds && row.customOrgIds.length">
                  {{ formatOrgNames(row.customOrgIds) }}
                </span>
                <span v-else>{{ scopeTypeLabel(row.scopeType) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="启用" width="90">
              <template slot-scope="{ row }">
                <el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template slot-scope="{ row }">
                <el-button type="text" @click="openOverrideDialog(row)">编辑</el-button>
                <el-button type="text" @click="openPreview(row.userId)">试算</el-button>
                <el-button type="text" style="color:#dc2626;" @click="deleteOverrideRule(row)">删除</el-button>
              </template>
            </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <el-dialog :visible.sync="pageDialogVisible" :title="pageDialogForm.pageCode ? '编辑页面' : '新增页面'" width="520px" class="workbench-dialog page-dialog">
      <el-form :model="pageDialogForm" label-width="110px">
        <el-form-item label="pageCode">
          <el-input v-model="pageDialogForm.pageCode" :disabled="Boolean(pageDialogForm.originalPageCode)" />
        </el-form-item>
        <el-form-item label="页面名称">
          <el-input v-model="pageDialogForm.pageName" />
        </el-form-item>
        <el-form-item label="启用控制">
          <el-switch v-model="pageDialogForm.enabled" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="pageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePageDialog">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="pagePickerVisible" title="选择要配置的页面" width="900px" class="workbench-dialog page-picker-dialog">
      <div class="role-picker-header">
        <div>
          <div style="font-size: 18px; font-weight: 600;">页面选择</div>
          <div class="muted-text" style="margin-top: 6px;">
            先切到需要配置的页面，再在主界面继续编辑角色和规则。
          </div>
        </div>
        <div class="muted-text">共 {{ pageSummaries.length }} 个页面，当前显示 {{ filteredPages.length }} 个。</div>
      </div>
      <el-input
        v-model.trim="pageKeyword"
        clearable
        placeholder="搜索页面名称或 pageCode"
        prefix-icon="el-icon-search"
        style="margin-top: 16px;"
      />
      <div class="page-picker-grid">
        <div
          v-for="page in filteredPages"
          :key="page.pageCode"
          :style="pageCardStyle(page)"
          class="page-picker-card"
          @click="selectPage(page.pageCode, true)"
        >
          <div style="display:flex; justify-content:space-between; gap: 12px; align-items:flex-start;">
            <div style="min-width: 0;">
              <div style="font-weight: 600;">{{ page.pageName }}</div>
              <div class="muted-text" style="font-size: 12px; margin-top: 6px;">{{ page.pageCode }}</div>
            </div>
            <el-tag size="mini" :type="page.enabled ? 'success' : 'info'">{{ page.enabled ? '已启用' : '未启用' }}</el-tag>
          </div>
          <div class="role-summary">
            <span class="code-chip">已配置角色 {{ page.configuredRoleCount }}</span>
            <span class="code-chip">个人覆盖 {{ page.overrideCount }}</span>
          </div>
        </div>
      </div>
      <div v-if="!filteredPages.length" class="muted-text role-empty">
        没有匹配到页面，请换个关键词试试。
      </div>
      <div slot="footer">
        <el-button @click="pagePickerVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="rolePickerVisible" title="选择要配置的角色" width="860px" class="workbench-dialog role-picker-dialog">
      <div class="role-picker-header">
        <div>
          <div style="font-size: 18px; font-weight: 600;">{{ selectedPage ? selectedPage.pageName : '当前页面' }}</div>
          <div class="muted-text" style="margin-top: 6px;">
            在这里切换角色，主页面会把宽度尽量留给规则编辑区。
          </div>
        </div>
        <div class="muted-text">共 {{ roleCards.length }} 个角色，当前显示 {{ filteredRoles.length }} 个。</div>
      </div>
      <el-input
        v-model.trim="roleKeyword"
        clearable
        placeholder="搜索角色名称或编码"
        prefix-icon="el-icon-search"
        style="margin-top: 16px;"
      />
      <div class="role-picker-grid">
        <div
          v-for="role in filteredRoles"
          :key="role.roleCode"
          :style="roleCardStyle(role)"
          class="role-picker-card"
          @click="selectRole(role.roleCode, true)"
        >
          <div style="display:flex; justify-content:space-between; gap: 12px; align-items:flex-start;">
            <div style="min-width: 0;">
              <div style="font-weight: 600;">{{ role.roleName }}</div>
              <div class="muted-text" style="font-size: 12px; margin-top: 6px;">{{ role.roleCode }}</div>
            </div>
            <el-tag size="mini" :type="role.configured ? 'success' : 'warning'">
              {{ role.configured ? '已配置' : '待配置' }}
            </el-tag>
          </div>
          <div class="role-summary">
            <span class="code-chip">默认 {{ role.baseRuleCount }}</span>
            <span class="code-chip">关键词 {{ role.keywordRuleCount }}</span>
            <span class="code-chip">个人覆盖 {{ role.overrideCount }}</span>
          </div>
        </div>
      </div>
      <div v-if="!filteredRoles.length" class="muted-text role-empty">
        没有匹配到角色，请换个关键词试试。
      </div>
      <div slot="footer">
        <el-button @click="rolePickerVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="keywordDialogVisible" :title="keywordDialogForm.id ? '编辑职位关键词例外' : '新增职位关键词例外'" width="560px" class="workbench-dialog keyword-dialog">
      <el-form :model="keywordDialogForm" label-width="120px">
        <el-form-item label="机构层级">
          <el-select v-model="keywordDialogForm.userOrgLevel" style="width:100%;">
            <el-option v-for="level in state.options.userOrgLevels" :key="level" :label="levelLabel(level)" :value="level" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位关键词">
          <el-input v-model="keywordDialogForm.jobKeyword" placeholder="例如：审计、客户经理" />
        </el-form-item>
        <el-form-item label="查看范围">
          <el-select v-model="keywordDialogForm.scopeType" style="width:100%;">
            <el-option v-for="scopeType in state.options.scopeTypes" :key="scopeType" :label="scopeTypeLabel(scopeType)" :value="scopeType" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="keywordDialogForm.enabled" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="keywordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveKeywordRule">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="overrideDialogVisible" :title="overrideDialogForm.id ? '编辑个人覆盖' : '新增个人覆盖'" width="760px" class="workbench-dialog override-dialog">
      <el-form :model="overrideDialogForm" label-width="120px">
        <el-form-item label="用户">
          <el-select v-model="overrideDialogForm.userId" filterable style="width:100%;">
            <el-option
              v-for="user in state.options.users"
              :key="user.userId"
              :label="`${user.userName} (${user.userId}) / ${user.orgName}`"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="机构范围">
          <div class="muted-text" style="margin-bottom: 8px;">请选择一个机构根节点，系统会按这个节点整棵子树授权；切换选择时会自动替换成新的根节点。</div>
          <div style="border:1px solid rgba(15,23,42,0.08); border-radius: 14px; padding: 12px; max-height: 420px; overflow:auto;">
            <el-tree
              ref="overrideOrgTree"
              :data="orgTreeData"
              node-key="orgId"
              show-checkbox
              default-expand-all
              @check="handleOverrideTreeCheck"
              :props="{ label: 'orgName', children: 'children' }"
            >
              <span slot-scope="{ data }">
                {{ data.orgName }}
                <span class="muted-text" style="font-size:12px;">({{ data.orgId }})</span>
              </span>
            </el-tree>
          </div>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="overrideDialogForm.enabled" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="overrideDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveOverrideRule">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="previewVisible" title="个人覆盖试算结果" width="820px" class="workbench-dialog preview-dialog">
      <div v-if="preview.scopeMode">
        <p><strong>结果模式：</strong>{{ preview.scopeMode }}</p>
        <p><strong>说明：</strong>{{ preview.message }}</p>
        <p><strong>SQL：</strong></p>
        <div class="sql-preview">{{ preview.sqlPreview }}</div>
        <p style="margin-top: 12px;"><strong>可见机构树：</strong></p>
        <div v-if="previewOrgTreeData.length" style="border:1px solid rgba(15,23,42,0.08); border-radius: 14px; padding: 10px;">
          <el-tree
            :data="previewOrgTreeData"
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
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  deleteDefaultRule,
  deleteUserRule,
  fetchDefaultRules,
  fetchPageConfigs,
  fetchUserRules,
  resolvePreview,
  saveDefaultRule,
  savePageConfig,
  saveUserRule,
  updateDefaultRule,
  updatePageConfig,
  updateUserRule
} from '../api';
import state from '../store/appState';

const LEVEL_ROWS = [
  { userOrgLevel: 'HEAD_OFFICE', levelLabel: '总行用户', description: '登录用户所属层级为总行时，默认能看什么范围' },
  { userOrgLevel: 'BRANCH', levelLabel: '分行用户', description: '登录用户所属层级为分行时，默认能看什么范围' },
  { userOrgLevel: 'SUB_BRANCH', levelLabel: '支行用户', description: '登录用户所属层级为支行时，默认能看什么范围' }
];

function buildOrgTree(orgs, visibleOrgIds) {
  const sourceMap = new Map((orgs || []).map(org => [org.orgId, { ...org, children: [] }]));
  let keepSet = null;

  if (visibleOrgIds && visibleOrgIds.length) {
    keepSet = new Set();
    visibleOrgIds.forEach(orgId => {
      let current = sourceMap.get(orgId);
      while (current) {
        keepSet.add(current.orgId);
        current = current.parentOrgId ? sourceMap.get(current.parentOrgId) : null;
      }
    });
  }

  const nodeMap = new Map();
  sourceMap.forEach((org, orgId) => {
    if (!keepSet || keepSet.has(orgId)) {
      nodeMap.set(orgId, { ...org, children: [] });
    }
  });

  const roots = [];
  nodeMap.forEach(node => {
    if (node.parentOrgId && nodeMap.has(node.parentOrgId)) {
      nodeMap.get(node.parentOrgId).children.push(node);
    } else {
      roots.push(node);
    }
  });
  return roots;
}

function createPageDialogForm(page) {
  return {
    originalPageCode: page ? page.pageCode : '',
    pageCode: page ? page.pageCode : '',
    pageName: page ? page.pageName : '',
    enabled: page ? page.enabled : true
  };
}

function createKeywordDialogForm(pageCode, roleCode, row) {
  return {
    id: row ? row.id : null,
    pageCode,
    roleCode,
    userOrgLevel: row ? row.userOrgLevel : 'HEAD_OFFICE',
    jobKeyword: row ? row.jobKeyword : '',
    scopeType: row ? row.scopeType : 'CURRENT_ORG_ONLY',
    enabled: row ? row.enabled : true
  };
}

function createOverrideDialogForm(pageCode, roleCode, row) {
  return {
    id: row ? row.id : null,
    userId: row ? row.userId : state.currentUserId,
    pageCode,
    roleCode,
    customOrgIds: row && row.customOrgIds ? [...row.customOrgIds] : [],
    enabled: row ? row.enabled : true
  };
}

export default {
  name: 'PageScopeWorkbenchPage',
  data() {
    return {
      state,
      pages: [],
      defaultRules: [],
      userRules: [],
      pageKeyword: '',
      pagePickerVisible: false,
      roleKeyword: '',
      rolePickerVisible: false,
      selectedPageCode: '',
      selectedRole: '',
      selectedPageForm: { enabled: false },
      pageDialogVisible: false,
      pageDialogForm: createPageDialogForm(),
      keywordDialogVisible: false,
      keywordDialogForm: createKeywordDialogForm('', ''),
      overrideDialogVisible: false,
      overrideDialogForm: createOverrideDialogForm('', ''),
      overrideSelectedRootId: '',
      applyingOverrideTreeSelection: false,
      previewVisible: false,
      preview: {}
    };
  },
  computed: {
    selectedPage() {
      return this.pages.find(page => page.pageCode === this.selectedPageCode) || null;
    },
    orgTreeData() {
      return buildOrgTree(this.state.options.orgs);
    },
    previewOrgTreeData() {
      return buildOrgTree(this.state.options.orgs, this.preview.visibleOrgIdsPreview);
    },
    pageSummaries() {
      return this.pages.map(page => {
        const pageRules = this.defaultRules.filter(rule => rule.pageCode === page.pageCode);
        const pageOverrides = this.userRules.filter(rule => rule.pageCode === page.pageCode);
        const configuredRoleCodes = new Set(pageRules.map(rule => rule.roleCode));
        return {
          ...page,
          configuredRoleCount: configuredRoleCodes.size,
          overrideCount: pageOverrides.length
        };
      });
    },
    filteredPages() {
      const keyword = (this.pageKeyword || '').toLowerCase();
      if (!keyword) {
        return this.pageSummaries;
      }
      return this.pageSummaries.filter(page =>
        (page.pageName || '').toLowerCase().indexOf(keyword) >= 0 ||
        (page.pageCode || '').toLowerCase().indexOf(keyword) >= 0
      );
    },
    roleCards() {
      return this.state.options.roles.map(role => {
        const pageRules = this.defaultRules.filter(rule => rule.pageCode === this.selectedPageCode && rule.roleCode === role.roleCode);
        const overrides = this.userRules.filter(rule => rule.pageCode === this.selectedPageCode && rule.roleCode === role.roleCode);
        const baseRuleCount = pageRules.filter(rule => !rule.jobKeyword).length;
        const keywordRuleCount = pageRules.filter(rule => rule.jobKeyword).length;
        return {
          ...role,
          configured: baseRuleCount > 0 || keywordRuleCount > 0 || overrides.length > 0,
          baseRuleCount,
          keywordRuleCount,
          overrideCount: overrides.length
        };
      });
    },
    filteredRoles() {
      const keyword = (this.roleKeyword || '').toLowerCase();
      if (!keyword) {
        return this.roleCards;
      }
      return this.roleCards.filter(role =>
        (role.roleName || '').toLowerCase().indexOf(keyword) >= 0 ||
        (role.roleCode || '').toLowerCase().indexOf(keyword) >= 0
      );
    },
    currentRoleMeta() {
      return this.state.options.roles.find(role => role.roleCode === this.selectedRole) || { roleName: this.selectedRole };
    },
    currentRoleStats() {
      return this.roleCards.find(role => role.roleCode === this.selectedRole) || {
        roleCode: this.selectedRole,
        baseRuleCount: 0,
        keywordRuleCount: 0,
        overrideCount: 0
      };
    },
    currentPageStats() {
      return this.pageSummaries.find(page => page.pageCode === this.selectedPageCode) || {
        pageCode: this.selectedPageCode,
        configuredRoleCount: 0,
        overrideCount: 0
      };
    },
    baseRuleRows() {
      return LEVEL_ROWS.map(levelRow => {
        const rule = this.defaultRules.find(item =>
          item.pageCode === this.selectedPageCode &&
          item.roleCode === this.selectedRole &&
          item.userOrgLevel === levelRow.userOrgLevel &&
          !item.jobKeyword
        );
        return {
          ...levelRow,
          id: rule ? rule.id : null,
          pageCode: this.selectedPageCode,
          roleCode: this.selectedRole,
          scopeType: rule ? rule.scopeType : 'CURRENT_ORG_ONLY',
          enabled: rule ? rule.enabled : true,
          jobKeyword: ''
        };
      });
    },
    keywordRuleRows() {
      return this.defaultRules.filter(rule =>
        rule.pageCode === this.selectedPageCode &&
        rule.roleCode === this.selectedRole &&
        rule.jobKeyword
      );
    },
    overrideRows() {
      return this.userRules.filter(rule =>
        rule.pageCode === this.selectedPageCode &&
        rule.roleCode === this.selectedRole
      );
    },
    userNameMap() {
      return this.state.options.users.reduce((acc, user) => {
        acc[user.userId] = user.userName;
        return acc;
      }, {});
    },
    userOrgMap() {
      return this.state.options.users.reduce((acc, user) => {
        acc[user.userId] = user.orgName;
        return acc;
      }, {});
    }
  },
  watch: {
    filteredPages(nextPages) {
      if (!nextPages.length) {
        return;
      }
      const stillVisible = nextPages.some(page => page.pageCode === this.selectedPageCode);
      if (!stillVisible) {
        this.selectPage(nextPages[0].pageCode);
      }
    },
    filteredRoles(nextRoles) {
      if (!nextRoles.length) {
        return;
      }
      const stillVisible = nextRoles.some(role => role.roleCode === this.selectedRole);
      if (!stillVisible) {
        this.selectedRole = nextRoles[0].roleCode;
      }
    }
  },
  created() {
    this.reloadAll();
    this.$root.$on('demo-user-changed', this.reloadAll);
  },
  beforeDestroy() {
    this.$root.$off('demo-user-changed', this.reloadAll);
  },
  methods: {
    async reloadAll() {
      await Promise.all([this.loadPages(), this.loadDefaultRules(), this.loadUserRules()]);
      this.ensureSelection();
    },
    async loadPages() {
      const { data } = await fetchPageConfigs();
      this.pages = data;
    },
    async loadDefaultRules() {
      const { data } = await fetchDefaultRules({});
      this.defaultRules = data;
    },
    async loadUserRules() {
      const { data } = await fetchUserRules({});
      this.userRules = data;
    },
    ensureSelection() {
      if (!this.selectedPageCode && this.pages.length) {
        this.selectedPageCode = this.pages[0].pageCode;
      }
      if (this.selectedPage) {
        this.selectedPageForm = { enabled: this.selectedPage.enabled };
      }
      if (!this.selectedRole && this.state.options.roles.length) {
        const configuredRole = this.roleCards.find(role => role.configured);
        this.selectedRole = configuredRole ? configuredRole.roleCode : this.state.options.roles[0].roleCode;
      }
    },
    selectPage(pageCode, closePicker) {
      this.selectedPageCode = pageCode;
      this.roleKeyword = '';
      this.selectedPageForm = { enabled: (this.pages.find(page => page.pageCode === pageCode) || {}).enabled };
      const configuredRole = this.roleCards.find(role => role.configured);
      this.selectedRole = configuredRole ? configuredRole.roleCode : (this.state.options.roles[0] || {}).roleCode || '';
      if (closePicker) {
        this.pagePickerVisible = false;
      }
    },
    selectRole(roleCode, closePicker) {
      this.selectedRole = roleCode;
      if (closePicker) {
        this.rolePickerVisible = false;
      }
    },
    pageCardStyle(page) {
      const active = page.pageCode === this.selectedPageCode;
      return {
        border: active ? '1px solid #3b82f6' : '1px solid rgba(15, 23, 42, 0.08)',
        borderRadius: '16px',
        padding: '14px',
        marginBottom: '12px',
        cursor: 'pointer',
        background: active ? 'linear-gradient(180deg, #eff6ff 0%, #ffffff 100%)' : '#fff',
        boxShadow: active ? '0 16px 30px rgba(59, 130, 246, 0.12)' : 'none'
      };
    },
    roleCardStyle(role) {
      const active = role.roleCode === this.selectedRole;
      return {
        border: active ? '1px solid #0f766e' : '1px solid rgba(15, 23, 42, 0.08)',
        borderRadius: '16px',
        padding: '14px',
        cursor: 'pointer',
        background: active ? 'linear-gradient(180deg, #ecfeff 0%, #ffffff 100%)' : '#fff',
        boxShadow: active ? '0 16px 30px rgba(13, 148, 136, 0.12)' : 'none'
      };
    },
    levelLabel(level) {
      const matched = LEVEL_ROWS.find(item => item.userOrgLevel === level);
      return matched ? matched.levelLabel : level;
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
    scopeTypeDescription(scopeType) {
      const descriptionMap = {
        CURRENT_ORG_ONLY: '只看当前机构本身，SQL 形态是 r.org_id = 当前机构id。',
        FIRST_DEPT_AND_DESCENDANTS: '先找到所属行，再取路径里所属行后面的下一个节点作为一级部门；如果后面没有节点，就回退为所属行。',
        OWNER_BANK_AND_DESCENDANTS: '总行取总行，分行取路径里最后一个分行节点，支行取路径里最后一个支行节点，然后看这棵机构树。',
        CURRENT_ORG_AND_DESCENDANTS: '直接以当前机构为根节点，看当前机构整棵子树。'
      };
      return descriptionMap[scopeType] || '个人覆盖可直接勾选机构树，自定义看到哪些机构。';
    },
    formatOrgNames(orgIds) {
      const nameMap = this.state.options.orgs.reduce((acc, org) => {
        acc[org.orgId] = org.orgName;
        return acc;
      }, {});
      return (orgIds || []).map(orgId => nameMap[orgId] || orgId).join(' / ');
    },
    openPageDialog(page) {
      this.pageDialogForm = createPageDialogForm(page);
      this.pageDialogVisible = true;
    },
    async savePageDialog() {
      const payload = {
        pageCode: this.pageDialogForm.pageCode,
        pageName: this.pageDialogForm.pageName,
        enabled: this.pageDialogForm.enabled
      };
      if (this.pageDialogForm.originalPageCode) {
        await updatePageConfig(this.pageDialogForm.originalPageCode, payload);
      } else {
        await savePageConfig(payload);
      }
      await this.loadPages();
      this.selectPage(payload.pageCode);
      this.pageDialogVisible = false;
      this.$message.success('页面信息已保存');
    },
    async savePageEnabled() {
      if (!this.selectedPage) {
        return;
      }
      await updatePageConfig(this.selectedPage.pageCode, {
        pageCode: this.selectedPage.pageCode,
        pageName: this.selectedPage.pageName,
        enabled: this.selectedPageForm.enabled
      });
      await this.loadPages();
      this.ensureSelection();
      this.$message.success('页面启用状态已更新');
    },
    async saveBaseRule(row) {
      const payload = {
        pageCode: this.selectedPageCode,
        roleCode: this.selectedRole,
        userOrgLevel: row.userOrgLevel,
        jobKeyword: '',
        scopeType: row.scopeType,
        enabled: row.enabled
      };
      if (row.id) {
        await updateDefaultRule(row.id, payload);
      } else {
        await saveDefaultRule(payload);
      }
      await this.loadDefaultRules();
      this.$message.success(`${row.levelLabel}默认规则已保存`);
    },
    async deleteBaseRule(row) {
      try {
        await this.$confirm(`确认清空“${row.levelLabel}”这条默认规则吗？清空后状态会恢复为未配置。`, '清空确认', {
          type: 'warning'
        });
        await deleteDefaultRule(row.id);
        await this.loadDefaultRules();
        this.$message.success(`${row.levelLabel}默认规则已清空`);
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          throw error;
        }
      }
    },
    openKeywordDialog(row) {
      this.keywordDialogForm = createKeywordDialogForm(this.selectedPageCode, this.selectedRole, row);
      this.keywordDialogVisible = true;
    },
    async saveKeywordRule() {
      const payload = {
        pageCode: this.selectedPageCode,
        roleCode: this.selectedRole,
        userOrgLevel: this.keywordDialogForm.userOrgLevel,
        jobKeyword: this.keywordDialogForm.jobKeyword,
        scopeType: this.keywordDialogForm.scopeType,
        enabled: this.keywordDialogForm.enabled
      };
      if (this.keywordDialogForm.id) {
        await updateDefaultRule(this.keywordDialogForm.id, payload);
      } else {
        await saveDefaultRule(payload);
      }
      await this.loadDefaultRules();
      this.keywordDialogVisible = false;
      this.$message.success('职位关键词例外规则已保存');
    },
    async deleteKeywordRule(row) {
      try {
        await this.$confirm(`确认删除职位关键词规则“${row.jobKeyword}”吗？`, '删除确认', {
          type: 'warning'
        });
        await deleteDefaultRule(row.id);
        await this.loadDefaultRules();
        this.$message.success('职位关键词例外规则已删除');
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          throw error;
        }
      }
    },
    openOverrideDialog(row) {
      this.overrideDialogForm = createOverrideDialogForm(this.selectedPageCode, this.selectedRole, row);
      if (!row && this.state.currentUser && this.state.currentUser.userId) {
        this.overrideDialogForm.userId = this.state.currentUser.userId;
      }
      this.overrideSelectedRootId = this.overrideDialogForm.customOrgIds[0] || '';
      this.overrideDialogVisible = true;
      this.$nextTick(() => {
        this.applyOverrideTreeSelection(this.overrideSelectedRootId);
      });
    },
    applyOverrideTreeSelection(rootOrgId) {
      if (!this.$refs.overrideOrgTree) {
        this.overrideSelectedRootId = rootOrgId || '';
        return;
      }
      this.applyingOverrideTreeSelection = true;
      this.overrideSelectedRootId = rootOrgId || '';
      this.$refs.overrideOrgTree.setCheckedKeys(rootOrgId ? [rootOrgId] : []);
      this.$nextTick(() => {
        this.applyingOverrideTreeSelection = false;
      });
    },
    handleOverrideTreeCheck(data, checkedState) {
      if (this.applyingOverrideTreeSelection) {
        return;
      }
      const checkedKeys = checkedState && checkedState.checkedKeys ? checkedState.checkedKeys : [];
      if (checkedKeys.indexOf(data.orgId) >= 0) {
        this.applyOverrideTreeSelection(data.orgId);
        return;
      }
      if (this.overrideSelectedRootId === data.orgId) {
        this.applyOverrideTreeSelection('');
      } else {
        this.applyOverrideTreeSelection(this.overrideSelectedRootId);
      }
    },
    async saveOverrideRule() {
      if (!this.overrideDialogForm.userId) {
        this.$message.warning('请选择用户');
        return;
      }
      const customOrgIds = this.overrideSelectedRootId ? [this.overrideSelectedRootId] : [];
      if (!customOrgIds.length) {
        this.$message.warning('请选择一个机构根节点');
        return;
      }
      const payload = {
        userId: this.overrideDialogForm.userId,
        pageCode: this.selectedPageCode,
        roleCode: this.selectedRole,
        customOrgIds,
        enabled: this.overrideDialogForm.enabled
      };
      if (this.overrideDialogForm.id) {
        await updateUserRule(this.overrideDialogForm.id, payload);
      } else {
        await saveUserRule(payload);
      }
      await this.loadUserRules();
      this.overrideDialogVisible = false;
      this.overrideSelectedRootId = '';
      this.$message.success('个人覆盖已保存');
    },
    async deleteOverrideRule(row) {
      try {
        await this.$confirm(`确认删除用户 ${this.userNameMap[row.userId] || row.userId} 的个人覆盖吗？`, '删除确认', {
          type: 'warning'
        });
        await deleteUserRule(row.id);
        await this.loadUserRules();
        this.$message.success('个人覆盖已删除');
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          throw error;
        }
      }
    },
    async openPreview(userId) {
      const { data } = await resolvePreview({
        userId,
        pageCode: this.selectedPageCode
      });
      this.preview = data;
      this.previewVisible = true;
    }
  }
};
</script>

<style scoped>
.workbench-header,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.workbench-layout {
  display: block;
}

.workbench-content {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.role-summary {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.summary-card {
  min-width: 0;
}

.role-summary-card {
  overflow: hidden;
}

.role-empty {
  padding: 20px 8px 8px;
}

.role-picker-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.role-picker-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 14px;
  max-height: min(62vh, 720px);
  overflow-y: auto;
  padding-right: 4px;
}

.role-picker-card {
  min-width: 0;
}

.page-picker-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 14px;
  max-height: min(62vh, 720px);
  overflow-y: auto;
  padding-right: 4px;
}

.page-picker-card {
  min-width: 0;
}

.scope-help-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.scope-help-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 14px;
  padding: 14px;
  background: #fbfdff;
}

.table-shell {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
}

.compact-table {
  width: 100%;
}

.compact-table ::v-deep .cell {
  white-space: normal;
  word-break: break-word;
}

.workbench-dialog ::v-deep .el-dialog {
  width: min(calc(100vw - 32px), var(--dialog-max-width, 760px)) !important;
  margin-top: 6vh !important;
}

.page-dialog {
  --dialog-max-width: 520px;
}

.keyword-dialog {
  --dialog-max-width: 560px;
}

.override-dialog {
  --dialog-max-width: 760px;
}

.preview-dialog {
  --dialog-max-width: 820px;
}

.role-picker-dialog {
  --dialog-max-width: 860px;
}

.page-picker-dialog {
  --dialog-max-width: 900px;
}

@media (max-width: 1240px) {
  .summary-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
