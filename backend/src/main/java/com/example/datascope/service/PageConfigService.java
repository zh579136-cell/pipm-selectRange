package com.example.datascope.service;

import com.example.datascope.exception.BadRequestException;
import com.example.datascope.model.PageConfig;
import com.example.datascope.repository.PageConfigRepository;
import com.example.datascope.web.request.PageConfigRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageConfigService {

    private final PageConfigRepository repository;

    public PageConfigService(PageConfigRepository repository) {
        this.repository = repository;
    }

    public List<PageConfig> findAll() {
        return repository.findAll();
    }

    public PageConfig save(PageConfigRequest request) {
        PageConfig pageConfig = new PageConfig();
        pageConfig.setPageCode(request.getPageCode().trim());
        pageConfig.setPageName(request.getPageName().trim());
        pageConfig.setEnabled(request.isEnabled());
        repository.save(pageConfig);
        return repository.findByPageCode(pageConfig.getPageCode());
    }

    public PageConfig update(String pageCode, PageConfigRequest request) {
        if (!pageCode.equals(request.getPageCode().trim())) {
            throw new BadRequestException("路径中的 pageCode 与表单中的 pageCode 不一致。");
        }
        return save(request);
    }
}
