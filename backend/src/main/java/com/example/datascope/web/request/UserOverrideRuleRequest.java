package com.example.datascope.web.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UserOverrideRuleRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String pageCode;

    @NotBlank
    private String roleCode;

    @NotBlank
    private String scopeType;

    @NotNull
    private Boolean enabled;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
