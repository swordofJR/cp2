package com.copyright.repository;

import com.copyright.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    // 查找用户提交的举报
    List<Report> findByReporterIdOrderByCreatedTimeDesc(Long reporterId);

    // 查找针对某用户的举报
    List<Report> findByReportedUserIdOrderByCreatedTimeDesc(Long reportedUserId);

    // 查找待审核的举报
    List<Report> findByStatusOrderByCreatedTimeDesc(String status);
}