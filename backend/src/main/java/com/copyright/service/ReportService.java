package com.copyright.service;

import com.copyright.entity.Report;
import com.copyright.entity.User;
import com.copyright.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ReportService {

    private static final Logger logger = Logger.getLogger(ReportService.class.getName());

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserService userService;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/evidence";

    public ReportService() {
        // 创建证据图片上传目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // 提交举报
    public Report submitReport(Long reporterId, String reporterUsername,
            Long reportedUserId, String reportedUsername,
            Long copyrightId, String copyrightTitle,
            String reason, MultipartFile evidenceFile) throws IOException {

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setReporterUsername(reporterUsername);
        report.setReportedUserId(reportedUserId);
        report.setReportedUsername(reportedUsername);
        report.setCopyrightId(copyrightId);
        report.setCopyrightTitle(copyrightTitle);
        report.setReason(reason);
        report.setStatus("PENDING");

        // 保存证据图片
        if (evidenceFile != null && !evidenceFile.isEmpty()) {
            String evidenceImgPath = saveEvidenceFile(evidenceFile);
            report.setEvidenceImg(evidenceImgPath);
        }

        logger.info("创建新举报：举报人ID=" + reporterId + ", 被举报人ID=" + reportedUserId);
        return reportRepository.save(report);
    }

    // 保存证据图片
    private String saveEvidenceFile(MultipartFile file) throws IOException {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = "evidence_" + UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.write(filePath, file.getBytes());

        logger.info("保存举报证据图片：" + uniqueFileName);
        return uniqueFileName;
    }

    // 获取用户提交的举报
    public List<Report> getReportsByReporter(Long reporterId) {
        return reportRepository.findByReporterIdOrderByCreatedTimeDesc(reporterId);
    }

    // 获取针对某用户的举报
    public List<Report> getReportsByReportedUserId(Long reportedUserId) {
        return reportRepository.findByReportedUserIdOrderByCreatedTimeDesc(reportedUserId);
    }

    // 获取待审核的举报
    public List<Report> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedTimeDesc("PENDING");
    }

    // 获取所有举报
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // 获取举报详情
    public Report getReportById(Long id) {
        return reportRepository.findById(id).orElse(null);
    }

    // 审核举报
    @Transactional
    public Report reviewReport(Long id, String status, String rejectReason) {
        Report report = getReportById(id);
        if (report == null) {
            logger.warning("审核举报失败：找不到ID为" + id + "的举报");
            return null;
        }

        // 检查当前状态，只有PENDING的举报才能被审核
        if (!"PENDING".equals(report.getStatus())) {
            logger.warning("审核举报失败：ID为" + id + "的举报状态不是PENDING，当前状态：" + report.getStatus());
            return null;
        }

        report.setStatus(status);

        if ("REJECTED".equals(status)) {
            report.setRejectReason(rejectReason);
            logger.info("举报被驳回，ID：" + id + "，驳回原因：" + rejectReason);
        } else if ("APPROVED".equals(status)) {
            try {
                // 审核通过，扣除被举报人20积分，奖励举报人10积分
                User reportedUser = userService.increaseUserCredit(report.getReportedUserId(), -20); // 扣除20分
                User reporter = userService.increaseUserCredit(report.getReporterId(), 10); // 奖励10分

                logger.info("举报被通过，ID：" + id +
                        "，被举报用户ID：" + report.getReportedUserId() + "，当前积分："
                        + (reportedUser != null ? reportedUser.getCredit() : "未知") +
                        "，举报人ID：" + report.getReporterId() + "，当前积分："
                        + (reporter != null ? reporter.getCredit() : "未知"));
            } catch (Exception e) {
                logger.severe("更新用户积分失败：" + e.getMessage());
                throw e; // 重新抛出异常，让事务回滚
            }
        }

        return reportRepository.save(report);
    }
}