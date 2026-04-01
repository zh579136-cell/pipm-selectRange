package com.example.datascope.model;

import com.example.datascope.domain.OrgLevel;

public class OrgNode {

    private String orgId;
    private String orgName;
    private String parentOrgId;
    private OrgLevel orgLevel;
    private String orgPathId;

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

    public String getParentOrgId() {
        return parentOrgId;
    }

    public void setParentOrgId(String parentOrgId) {
        this.parentOrgId = parentOrgId;
    }

    public OrgLevel getOrgLevel() {
        return orgLevel;
    }

    public void setOrgLevel(OrgLevel orgLevel) {
        this.orgLevel = orgLevel;
    }

    public String getOrgPathId() {
        return orgPathId;
    }

    public void setOrgPathId(String orgPathId) {
        this.orgPathId = orgPathId;
    }
}
