package com.example.datascope.web;

import com.example.datascope.model.DemoReportQueryResponse;
import com.example.datascope.model.DemoUser;
import com.example.datascope.service.CurrentUserResolver;
import com.example.datascope.service.DemoReportQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/demo/report")
public class DemoReportController {

    private final DemoReportQueryService demoReportQueryService;
    private final CurrentUserResolver currentUserResolver;

    public DemoReportController(DemoReportQueryService demoReportQueryService, CurrentUserResolver currentUserResolver) {
        this.demoReportQueryService = demoReportQueryService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/query")
    public DemoReportQueryResponse query(@RequestParam String pageCode,
                                         @RequestParam(required = false) String customerName,
                                         @RequestParam(required = false) String metricName,
                                         HttpServletRequest request) {
        DemoUser user = currentUserResolver.resolveCurrentUser(request);
        return demoReportQueryService.query(pageCode, customerName, metricName, user);
    }
}
