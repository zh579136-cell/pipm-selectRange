package com.example.datascope.service;

import com.example.datascope.domain.ScopeType;
import com.example.datascope.domain.UserOrgLevel;
import com.example.datascope.exception.BadRequestException;
import com.example.datascope.exception.ConfigConflictException;
import com.example.datascope.model.DefaultRule;
import com.example.datascope.repository.DefaultRuleRepository;
import com.example.datascope.web.request.DefaultRuleRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultRuleService {

    private final DefaultRuleRepository repository;

    public DefaultRuleService(DefaultRuleRepository repository) {
        this.repository = repository;
    }

    public List<DefaultRule> findAll(String pageCode, String roleCode, String userOrgLevel) {
        return repository.findAll(pageCode, roleCode, userOrgLevel);
    }

    public DefaultRule create(DefaultRuleRequest request) {
        DefaultRule rule = fromRequest(request);
        validateUnique(rule, null);
        return repository.save(rule);
    }

    public DefaultRule update(Long id, DefaultRuleRequest request) {
        DefaultRule exists = repository.findById(id);
        if (exists == null) {
            throw new BadRequestException("未找到默认规则: " + id);
        }
        DefaultRule rule = fromRequest(request);
        rule.setId(id);
        validateUnique(rule, id);
        repository.update(rule);
        return repository.findById(id);
    }

    public void delete(Long id) {
        DefaultRule exists = repository.findById(id);
        if (exists == null) {
            throw new BadRequestException("未找到默认规则: " + id);
        }
        repository.deleteById(id);
    }

    private void validateUnique(DefaultRule rule, Long selfId) {
        List<DefaultRule> rules = repository.findByUniqueKey(rule.getPageCode(), rule.getRoleCode(), rule.getUserOrgLevel());
        String normalizedKeyword = normalize(rule.getJobKeyword());
        for (DefaultRule item : rules) {
            if (selfId != null && selfId.equals(item.getId())) {
                continue;
            }
            if (normalize(item.getJobKeyword()).equals(normalizedKeyword)) {
                throw new ConfigConflictException("默认规则唯一键冲突：同一页面、角色、机构层级、职位关键词只能有一条规则。");
            }
        }
    }

    private DefaultRule fromRequest(DefaultRuleRequest request) {
        DefaultRule rule = new DefaultRule();
        rule.setPageCode(request.getPageCode().trim());
        rule.setRoleCode(request.getRoleCode().trim());
        rule.setUserOrgLevel(UserOrgLevel.valueOf(request.getUserOrgLevel().trim()));
        rule.setJobKeyword(normalizeToNull(request.getJobKeyword()));
        rule.setScopeType(ScopeType.valueOf(request.getScopeType().trim()));
        rule.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        return rule;
    }

    private String normalizeToNull(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
