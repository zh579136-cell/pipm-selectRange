package com.example.datascope.web;

import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.DefaultRule;
import com.example.datascope.model.DemoUser;
import com.example.datascope.model.PageConfig;
import com.example.datascope.model.UserOverrideRule;
import com.example.datascope.exception.BadRequestException;
import com.example.datascope.repository.UserRepository;
import com.example.datascope.service.DataScopeResolveService;
import com.example.datascope.service.DefaultRuleService;
import com.example.datascope.service.PageConfigService;
import com.example.datascope.service.UserOverrideRuleService;
import com.example.datascope.web.request.DefaultRuleRequest;
import com.example.datascope.web.request.PageConfigRequest;
import com.example.datascope.web.request.ResolvePreviewRequest;
import com.example.datascope.web.request.UserOverrideRuleRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/data-scope")
public class DataScopeManagementController {

    private final PageConfigService pageConfigService;
    private final DefaultRuleService defaultRuleService;
    private final UserOverrideRuleService userOverrideRuleService;
    private final DataScopeResolveService dataScopeResolveService;
    private final UserRepository userRepository;

    public DataScopeManagementController(PageConfigService pageConfigService,
                                         DefaultRuleService defaultRuleService,
                                         UserOverrideRuleService userOverrideRuleService,
                                         DataScopeResolveService dataScopeResolveService,
                                         UserRepository userRepository) {
        this.pageConfigService = pageConfigService;
        this.defaultRuleService = defaultRuleService;
        this.userOverrideRuleService = userOverrideRuleService;
        this.dataScopeResolveService = dataScopeResolveService;
        this.userRepository = userRepository;
    }

    // 页面、默认规则、个人覆盖都放在这个控制器下，前端可以把它当成“规则配置台”的后端接口。
    @GetMapping("/pages")
    public List<PageConfig> pageConfigs() {
        return pageConfigService.findAll();
    }

    @PostMapping("/pages")
    public PageConfig createPage(@Validated @RequestBody PageConfigRequest request) {
        return pageConfigService.save(request);
    }

    @PutMapping("/pages/{pageCode}")
    public PageConfig updatePage(@PathVariable String pageCode, @Validated @RequestBody PageConfigRequest request) {
        return pageConfigService.update(pageCode, request);
    }

    @GetMapping("/default-rules")
    public List<DefaultRule> defaultRules(@RequestParam(required = false) String pageCode,
                                          @RequestParam(required = false) String roleCode,
                                          @RequestParam(required = false) String userOrgLevel) {
        return defaultRuleService.findAll(pageCode, roleCode, userOrgLevel);
    }

    @PostMapping("/default-rules")
    public DefaultRule createDefaultRule(@Validated @RequestBody DefaultRuleRequest request) {
        return defaultRuleService.create(request);
    }

    @PutMapping("/default-rules/{id}")
    public DefaultRule updateDefaultRule(@PathVariable Long id, @Validated @RequestBody DefaultRuleRequest request) {
        return defaultRuleService.update(id, request);
    }

    @DeleteMapping("/default-rules/{id}")
    public void deleteDefaultRule(@PathVariable Long id) {
        defaultRuleService.delete(id);
    }

    @GetMapping("/user-rules")
    public List<UserOverrideRule> userRules(@RequestParam(required = false) String userId,
                                            @RequestParam(required = false) String pageCode,
                                            @RequestParam(required = false) String roleCode) {
        return userOverrideRuleService.findAll(userId, pageCode, roleCode);
    }

    @PostMapping("/user-rules")
    public UserOverrideRule createUserRule(@Validated @RequestBody UserOverrideRuleRequest request) {
        return userOverrideRuleService.create(request);
    }

    @PutMapping("/user-rules/{id}")
    public UserOverrideRule updateUserRule(@PathVariable Long id, @Validated @RequestBody UserOverrideRuleRequest request) {
        return userOverrideRuleService.update(id, request);
    }

    @DeleteMapping("/user-rules/{id}")
    public void deleteUserRule(@PathVariable Long id) {
        userOverrideRuleService.delete(id);
    }

    @PostMapping("/resolve-preview")
    public DataScopeResult resolvePreview(@Validated @RequestBody ResolvePreviewRequest request) {
        DemoUser user = userRepository.findByUserId(request.getUserId());
        if (user == null) {
            throw new BadRequestException("未找到试算用户: " + request.getUserId());
        }
        return dataScopeResolveService.resolve(request.getPageCode(), user);
    }
}
