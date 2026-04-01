package com.example.datascope.model;

import java.util.ArrayList;
import java.util.List;

public class RuntimeOptions {

    private List<DemoUser> users = new ArrayList<DemoUser>();
    private List<RoleInfo> roles = new ArrayList<RoleInfo>();
    private List<PageConfig> pageConfigs = new ArrayList<PageConfig>();
    private List<String> scopeTypes = new ArrayList<String>();
    private List<String> userOrgLevels = new ArrayList<String>();

    public List<DemoUser> getUsers() {
        return users;
    }

    public void setUsers(List<DemoUser> users) {
        this.users = users;
    }

    public List<RoleInfo> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleInfo> roles) {
        this.roles = roles;
    }

    public List<PageConfig> getPageConfigs() {
        return pageConfigs;
    }

    public void setPageConfigs(List<PageConfig> pageConfigs) {
        this.pageConfigs = pageConfigs;
    }

    public List<String> getScopeTypes() {
        return scopeTypes;
    }

    public void setScopeTypes(List<String> scopeTypes) {
        this.scopeTypes = scopeTypes;
    }

    public List<String> getUserOrgLevels() {
        return userOrgLevels;
    }

    public void setUserOrgLevels(List<String> userOrgLevels) {
        this.userOrgLevels = userOrgLevels;
    }
}
