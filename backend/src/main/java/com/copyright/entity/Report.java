package com.copyright.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId; // 举报人ID

    @Column(nullable = false)
    private String reporterUsername; // 举报人用户名

    @Column(nullable = false)
    private Long reportedUserId; // 被举报人ID

    @Column(nullable = false)
    private String reportedUsername; // 被举报人用户名

    @Column(nullable = false)
    private Long copyrightId; // 涉及的版权ID

    @Column(nullable = false)
    private String copyrightTitle; // 版权标题

    @Column(nullable = false)
    private String reason; // 举报原因

    @Column(name = "evidence_img")
    private String evidenceImg; // 证据图片

    @Column(nullable = false)
    private String status; // 状态：PENDING 待审核, APPROVED 通过, REJECTED 拒绝

    @Column
    private String rejectReason; // 拒绝理由

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}