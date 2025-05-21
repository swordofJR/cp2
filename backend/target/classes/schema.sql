-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS copyright_db;

-- 使用数据库
USE copyright_db;

-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  address VARCHAR(255) NOT NULL,
  hash VARCHAR(255) NOT NULL,
  deleted INT NOT NULL DEFAULT 0,
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建版权表（如果不存在）
CREATE TABLE IF NOT EXISTS copyrights (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  img_url VARCHAR(255) NOT NULL,
  category VARCHAR(100) NOT NULL,
  status VARCHAR(50) NOT NULL,
  owner_address VARCHAR(255) NOT NULL,
  user_id BIGINT,
  price DECIMAL(19,2),
  reason VARCHAR(255),
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 