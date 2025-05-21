package com.copyright.service;

import com.copyright.entity.Copyright;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CopyrightJdbcService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads";

    public CopyrightJdbcService() {
        // 创建上传目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Copyright uploadCopyright(MultipartFile file, String title, String description, String category,
            String ownerAddress, Long userId) throws IOException {
        // 保存文件
        String uniqueFileName = saveFile(file);

        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 插入数据库
        String sql = "INSERT INTO copyrights (title, description, img_url, category, status, owner_address, user_id, created_time, updated_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, title, description, uniqueFileName, category, "PENDING", ownerAddress, userId, now,
                now);

        // 获取刚刚插入的记录的ID
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // 创建版权对象
        Copyright copyright = new Copyright();
        copyright.setId(id);
        copyright.setTitle(title);
        copyright.setDescription(description);
        copyright.setImgUrl(uniqueFileName);
        copyright.setCategory(category);
        copyright.setStatus("PENDING");
        copyright.setOwnerAddress(ownerAddress);
        copyright.setUserId(userId);
        copyright.setCreatedTime(now);
        copyright.setUpdatedTime(now);

        return copyright;
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
        String sql = "SELECT * FROM copyrights";
        return jdbcTemplate.query(sql, new CopyrightRowMapper());
    }

    public List<Map<String, Object>> getAllCopyrightsWithUsernames() {
        String sql = "SELECT c.*, u.username FROM copyrights c LEFT JOIN users u ON c.user_id = u.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            Copyright copyright = new CopyrightRowMapper().mapRow(rs, rowNum);

            // 将Copyright对象的所有属性复制到Map中
            result.put("id", copyright.getId());
            result.put("title", copyright.getTitle());
            result.put("description", copyright.getDescription());
            result.put("imgUrl", copyright.getImgUrl());
            result.put("category", copyright.getCategory());
            result.put("status", copyright.getStatus());
            result.put("ownerAddress", copyright.getOwnerAddress());
            result.put("userId", copyright.getUserId());
            result.put("price", copyright.getPrice());
            result.put("reason", copyright.getReason());
            result.put("createdTime", copyright.getCreatedTime());
            result.put("updatedTime", copyright.getUpdatedTime());

            // 添加用户名
            result.put("username", rs.getString("username"));

            return result;
        });
    }

    public List<Copyright> getPendingCopyrights() {
        String sql = "SELECT * FROM copyrights WHERE status = 'PENDING'";
        return jdbcTemplate.query(sql, new CopyrightRowMapper());
    }

    public List<Copyright> getUserCopyrights(String ownerAddress) {
        String sql = "SELECT * FROM copyrights WHERE owner_address = ?";
        return jdbcTemplate.query(sql, new CopyrightRowMapper(), ownerAddress);
    }

    public List<Map<String, Object>> getUserCopyrightsWithUsername(String ownerAddress) {
        String sql = "SELECT c.*, u.username FROM copyrights c LEFT JOIN users u ON c.user_id = u.id WHERE c.owner_address = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            Copyright copyright = new CopyrightRowMapper().mapRow(rs, rowNum);

            // 将Copyright对象的所有属性复制到Map中
            result.put("id", copyright.getId());
            result.put("title", copyright.getTitle());
            result.put("description", copyright.getDescription());
            result.put("imgUrl", copyright.getImgUrl());
            result.put("category", copyright.getCategory());
            result.put("status", copyright.getStatus());
            result.put("ownerAddress", copyright.getOwnerAddress());
            result.put("userId", copyright.getUserId());
            result.put("price", copyright.getPrice());
            result.put("reason", copyright.getReason());
            result.put("createdTime", copyright.getCreatedTime());
            result.put("updatedTime", copyright.getUpdatedTime());

            // 添加用户名
            result.put("username", rs.getString("username"));

            return result;
        }, ownerAddress);
    }

    public List<Copyright> getUserCopyrightsByUserId(Long userId) {
        String sql = "SELECT * FROM copyrights WHERE user_id = ?";
        return jdbcTemplate.query(sql, new CopyrightRowMapper(), userId);
    }

    public List<Map<String, Object>> getUserCopyrightsByUserIdWithUsername(Long userId) {
        String sql = "SELECT c.*, u.username FROM copyrights c LEFT JOIN users u ON c.user_id = u.id WHERE c.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            Copyright copyright = new CopyrightRowMapper().mapRow(rs, rowNum);

            // 将Copyright对象的所有属性复制到Map中
            result.put("id", copyright.getId());
            result.put("title", copyright.getTitle());
            result.put("description", copyright.getDescription());
            result.put("imgUrl", copyright.getImgUrl());
            result.put("category", copyright.getCategory());
            result.put("status", copyright.getStatus());
            result.put("ownerAddress", copyright.getOwnerAddress());
            result.put("userId", copyright.getUserId());
            result.put("price", copyright.getPrice());
            result.put("reason", copyright.getReason());
            result.put("createdTime", copyright.getCreatedTime());
            result.put("updatedTime", copyright.getUpdatedTime());

            // 添加用户名
            result.put("username", rs.getString("username"));

            return result;
        }, userId);
    }

    public List<Copyright> getMarketplaceCopyrights() {
        String sql = "SELECT * FROM copyrights WHERE status = 'LISTED'";
        return jdbcTemplate.query(sql, new CopyrightRowMapper());
    }

    public List<Map<String, Object>> getMarketplaceCopyrightsWithUsernames() {
        String sql = "SELECT c.*, u.username FROM copyrights c LEFT JOIN users u ON c.user_id = u.id WHERE c.status = 'LISTED'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            Copyright copyright = new CopyrightRowMapper().mapRow(rs, rowNum);

            // 将Copyright对象的所有属性复制到Map中
            result.put("id", copyright.getId());
            result.put("title", copyright.getTitle());
            result.put("description", copyright.getDescription());
            result.put("imgUrl", copyright.getImgUrl());
            result.put("category", copyright.getCategory());
            result.put("status", copyright.getStatus());
            result.put("ownerAddress", copyright.getOwnerAddress());
            result.put("userId", copyright.getUserId());
            result.put("price", copyright.getPrice());
            result.put("reason", copyright.getReason());
            result.put("createdTime", copyright.getCreatedTime());
            result.put("updatedTime", copyright.getUpdatedTime());

            // 添加用户名
            result.put("username", rs.getString("username"));

            return result;
        });
    }

    public Copyright getCopyright(Long id) {
        String sql = "SELECT * FROM copyrights WHERE id = ?";
        List<Copyright> copyrights = jdbcTemplate.query(sql, new CopyrightRowMapper(), id);
        return copyrights.isEmpty() ? null : copyrights.get(0);
    }

    public Copyright reviewCopyright(Long id, String status, String reason) {
        String sql = "UPDATE copyrights SET status = ?, reason = ?, updated_time = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, reason, LocalDateTime.now(), id);
        return getCopyright(id);
    }

    public Copyright listCopyright(Long id, BigDecimal price) {
        String sql = "UPDATE copyrights SET status = 'LISTED', price = ?, updated_time = ? WHERE id = ? AND status = 'APPROVED'";
        int updated = jdbcTemplate.update(sql, price, LocalDateTime.now(), id);
        return updated > 0 ? getCopyright(id) : null;
    }

    public Copyright purchaseCopyright(Long id, String newOwnerAddress, Long newUserId) {
        // 首先获取原始版权信息
        Copyright originalCopyright = getCopyright(id);
        if (originalCopyright == null || !"LISTED".equals(originalCopyright.getStatus())) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 更新原版权为SOLD状态，保留给卖家
        String updateSql = "UPDATE copyrights SET status = 'SOLD', updated_time = ? WHERE id = ? AND status = 'LISTED'";
        int updated = jdbcTemplate.update(updateSql, now, id);

        if (updated <= 0) {
            return null;
        }

        // 2. 创建一个新的版权副本给买家，状态为PENDING
        String insertSql = "INSERT INTO copyrights (title, description, img_url, category, status, owner_address, user_id, price, created_time, updated_time) "
                +
                "VALUES (?, ?, ?, ?, 'PENDING', ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                insertSql,
                originalCopyright.getTitle(),
                originalCopyright.getDescription(),
                originalCopyright.getImgUrl(),
                originalCopyright.getCategory(),
                newOwnerAddress,
                newUserId,
                originalCopyright.getPrice(),
                now,
                now);

        // 获取新插入的买家版权ID
        Long newCopyrightId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // 返回买家的新版权
        return getCopyright(newCopyrightId);
    }

    public Copyright delistCopyright(Long id) {
        String sql = "UPDATE copyrights SET status = 'DELISTED', updated_time = ? WHERE id = ? AND status = 'LISTED'";
        int updated = jdbcTemplate.update(sql, LocalDateTime.now(), id);
        return updated > 0 ? getCopyright(id) : null;
    }

    private static class CopyrightRowMapper implements RowMapper<Copyright> {
        @Override
        public Copyright mapRow(ResultSet rs, int rowNum) throws SQLException {
            Copyright copyright = new Copyright();
            copyright.setId(rs.getLong("id"));
            copyright.setTitle(rs.getString("title"));
            copyright.setDescription(rs.getString("description"));
            copyright.setImgUrl(rs.getString("img_url"));
            copyright.setCategory(rs.getString("category"));
            copyright.setStatus(rs.getString("status"));
            copyright.setOwnerAddress(rs.getString("owner_address"));
            copyright.setUserId(rs.getLong("user_id"));
            copyright.setPrice(rs.getBigDecimal("price"));
            copyright.setReason(rs.getString("reason"));
            copyright.setCreatedTime(rs.getTimestamp("created_time").toLocalDateTime());
            copyright.setUpdatedTime(rs.getTimestamp("updated_time").toLocalDateTime());
            return copyright;
        }
    }
}