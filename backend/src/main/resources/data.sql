-- 添加管理员用户（用户名为root，密码为root）
INSERT INTO users (username, password, address, hash, deleted, created_time, updated_time)
VALUES ('root', 'root', '0x0000000000000000000000000000000000000000', 'roothash', 0, NOW(), NOW());

-- 添加普通用户
INSERT INTO users (username, password, address, hash, deleted, created_time, updated_time)
VALUES ('jr', '123456', '0x0000000000000000000000000000000000000000', 'roothash', 0, NOW(), NOW());
-- -- 添加测试数据
-- INSERT INTO copyrights (title, description, img_url, category, status, owner_address, user_id, price, created_time, updated_time)
-- VALUES ('测试版权1', '这是一个测试版权描述', 'test-image-1.jpg', '艺术', 'APPROVED', '0x0000000000000000000000000000000000000000', 1, 100.00, NOW(), NOW());

-- INSERT INTO copyrights (title, description, img_url, category, status, owner_address, user_id, price, created_time, updated_time)
-- VALUES ('测试版权2', '这是另一个测试版权描述', 'test-image-2.jpg', '音乐', 'PENDING', '0x0000000000000000000000000000000000000000', 1, null, NOW(), NOW());

-- INSERT INTO copyrights (title, description, img_url, category, status, owner_address, user_id, price, created_time, updated_time)
-- VALUES ('测试版权3', '这是一个已上架的测试版权', 'test-image-3.jpg', '视频', 'LISTED', '0x0000000000000000000000000000000000000000', 1, 200.00, NOW(), NOW());