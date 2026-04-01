import Vue from 'vue';
import Router from 'vue-router';
import DemoReportPage from '../views/DemoReportPage.vue';
import PageConfigPage from '../views/PageConfigPage.vue';
import DefaultRulePage from '../views/DefaultRulePage.vue';
import UserRulePage from '../views/UserRulePage.vue';

Vue.use(Router);

export default new Router({
  routes: [
    { path: '/', redirect: '/demo/report' },
    { path: '/demo/report', component: DemoReportPage, meta: { title: '演示查询页' } },
    { path: '/configs/pages', component: PageConfigPage, meta: { title: '页面白名单管理' } },
    { path: '/configs/default-rules', component: DefaultRulePage, meta: { title: '默认规则管理' } },
    { path: '/configs/user-rules', component: UserRulePage, meta: { title: '个人覆盖管理' } }
  ]
});
