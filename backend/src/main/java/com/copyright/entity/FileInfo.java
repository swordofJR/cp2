// package com.copyright.entity;

// import lombok.Data;
// import javax.persistence.*;
// import java.time.LocalDateTime;

// @Data
// @Entity
// @Table(name = "copyrights")
// public class FileInfo {
// @Id
// @GeneratedValue(strategy = GenerationType.IDENTITY)
// private Long id;

// @Column(nullable = false)
// private int fileId;

// @Column(nullable = false)
// private int ownerId;

// @Column(nullable = false)
// private String fileName;

// @Column(nullable = false)
// private int fileCID;

// @Column(nullable = false)
// private String fileSize;

// @Column(nullable = false)
// private int deleted;

// @Column(nullable = false)
// private LocalDateTime createdTime;

// @Column(nullable = false)
// private LocalDateTime updatedTime;

// @PrePersist
// protected void onCreate() {
// createdTime = LocalDateTime.now();
// updatedTime = LocalDateTime.now();
// }

// @PreUpdate
// protected void onUpdate() {
// updatedTime = LocalDateTime.now();
// }
// }