package com.example.datascope.model;

import java.util.ArrayList;
import java.util.List;

public class DemoReportQueryResponse {

    private DataScopeResult scope;
    private List<DemoReportRow> rows = new ArrayList<DemoReportRow>();

    public DataScopeResult getScope() {
        return scope;
    }

    public void setScope(DataScopeResult scope) {
        this.scope = scope;
    }

    public List<DemoReportRow> getRows() {
        return rows;
    }

    public void setRows(List<DemoReportRow> rows) {
        this.rows = rows;
    }
}
