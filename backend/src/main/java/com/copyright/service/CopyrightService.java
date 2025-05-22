package com.copyright.service;

import com.copyright.entity.Copyright;
import com.copyright.repository.CopyrightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CopyrightService {

    private static final Logger logger = LoggerFactory.getLogger(CopyrightService.class);

    @Autowired
    private CopyrightRepository copyrightRepository;

    @Autowired
    private IpfsService ipfsService;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads";

    public CopyrightService() {
        // 创建上传目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Copyright uploadCopyright(MultipartFile file, String title, String description, String category,
            String ownerAddress, Long userId) throws IOException {
        // 保存文件到本地
        String uniqueFileName = saveFile(file);

        // 上传文件到IPFS（如果IPFS服务可用）
        String ipfsHash = ipfsService.uploadFile(file);
        logger.info("文件上传完成 - 本地路径: {}, IPFS哈希: {}", uniqueFileName,
                ipfsHash.isEmpty() ? "未上传到IPFS" : ipfsHash);

        // 创建版权对象
        Copyright copyright = new Copyright();
        copyright.setTitle(title);
        copyright.setDescription(description);
        copyright.setImgUrl(uniqueFileName);

        // 只有当IPFS哈希不为空时才设置
        if (!ipfsHash.isEmpty()) {
            copyright.setIpfsHash(ipfsHash);
        }

        copyright.setCategory(category);
        copyright.setStatus("PENDING"); // 默认状态为待审核
        copyright.setOwnerAddress(ownerAddress);
        copyright.setUserId(userId);

        // 保存到数据库
        return copyrightRepository.save(copyright);
    }

    private String saveFile(MultipartFile file) throws IOException {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.write(filePath, file.getBytes());

        return uniqueFileName;
    }

    public List<Copyright> getAllCopyrights() {
        return copyrightRepository.findAll();
    }

    public List<Copyright> getPendingCopyrights() {
        return copyrightRepository.findByStatus("PENDING");
    }

    public List<Copyright> getUserCopyrights(String ownerAddress) {
        return copyrightRepository.findByOwnerAddress(ownerAddress);
    }

    public List<Copyright> getMarketplaceCopyrights() {
        return copyrightRepository.findByStatus("LISTED");
    }

    public Copyright getCopyright(Long id) {
        return copyrightRepository.findById(id).orElse(null);
    }

    public Copyright reviewCopyright(Long id, String status, String reason) {
        Copyright copyright = getCopyright(id);
        if (copyright != null) {
            copyright.setStatus(status);
            copyright.setReason(reason);
            return copyrightRepository.save(copyright);
        }
        return null;
    }

    public Copyright listCopyright(Long id, BigDecimal price) {
        Copyright copyright = getCopyright(id);
        if (copyright != null && "APPROVED".equals(copyright.getStatus())) {
            copyright.setStatus("LISTED");
            copyright.setPrice(price);
            return copyrightRepository.save(copyright);
        }
        return null;
    }

    public Copyright purchaseCopyright(Long id, String newOwnerAddress, Long newUserId) {
        Copyright copyright = getCopyright(id);
        if (copyright != null && "LISTED".equals(copyright.getStatus())) {
            copyright.setStatus("SOLD");
            copyright.setOwnerAddress(newOwnerAddress);
            copyright.setUserId(newUserId);
            return copyrightRepository.save(copyright);
        }
        return null;
    }
}