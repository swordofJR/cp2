package com.copyright.controller;

import com.copyright.entity.Copyright;
import com.copyright.service.CopyrightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/copyright")
public class CopyrightController {

    @Autowired
    private CopyrightService copyrightService;

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
            Copyright copyright = copyrightService.uploadCopyright(file, title, description, category, ownerAddress,
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
        return ResponseEntity.ok(copyrightService.getAllCopyrights());
    }

    /**
     * 获取待审核的版权信息
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Copyright>> getPendingCopyrights() {
        return ResponseEntity.ok(copyrightService.getPendingCopyrights());
    }

    /**
     * 获取用户的版权信息
     */
    @GetMapping("/user/{address}")
    public ResponseEntity<List<Copyright>> getUserCopyrights(@PathVariable String address) {
        return ResponseEntity.ok(copyrightService.getUserCopyrights(address));
    }

    /**
     * 获取市场上架的版权信息
     */
    @GetMapping("/marketplace")
    public ResponseEntity<List<Copyright>> getMarketplaceCopyrights() {
        return ResponseEntity.ok(copyrightService.getMarketplaceCopyrights());
    }

    /**
     * 获取版权详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Copyright> getCopyright(@PathVariable Long id) {
        Copyright copyright = copyrightService.getCopyright(id);
        if (copyright != null) {
            return ResponseEntity.ok(copyright);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 审核版权
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<Copyright> reviewCopyright(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        Copyright copyright = copyrightService.reviewCopyright(id, status, reason);
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
        Copyright copyright = copyrightService.listCopyright(id, price);
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
        Copyright copyright = copyrightService.purchaseCopyright(id, newOwnerAddress, newUserId);
        if (copyright != null) {
            return ResponseEntity.ok(copyright);
        }
        return ResponseEntity.badRequest().build();
    }
}