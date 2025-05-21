package com.copyright.controller;

import com.copyright.entity.User;
import com.copyright.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jdbc/user")
public class JdbcUserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username, @RequestParam String password) {
        try {
            // 验证用户名是否已存在
            if (userService.isUsernameExists(username)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户名已存在");
                return ResponseEntity.badRequest().body(error);
            }

            User user = userService.register(username, password);
            if (user != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("message", "注册成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "注册失败");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "服务器错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            User user = userService.login(username, password);
            if (user != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("address", user.getAddress());
                response.put("credit", user.getCredit()); // 添加用户积分信息
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户名或密码错误");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "服务器错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}/credit")
    public ResponseEntity<Map<String, Object>> getUserCredit(@PathVariable Long id) {
        int credit = userService.getUserCredit(id);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", id);
        response.put("credit", credit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/eligible")
    public ResponseEntity<Map<String, Object>> isUserEligible(@PathVariable Long id) {
        boolean eligible = userService.isEligibleForTransaction(id);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", id);
        response.put("eligible", eligible);
        if (!eligible) {
            response.put("message", "积分不足，无法参与交易。最低要求积分为60分。");
        }
        return ResponseEntity.ok(response);
    }
}