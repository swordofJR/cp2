package com.copyright.controller;

import com.copyright.entity.Copyright;
import com.copyright.service.CopyrightJdbcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jdbc/copyright")
public class CopyrightJdbcController {

    @Autowired
    private CopyrightJdbcService copyrightJdbcService;

    /**
     * 上传版权信息
     */
    @PostMapping("/upload")
    public ResponseEntity<Copyright> uploadCopyright(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("ownerAddress") String ownerAddress,
            @RequestParam(value = "userId", required = false) Long userId) {
        try {
            Copyright copyright = copyrightJdbcService.uploadCopyright(file, title, description, category, ownerAddress,
                    userId);
            return ResponseEntity.ok(copyright);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取所有版权信息
     */
    @GetMapping("/all")
    public ResponseEntity<List<Copyright>> getAllCopyrights() {
        return ResponseEntity.ok(copyrightJdbcService.getAllCopyrights());
    }

    /**
     * 获取所有版权信息（包含用户名）
     */
    @GetMapping("/all-with-users")
    public ResponseEntity<List<Map<String, Object>>> getAllCopyrightsWithUsers() {
        return ResponseEntity.ok(copyrightJdbcService.getAllCopyrightsWithUsernames());
    }

    /**
     * 获取待审核的版权信息
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Copyright>> getPendingCopyrights() {
        return ResponseEntity.ok(copyrightJdbcService.getPendingCopyrights());
    }

    /**
     * 获取用户的版权信息
     */
    @GetMapping("/user/{address}")
    public ResponseEntity<List<Copyright>> getUserCopyrights(@PathVariable String address) {
        return ResponseEntity.ok(copyrightJdbcService.getUserCopyrights(address));
    }

    /**
     * 获取用户的版权信息，包含用户名
     */
    @GetMapping("/user/{address}/with-username")
    public ResponseEntity<List<Map<String, Object>>> getUserCopyrightsWithUsername(
            @PathVariable String address,
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(copyrightJdbcService.getUserCopyrightsWithUsernameAndStatus(address, status));
        }
        return ResponseEntity.ok(copyrightJdbcService.getUserCopyrightsWithUsername(address));
    }

    /**
     * 获取用户的版权信息 (通过用户ID)
     */
    @GetMapping("/user-id/{userId}")
    public ResponseEntity<List<Copyright>> getUserCopyrightsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(copyrightJdbcService.getUserCopyrightsByUserId(userId));
    }

    /**
     * 获取用户的版权信息 (通过用户ID)，包含用户名
     */
    @GetMapping("/user-id/{userId}/with-username")
    public ResponseEntity<List<Map<String, Object>>> getUserCopyrightsByUserIdWithUsername(
            @PathVariable Long userId,
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity
                    .ok(copyrightJdbcService.getUserCopyrightsByUserIdWithUsernameAndStatus(userId, status));
        }
        return ResponseEntity.ok(copyrightJdbcService.getUserCopyrightsByUserIdWithUsername(userId));
    }

    /**
     * 获取市场上架的版权信息
     */
    @GetMapping("/marketplace")
    public ResponseEntity<List<Copyright>> getMarketplaceCopyrights() {
        return ResponseEntity.ok(copyrightJdbcService.getMarketplaceCopyrights());
    }

    /**
     * 获取市场上架的版权信息（包含用户名）
     */
    @GetMapping("/marketplace-with-usernames")
    public ResponseEntity<List<Map<String, Object>>> getMarketplaceCopyrightsWithUsernames() {
        return ResponseEntity.ok(copyrightJdbcService.getMarketplaceCopyrightsWithUsernames());
    }

    /**
     * 审核版权
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<Copyright> reviewCopyright(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        Copyright copyright = copyrightJdbcService.reviewCopyright(id, status, reason);
        if (copyright != null) {
            return ResponseEntity.ok(copyright);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 上架版权
     */
    @PostMapping("/{id}/list")
    public ResponseEntity<Copyright> listCopyright(
            @PathVariable Long id,
            @RequestParam BigDecimal price) {
        Copyright copyright = copyrightJdbcService.listCopyright(id, price);
        if (copyright != null) {
            return ResponseEntity.ok(copyright);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 购买版权
     */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Copyright> purchaseCopyright(
            @PathVariable Long id,
            @RequestParam String newOwnerAddress,
            @RequestParam(required = false) Long newUserId) {
        Copyright copyright = copyrightJdbcService.purchaseCopyright(id, newOwnerAddress, newUserId);
        if (copyright != null) {
            return ResponseEntity.ok(copyright);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 获取版权详情
     * 注意：这个端点必须放在最后，避免与其他路径冲突
     */
    @GetMapping("/{id}")
    public ResponseEntity<Copyright> getCopyright(@PathVariable Long id) {
        Copyright copyright = copyrightJdbcService.getCopyright(id);
        if (copyright != null) {
            return ResponseEntity.ok(copyright);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 下架版权
     */
    @PostMapping("/{id}/delist")
    public ResponseEntity<Copyright> delistCopyright(
            @PathVariable Long id) {
        Copyright copyright = copyrightJdbcService.delistCopyright(id);
        if (copyright != null) {
            return ResponseEntity.ok(copyright);
        }
        return ResponseEntity.badRequest().build();
    }
}