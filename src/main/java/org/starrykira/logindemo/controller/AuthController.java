package org.starrykira.logindemo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.starrykira.logindemo.dto.*;
import org.starrykira.logindemo.entity.User;
import org.starrykira.logindemo.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 简单验证
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户名不能为空"));
            }
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("密码不能为空"));
            }
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("邮箱不能为空"));
            }
            
            User user = userService.registerUser(registerRequest);
            UserResponse userResponse = new UserResponse(user);
            return ResponseEntity.ok(ApiResponse.success("注册成功", userResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest loginRequest, 
                                                                 HttpServletRequest request) {
        try {
            // 简单验证
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户名不能为空"));
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("密码不能为空"));
            }
            
            // 验证用户登录
            User user = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
            
            // 创建Session (存储在Redis中)
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            
            // 返回响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", new UserResponse(user));
            responseData.put("sessionId", session.getId());
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", responseData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        try {
            // 清除Session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            return ResponseEntity.ok(ApiResponse.success("登出成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("登出失败"));
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    // 重新从数据库获取最新用户信息
                    User currentUser = userService.findById(user.getId())
                            .orElseThrow(() -> new RuntimeException("用户不存在"));
                    UserResponse userResponse = new UserResponse(currentUser);
                    return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", userResponse));
                }
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取用户信息失败"));
        }
    }
    
    /**
     * 检查登录状态
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkLoginStatus(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    return ResponseEntity.ok(ApiResponse.success("已登录", true));
                }
            }
            return ResponseEntity.ok(ApiResponse.success("未登录", false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("检查登录状态失败"));
        }
    }
} 