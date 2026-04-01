package com.example.datascope.model;

import java.util.ArrayList;
import java.util.List;

public class SqlCondition {

    private String whereClause;
    private String preview;
    private List<Object> args = new ArrayList<Object>();

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }
}
