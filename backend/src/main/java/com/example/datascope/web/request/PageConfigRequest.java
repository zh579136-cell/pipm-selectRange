package com.example.datascope.web.request;

import javax.validation.constraints.NotBlank;

public class PageConfigRequest {

    @NotBlank
    private String pageCode;

    @NotBlank
    private String pageName;

    private boolean enabled;

    public String getPageCode() {
        return pageCode;
    }

    public void setPageCode(String pageCode) {
        this.pageCode = pageCode;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
