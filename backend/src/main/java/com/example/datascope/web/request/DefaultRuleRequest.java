package com.example.datascope.web.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class DefaultRuleRequest {

    @NotBlank
    private String pageCode;

    @NotBlank
    private String roleCode;

    @NotBlank
    private String userOrgLevel;

    private String jobKeyword;

    @NotBlank
    private String scopeType;

    @NotNull
    private Boolean enabled;

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

    public String getUserOrgLevel() {
        return userOrgLevel;
    }

    public void setUserOrgLevel(String userOrgLevel) {
        this.userOrgLevel = userOrgLevel;
    }

    public String getJobKeyword() {
        return jobKeyword;
    }

    public void setJobKeyword(String jobKeyword) {
        this.jobKeyword = jobKeyword;
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
