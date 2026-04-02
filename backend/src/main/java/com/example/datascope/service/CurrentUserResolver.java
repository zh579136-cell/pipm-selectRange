package com.example.datascope.service;

import com.example.datascope.exception.BadRequestException;
import com.example.datascope.model.DemoUser;
import com.example.datascope.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
/**
 * 当前用户解析器。
 *
 * 演示版没有接真实登录体系，所以这里统一从请求头里取用户号，
 * 再去用户表查出演示用户完整信息（机构、角色、职位等）。
 *
 * 真实项目里，这一层通常会替换成：
 * - 从 JWT / Session / SSO 上下文取登录用户
 * - 再补齐角色、机构、岗位等扩展信息
 */
public class CurrentUserResolver {

    public static final String DEMO_USER_HEADER = "x-demo-user-id";
    private static final String DEFAULT_USER_ID = "U100";

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 演示版不接真实登录，统一从请求头读取当前用户。
     * 如果前端没有传 header，就默认用 U100，方便本地直接打开页面演示。
     */
    public DemoUser resolveCurrentUser(HttpServletRequest request) {
        String userId = request.getHeader(DEMO_USER_HEADER);
        if (userId == null || userId.trim().isEmpty()) {
            // 本地直接打开页面时，默认给一个演示用户，避免前端必须先做登录切换。
            userId = DEFAULT_USER_ID;
        }
        DemoUser user = userRepository.findByUserId(userId.trim());
        if (user == null) {
            throw new BadRequestException("未找到演示用户: " + userId);
        }
        return user;
    }

    /**
     * 给前端下拉框/切换器使用，返回所有可选演示用户。
     */
    public List<DemoUser> listAllUsers() {
        return userRepository.findAll();
    }
}
