package com.example.datascope.service;

import com.example.datascope.domain.ScopeType;
import com.example.datascope.exception.BadRequestException;
import com.example.datascope.exception.ConfigConflictException;
import com.example.datascope.model.UserOverrideRule;
import com.example.datascope.repository.UserOverrideRuleRepository;
import com.example.datascope.web.request.UserOverrideRuleRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserOverrideRuleService {

    private final UserOverrideRuleRepository repository;

    public UserOverrideRuleService(UserOverrideRuleRepository repository) {
        this.repository = repository;
    }

    public List<UserOverrideRule> findAll(String userId, String pageCode, String roleCode) {
        return repository.findAll(userId, pageCode, roleCode);
    }

    public UserOverrideRule create(UserOverrideRuleRequest request) {
        UserOverrideRule rule = fromRequest(request);
        validateUnique(rule, null);
        return repository.save(rule);
    }

    public UserOverrideRule update(Long id, UserOverrideRuleRequest request) {
        UserOverrideRule exists = repository.findById(id);
        if (exists == null) {
            throw new BadRequestException("未找到个人覆盖规则: " + id);
        }
        UserOverrideRule rule = fromRequest(request);
        rule.setId(id);
        validateUnique(rule, id);
        repository.update(rule);
        return repository.findById(id);
    }

    public void delete(Long id) {
        UserOverrideRule exists = repository.findById(id);
        if (exists == null) {
            throw new BadRequestException("未找到个人覆盖规则: " + id);
        }
        repository.deleteById(id);
    }

    private void validateUnique(UserOverrideRule rule, Long selfId) {
        List<UserOverrideRule> rules = repository.findByUniqueKey(rule.getUserId(), rule.getPageCode(), rule.getRoleCode());
        for (UserOverrideRule item : rules) {
            if (selfId != null && selfId.equals(item.getId())) {
                continue;
            }
            throw new ConfigConflictException("个人覆盖规则唯一键冲突：同一用户、页面、角色只能有一条规则。");
        }
    }

    private UserOverrideRule fromRequest(UserOverrideRuleRequest request) {
        UserOverrideRule rule = new UserOverrideRule();
        rule.setUserId(request.getUserId().trim());
        rule.setPageCode(request.getPageCode().trim());
        rule.setRoleCode(request.getRoleCode().trim());
        List<String> customOrgIds = request.getCustomOrgIds() == null ? java.util.Collections.<String>emptyList() : request.getCustomOrgIds().stream()
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .distinct()
            .collect(Collectors.toList());
        rule.setCustomOrgIds(customOrgIds);
        if (!customOrgIds.isEmpty()) {
            if (customOrgIds.size() != 1) {
                throw new BadRequestException("个人覆盖只能选择一个机构根节点。");
            }
            rule.setScopeType(null);
        } else if (request.getScopeType() != null && !request.getScopeType().trim().isEmpty()) {
            rule.setScopeType(ScopeType.valueOf(request.getScopeType().trim()));
        } else {
            throw new BadRequestException("个人覆盖必须配置快捷范围或个人覆盖根节点。");
        }
        rule.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        return rule;
    }
}
