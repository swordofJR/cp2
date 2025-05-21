package com.copyright.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取上传目录的绝对路径
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

        // 确保目录存在
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 添加资源映射：/api/uploads/** -> file:/实际路径/uploads/
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + uploadDir + File.separator);

        // 打印映射信息便于调试
        System.out.println("添加资源映射: /api/uploads/** -> " + "file:" + uploadDir + File.separator);
    }
}