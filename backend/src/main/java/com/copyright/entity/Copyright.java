package com.copyright.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "copyrights")
public class Copyright {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String imgUrl;

    @Column
    private String ipfsHash;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, LISTED, SOLD

    @Column(nullable = false)
    private String ownerAddress;

    @Column
    private Long userId;

    @Column
    private BigDecimal price;

    @Column
    private String reason; // 审核拒绝原因

    @Column(nullable = false)
    private LocalDateTime createdTime;

    @Column(nullable = false)
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