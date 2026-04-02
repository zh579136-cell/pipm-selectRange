package com.example.datascope.model;

import com.example.datascope.domain.RuleSource;
import com.example.datascope.domain.ScopeType;

public class RuleMatch {

    private String roleCode;
    private RuleSource ruleSource;
    private Long ruleId;
    private ScopeType scopeType;
    private String matchedBy;
    private String matchedUserOrgLevel;
    private String resolvedScopeLabel;
    private String resolvedScopeNote;
    private String resolvedOrgId;
    private String resolvedOrgName;
    private java.util.List<String> resolvedOrgIds = new java.util.ArrayList<String>();

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public RuleSource getRuleSource() {
        return ruleSource;
    }

    public void setRuleSource(RuleSource ruleSource) {
        this.ruleSource = ruleSource;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public String getMatchedBy() {
        return matchedBy;
    }

    public void setMatchedBy(String matchedBy) {
        this.matchedBy = matchedBy;
    }

    public String getMatchedUserOrgLevel() {
        return matchedUserOrgLevel;
    }

    public void setMatchedUserOrgLevel(String matchedUserOrgLevel) {
        this.matchedUserOrgLevel = matchedUserOrgLevel;
    }

    public String getResolvedScopeLabel() {
        return resolvedScopeLabel;
    }

    public void setResolvedScopeLabel(String resolvedScopeLabel) {
        this.resolvedScopeLabel = resolvedScopeLabel;
    }

    public String getResolvedScopeNote() {
        return resolvedScopeNote;
    }

    public void setResolvedScopeNote(String resolvedScopeNote) {
        this.resolvedScopeNote = resolvedScopeNote;
    }

    public String getResolvedOrgId() {
        return resolvedOrgId;
    }

    public void setResolvedOrgId(String resolvedOrgId) {
        this.resolvedOrgId = resolvedOrgId;
    }

    public String getResolvedOrgName() {
        return resolvedOrgName;
    }

    public void setResolvedOrgName(String resolvedOrgName) {
        this.resolvedOrgName = resolvedOrgName;
    }

    public java.util.List<String> getResolvedOrgIds() {
        return resolvedOrgIds;
    }

    public void setResolvedOrgIds(java.util.List<String> resolvedOrgIds) {
        this.resolvedOrgIds = resolvedOrgIds;
    }
}
