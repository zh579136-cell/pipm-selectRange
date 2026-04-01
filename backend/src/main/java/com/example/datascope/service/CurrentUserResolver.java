package com.example.datascope.service;

import com.example.datascope.exception.BadRequestException;
import com.example.datascope.model.DemoUser;
import com.example.datascope.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class CurrentUserResolver {

    public static final String DEMO_USER_HEADER = "x-demo-user-id";
    private static final String DEFAULT_USER_ID = "U100";

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public DemoUser resolveCurrentUser(HttpServletRequest request) {
        String userId = request.getHeader(DEMO_USER_HEADER);
        if (userId == null || userId.trim().isEmpty()) {
            userId = DEFAULT_USER_ID;
        }
        DemoUser user = userRepository.findByUserId(userId.trim());
        if (user == null) {
            throw new BadRequestException("未找到演示用户: " + userId);
        }
        return user;
    }

    public List<DemoUser> listAllUsers() {
        return userRepository.findAll();
    }
}
