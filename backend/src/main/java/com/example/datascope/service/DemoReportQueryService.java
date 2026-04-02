package com.example.datascope.service;

import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.DemoReportQueryResponse;
import com.example.datascope.model.DemoUser;
import com.example.datascope.model.SqlCondition;
import com.example.datascope.repository.DemoReportRepository;
import org.springframework.stereotype.Service;

@Service
/**
 * 演示查询页服务。
 *
 * 这个类代表业务系统里“某个真实查询页面”的典型接入方式。
 * 它不自己理解角色、规则、机构树，而是只做三件事：
 * 1. 调用数据范围服务拿到范围结果
 * 2. 把范围结果转换成 SQL 条件
 * 3. 把这个条件交给真实查询仓储执行
 *
 * 换句话说，业务查询页真正需要接入的地方，就是这一层。
 */
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

    /**
     * 模拟业务查询页的接入方式：
     * 1. 先按 pageCode + 当前用户算出数据范围
     * 2. 再把数据范围转换成 SQL 过滤条件
     * 3. 最后将条件拼进真实查询
     */
    public DemoReportQueryResponse query(String pageCode, String customerName, String metricName, DemoUser user) {
        // 先基于 pageCode + 当前用户，统一算出可见机构范围。
        DataScopeResult scopeResult = dataScopeResolveService.resolve(pageCode, user);

        // 再把范围结果翻译成 SQL 条件，供仓储层直接使用。
        SqlCondition condition = dataScopeSqlBuilder.build(scopeResult);

        DemoReportQueryResponse response = new DemoReportQueryResponse();
        response.setScope(scopeResult);
        if (scopeResult.isAccessDenied()) {
            // 无权限时直接返回空结果，但仍然把 scope 信息带回前端，方便页面解释“为什么没数据”。
            return response;
        }

        // 真正查数据的地方只认 SQL 条件，不需要感知规则细节。
        response.setRows(demoReportRepository.query(customerName, metricName, condition));
        return response;
    }
}
