package com.example.datascope.model;

import com.example.datascope.domain.ScopeType;
import com.example.datascope.domain.UserOrgLevel;

public class DefaultRule {

    private Long id;
    private String pageCode;
    private String roleCode;
    private UserOrgLevel userOrgLevel;
    private String jobKeyword;
    private ScopeType scopeType;
    private boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPageCode() {
        return pageCode;
    }

    public void setPageCode(String pageCode) {
        this.pageCode = pageCode;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public UserOrgLevel getUserOrgLevel() {
        return userOrgLevel;
    }

    public void setUserOrgLevel(UserOrgLevel userOrgLevel) {
        this.userOrgLevel = userOrgLevel;
    }

    public String getJobKeyword() {
        return jobKeyword;
    }

    public void setJobKeyword(String jobKeyword) {
        this.jobKeyword = jobKeyword;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
