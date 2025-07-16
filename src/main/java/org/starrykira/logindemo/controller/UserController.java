package org.starrykira.logindemo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.starrykira.logindemo.dto.ApiResponse;
import org.starrykira.logindemo.dto.UserResponse;
import org.starrykira.logindemo.entity.User;
import org.starrykira.logindemo.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取所有用户 (需要登录验证)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(HttpServletRequest request) {
        try {
            // 简单的登录验证
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请先登录"));
            }
            
            List<UserResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取用户列表失败"));
        }
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 简单的登录验证
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请先登录"));
            }
            
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            UserResponse userResponse = new UserResponse(user);
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", userResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, 
                                                               @RequestBody User userDetails,
                                                               HttpServletRequest request) {
        try {
            // 简单的登录验证
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请先登录"));
            }
            
            // 检查是否是本人或管理员
            User currentUser = (User) session.getAttribute("user");
            if (!currentUser.getId().equals(id) && currentUser.getRole() != User.Role.ADMIN) {
                return ResponseEntity.badRequest().body(ApiResponse.error("无权限修改其他用户信息"));
            }
            
            User updatedUser = userService.updateUser(id, userDetails);
            UserResponse userResponse = new UserResponse(updatedUser);
            return ResponseEntity.ok(ApiResponse.success("更新用户信息成功", userResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除用户 (需要管理员权限)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 简单的登录和权限验证
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请先登录"));
            }
            
            User currentUser = (User) session.getAttribute("user");
            if (currentUser.getRole() != User.Role.ADMIN) {
                return ResponseEntity.badRequest().body(ApiResponse.error("需要管理员权限"));
            }
            
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("删除用户成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Long id, 
                                                           @RequestBody Map<String, String> passwordData,
                                                           HttpServletRequest request) {
        try {
            // 简单的登录验证
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请先登录"));
            }
            
            // 检查是否是本人
            User currentUser = (User) session.getAttribute("user");
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("只能修改自己的密码"));
            }
            
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("密码不能为空"));
            }
            
            userService.changePassword(id, oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户的个人资料
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请先登录"));
            }
            
            User user = (User) session.getAttribute("user");
            // 重新从数据库获取最新用户信息
            User currentUser = userService.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            UserResponse userResponse = new UserResponse(currentUser);
            return ResponseEntity.ok(ApiResponse.success("获取个人资料成功", userResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取个人资料失败"));
        }
    }
} 