package com.example.datascope.model;

import com.example.datascope.domain.UserOrgLevel;

import java.util.ArrayList;
import java.util.List;

public class DemoUser {

    private String userId;
    private String userName;
    private String loginName;
    private String orgId;
    private String orgName;
    private String orgPathId;
    private UserOrgLevel userOrgLevel;
    private String positionName;
    private List<String> roleCodes = new ArrayList<String>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgPathId() {
        return orgPathId;
    }

    public void setOrgPathId(String orgPathId) {
        this.orgPathId = orgPathId;
    }

    public UserOrgLevel getUserOrgLevel() {
        return userOrgLevel;
    }

    public void setUserOrgLevel(UserOrgLevel userOrgLevel) {
        this.userOrgLevel = userOrgLevel;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}
