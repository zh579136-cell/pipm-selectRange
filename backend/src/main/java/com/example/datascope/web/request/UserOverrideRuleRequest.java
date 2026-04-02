package com.example.datascope.web.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class UserOverrideRuleRequest {

    @NotBlank(message = "userId不能为空")
    private String userId;

    @NotBlank(message = "pageCode不能为空")
    private String pageCode;

    @NotBlank(message = "roleCode不能为空")
    private String roleCode;

    private String scopeType;

    private List<String> customOrgIds = new ArrayList<String>();

    @NotNull(message = "enabled不能为空")
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

    public List<String> getCustomOrgIds() {
        return customOrgIds;
    }

    public void setCustomOrgIds(List<String> customOrgIds) {
        this.customOrgIds = customOrgIds;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
