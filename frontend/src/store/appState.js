import Vue from 'vue';

const storedUserId = localStorage.getItem('demo-user-id') || 'U100';

const state = Vue.observable({
  currentUserId: storedUserId,
  currentUser: null,
  options: {
    users: [],
    roles: [],
    pageConfigs: [],
    scopeTypes: [],
    userOrgLevels: []
  }
});

export function setCurrentUserId(userId) {
  state.currentUserId = userId;
  localStorage.setItem('demo-user-id', userId);
}

export default state;
