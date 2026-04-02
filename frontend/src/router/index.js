import Vue from 'vue';
import Router from 'vue-router';
import DemoReportPage from '../views/DemoReportPage.vue';
import PageScopeWorkbenchPage from '../views/PageScopeWorkbenchPage.vue';

Vue.use(Router);

export default new Router({
  routes: [
    { path: '/', redirect: '/demo/report' },
    { path: '/demo/report', component: DemoReportPage, meta: { title: '演示查询页' } },
    { path: '/configs/workbench', component: PageScopeWorkbenchPage, meta: { title: '规则配置台' } }
  ]
});
