package com.example.datascope.web;

import com.example.datascope.domain.ScopeType;
import com.example.datascope.domain.UserOrgLevel;
import com.example.datascope.model.DataScopeResult;
import com.example.datascope.model.DemoUser;
import com.example.datascope.model.RuntimeOptions;
import com.example.datascope.repository.RoleRepository;
import com.example.datascope.service.CurrentUserResolver;
import com.example.datascope.service.DataScopeResolveService;
import com.example.datascope.service.PageConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/runtime")
public class RuntimeController {

    private final CurrentUserResolver currentUserResolver;
    private final DataScopeResolveService dataScopeResolveService;
    private final RoleRepository roleRepository;
    private final PageConfigService pageConfigService;

    public RuntimeController(CurrentUserResolver currentUserResolver,
                             DataScopeResolveService dataScopeResolveService,
                             RoleRepository roleRepository,
                             PageConfigService pageConfigService) {
        this.currentUserResolver = currentUserResolver;
        this.dataScopeResolveService = dataScopeResolveService;
        this.roleRepository = roleRepository;
        this.pageConfigService = pageConfigService;
    }

    @GetMapping("/current-user")
    public DemoUser currentUser(HttpServletRequest request) {
        return currentUserResolver.resolveCurrentUser(request);
    }

    @GetMapping("/demo-users")
    public List<DemoUser> demoUsers() {
        return currentUserResolver.listAllUsers();
    }

    @GetMapping("/data-scope/resolve")
    public DataScopeResult resolve(@RequestParam String pageCode, HttpServletRequest request) {
        DemoUser user = currentUserResolver.resolveCurrentUser(request);
        return dataScopeResolveService.resolve(pageCode, user);
    }

    @GetMapping("/options")
    public RuntimeOptions options() {
        RuntimeOptions options = new RuntimeOptions();
        options.setUsers(currentUserResolver.listAllUsers());
        options.setRoles(roleRepository.findAll());
        options.setPageConfigs(pageConfigService.findAll());

        List<String> scopeTypes = new ArrayList<String>();
        for (ScopeType scopeType : ScopeType.values()) {
            scopeTypes.add(scopeType.name());
        }
        options.setScopeTypes(scopeTypes);

        List<String> userOrgLevels = new ArrayList<String>();
        for (UserOrgLevel level : UserOrgLevel.values()) {
            userOrgLevels.add(level.name());
        }
        options.setUserOrgLevels(userOrgLevels);
        return options;
    }
}
