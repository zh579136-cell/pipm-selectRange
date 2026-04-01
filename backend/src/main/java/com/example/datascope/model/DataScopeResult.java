package com.example.datascope.model;

import com.example.datascope.domain.DataScopeMode;

import java.util.ArrayList;
import java.util.List;

public class DataScopeResult {

    private String pageCode;
    private boolean pageEnabled;
    private boolean accessDenied;
    private String message;
    private DataScopeMode scopeMode;
    private List<RuleMatch> matchedRoleRules = new ArrayList<RuleMatch>();
    private List<ScopeRoot> resolvedScopeRoots = new ArrayList<ScopeRoot>();
    private List<String> exactOrgIds = new ArrayList<String>();
    private List<String> subtreeOrgIds = new ArrayList<String>();
    private String sqlPreview;
    private List<String> visibleOrgIdsPreview = new ArrayList<String>();

    public String getPageCode() {
        return pageCode;
    }

    public void setPageCode(String pageCode) {
        this.pageCode = pageCode;
    }

    public boolean isPageEnabled() {
        return pageEnabled;
    }

    public void setPageEnabled(boolean pageEnabled) {
        this.pageEnabled = pageEnabled;
    }

    public boolean isAccessDenied() {
        return accessDenied;
    }

    public void setAccessDenied(boolean accessDenied) {
        this.accessDenied = accessDenied;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataScopeMode getScopeMode() {
        return scopeMode;
    }

    public void setScopeMode(DataScopeMode scopeMode) {
        this.scopeMode = scopeMode;
    }

    public List<RuleMatch> getMatchedRoleRules() {
        return matchedRoleRules;
    }

    public void setMatchedRoleRules(List<RuleMatch> matchedRoleRules) {
        this.matchedRoleRules = matchedRoleRules;
    }

    public List<ScopeRoot> getResolvedScopeRoots() {
        return resolvedScopeRoots;
    }

    public void setResolvedScopeRoots(List<ScopeRoot> resolvedScopeRoots) {
        this.resolvedScopeRoots = resolvedScopeRoots;
    }

    public List<String> getExactOrgIds() {
        return exactOrgIds;
    }

    public void setExactOrgIds(List<String> exactOrgIds) {
        this.exactOrgIds = exactOrgIds;
    }

    public List<String> getSubtreeOrgIds() {
        return subtreeOrgIds;
    }

    public void setSubtreeOrgIds(List<String> subtreeOrgIds) {
        this.subtreeOrgIds = subtreeOrgIds;
    }

    public String getSqlPreview() {
        return sqlPreview;
    }

    public void setSqlPreview(String sqlPreview) {
        this.sqlPreview = sqlPreview;
    }

    public List<String> getVisibleOrgIdsPreview() {
        return visibleOrgIdsPreview;
    }

    public void setVisibleOrgIdsPreview(List<String> visibleOrgIdsPreview) {
        this.visibleOrgIdsPreview = visibleOrgIdsPreview;
    }
}
