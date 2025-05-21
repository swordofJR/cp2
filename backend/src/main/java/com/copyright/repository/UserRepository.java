package com.copyright.repository;

import com.copyright.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsernameAndDeleted(String username, int deleted);

    // 查找根据删除状态过滤的所有用户
    List<User> findAllByDeleted(int deleted);

    // 根据ID查找用户
    User findByIdAndDeleted(Long id, int deleted);
}