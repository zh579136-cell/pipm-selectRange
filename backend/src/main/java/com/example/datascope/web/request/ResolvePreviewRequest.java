package com.example.datascope.web.request;

import javax.validation.constraints.NotBlank;

public class ResolvePreviewRequest {

    @NotBlank
    private String pageCode;

    @NotBlank
    private String userId;

    public String getPageCode() {
        return pageCode;
    }

    public void setPageCode(String pageCode) {
        this.pageCode = pageCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
