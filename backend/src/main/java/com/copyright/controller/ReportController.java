package com.copyright.controller;

import com.copyright.entity.Report;
import com.copyright.entity.User;
import com.copyright.service.ReportService;
import com.copyright.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger = Logger.getLogger(ReportController.class.getName());

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    /**
     * 提交举报
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitReport(
            @RequestParam("reporterId") Long reporterId,
            @RequestParam("reportedUserId") Long reportedUserId,
            @RequestParam("copyrightId") Long copyrightId,
            @RequestParam("copyrightTitle") String copyrightTitle,
            @RequestParam("reason") String reason,
            @RequestParam(value = "evidence", required = false) MultipartFile evidenceFile) {

        try {
            logger.info("收到举报提交请求，举报人ID：" + reporterId + "，被举报人ID：" + reportedUserId);

            // 获取举报人用户信息
            User reporter = userService.getUserById(reporterId);
            if (reporter == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "举报人不存在");
                return ResponseEntity.badRequest().body(error);
            }

            // 获取被举报人用户信息
            User reportedUser = userService.getUserById(reportedUserId);
            if (reportedUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "被举报人不存在");
                return ResponseEntity.badRequest().body(error);
            }

            // 提交举报
            Report report = reportService.submitReport(
                    reporterId,
                    reporter.getUsername(),
                    reportedUserId,
                    reportedUser.getUsername(),
                    copyrightId,
                    copyrightTitle,
                    reason,
                    evidenceFile);

            logger.info("举报提交成功，ID：" + report.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", report.getId());
            response.put("status", report.getStatus());
            response.put("message", "举报提交成功，等待管理员审核");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.severe("保存举报证据失败: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "保存举报证据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.severe("提交举报失败: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "提交举报失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取用户提交的举报列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Report>> getUserReports(@PathVariable Long userId) {
        logger.info("获取用户举报列表，用户ID：" + userId);
        return ResponseEntity.ok(reportService.getReportsByReporter(userId));
    }

    /**
     * 获取针对某用户的举报列表（被举报人查看）
     */
    @GetMapping("/reported/{userId}")
    public ResponseEntity<List<Report>> getReportedUserReports(@PathVariable Long userId) {
        logger.info("获取被举报信息，用户ID：" + userId);
        List<Report> reports = reportService.getReportsByReportedUserId(userId);

        // 隐藏举报人信息
        reports.forEach(report -> {
            report.setReporterId(null);
            report.setReporterUsername("匿名用户"); // 隐藏实际举报人用户名
        });

        return ResponseEntity.ok(reports);
    }

    /**
     * 获取待审核的举报列表
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPendingReports() {
        logger.info("获取待审核举报列表");
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    /**
     * 获取所有举报列表（管理员）
     */
    @GetMapping("/all")
    public ResponseEntity<List<Report>> getAllReports() {
        logger.info("获取所有举报列表");
        return ResponseEntity.ok(reportService.getAllReports());
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        logger.info("获取举报详情，ID：" + id);
        Report report = reportService.getReportById(id);
        if (report != null) {
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 审核举报
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<?> reviewReport(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectReason) {

        logger.info("收到举报审核请求，ID：" + id + "，状态：" + status);

        // 只接受 APPROVED 或 REJECTED 状态
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "无效的状态值，只能是 APPROVED 或 REJECTED");
            return ResponseEntity.badRequest().body(error);
        }

        // 如果是驳回，必须提供驳回理由
        if (status.equals("REJECTED") && (rejectReason == null || rejectReason.trim().isEmpty())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "驳回举报时必须提供驳回理由");
            return ResponseEntity.badRequest().body(error);
        }

        Report report = reportService.reviewReport(id, status, rejectReason);
        if (report != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", report.getId());
            response.put("status", report.getStatus());

            if ("APPROVED".equals(status)) {
                logger.info("举报审核通过，举报ID：" + id + "，扣除用户" + report.getReportedUserId() + "积分20分，奖励用户"
                        + report.getReporterId() + "积分10分");
                response.put("message", "举报已通过，已扣除被举报用户20积分，奖励举报人10积分");
            } else {
                logger.info("举报审核驳回，举报ID：" + id + "，驳回原因：" + rejectReason);
                response.put("message", "举报已驳回");
            }

            return ResponseEntity.ok(response);
        }

        logger.warning("未找到指定举报，ID：" + id);
        Map<String, String> error = new HashMap<>();
        error.put("message", "未找到指定举报");
        return ResponseEntity.notFound().build();
    }
}