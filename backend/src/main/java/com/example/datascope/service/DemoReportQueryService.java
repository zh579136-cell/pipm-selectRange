package com.example.datascope.service;

import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.DemoReportQueryResponse;
import com.example.datascope.model.DemoUser;
import com.example.datascope.model.SqlCondition;
import com.example.datascope.repository.DemoReportRepository;
import org.springframework.stereotype.Service;

@Service
public class DemoReportQueryService {

    private final DataScopeResolveService dataScopeResolveService;
    private final DataScopeSqlBuilder dataScopeSqlBuilder;
    private final DemoReportRepository demoReportRepository;

    public DemoReportQueryService(DataScopeResolveService dataScopeResolveService,
                                  DataScopeSqlBuilder dataScopeSqlBuilder,
                                  DemoReportRepository demoReportRepository) {
        this.dataScopeResolveService = dataScopeResolveService;
        this.dataScopeSqlBuilder = dataScopeSqlBuilder;
        this.demoReportRepository = demoReportRepository;
    }

    public DemoReportQueryResponse query(String pageCode, String customerName, String metricName, DemoUser user) {
        DataScopeResult scopeResult = dataScopeResolveService.resolve(pageCode, user);
        SqlCondition condition = dataScopeSqlBuilder.build(scopeResult);

        DemoReportQueryResponse response = new DemoReportQueryResponse();
        response.setScope(scopeResult);
        if (scopeResult.isAccessDenied()) {
            return response;
        }
        response.setRows(demoReportRepository.query(customerName, metricName, condition));
        return response;
    }
}
