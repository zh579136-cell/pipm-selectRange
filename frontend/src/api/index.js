import http from './http';

export function fetchRuntimeOptions() {
  return http.get('/runtime/options');
}

export function fetchCurrentUser() {
  return http.get('/runtime/current-user');
}

export function fetchDataScope(pageCode) {
  return http.get('/runtime/data-scope/resolve', { params: { pageCode } });
}

export function queryDemoReport(params) {
  return http.get('/demo/report/query', { params });
}

export function fetchPageConfigs() {
  return http.get('/data-scope/pages');
}

export function savePageConfig(payload) {
  return http.post('/data-scope/pages', payload);
}

export function updatePageConfig(pageCode, payload) {
  return http.put(`/data-scope/pages/${pageCode}`, payload);
}

export function fetchDefaultRules(params) {
  return http.get('/data-scope/default-rules', { params });
}

export function saveDefaultRule(payload) {
  return http.post('/data-scope/default-rules', payload);
}

export function updateDefaultRule(id, payload) {
  return http.put(`/data-scope/default-rules/${id}`, payload);
}

export function deleteDefaultRule(id) {
  return http.delete(`/data-scope/default-rules/${id}`);
}

export function fetchUserRules(params) {
  return http.get('/data-scope/user-rules', { params });
}

export function saveUserRule(payload) {
  return http.post('/data-scope/user-rules', payload);
}

export function updateUserRule(id, payload) {
  return http.put(`/data-scope/user-rules/${id}`, payload);
}

export function deleteUserRule(id) {
  return http.delete(`/data-scope/user-rules/${id}`);
}

export function resolvePreview(payload) {
  return http.post('/data-scope/resolve-preview', payload);
}
