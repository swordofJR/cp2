package com.copyright.service;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class IpfsService {

    private static final Logger logger = LoggerFactory.getLogger(IpfsService.class);
    private IPFS ipfs;
    private boolean ipfsAvailable = false;

    public IpfsService(@Value("${ipfs.host:localhost}") String ipfsHost,
            @Value("${ipfs.port:5001}") int ipfsPort) {
        try {
            this.ipfs = new IPFS(ipfsHost, ipfsPort);
            // 测试IPFS连接
            this.ipfs.version();
            this.ipfsAvailable = true;
            logger.info("IPFS 连接成功: {}:{}", ipfsHost, ipfsPort);
        } catch (Exception e) {
            // logger.warn("IPFS 连接失败: {}:{}，将使用本地存储", ipfsHost, ipfsPort);
            logger.debug("IPFS 连接异常: ", e);
        }
    }

    /**
     * 将文件上传到IPFS
     * 
     * @param file 要上传的文件
     * @return 返回IPFS中的哈希值（CID）或者空字符串（如果IPFS不可用）
     * @throws IOException 如果上传过程中发生错误
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (!ipfsAvailable) {
            logger.info("IPFS服务不可用");
            return "";
        }

        try {
            NamedStreamable.InputStreamWrapper fileWrapper = new NamedStreamable.InputStreamWrapper(
                    file.getOriginalFilename(), file.getInputStream());
            MerkleNode addResult = ipfs.add(fileWrapper).get(0);
            logger.info("文件上传到IPFS成功: {}", addResult.hash.toString());
            return addResult.hash.toString();
        } catch (Exception e) {
            logger.error("上传文件到IPFS失败: ", e);
            return "";
        }
    }
}