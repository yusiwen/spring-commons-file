package cn.yusiwen.spring.commons.file.core.operator.aliyun;

import cn.yusiwen.spring.commons.file.core.enums.BucketAuthEnum;
import cn.yusiwen.spring.commons.file.core.exception.FileServiceException;
import cn.yusiwen.spring.commons.file.core.operator.FileOperator;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class AliyunFileOperator implements FileOperator {

    private static final Logger log = LoggerFactory.getLogger(AliyunFileOperator.class);

    private final OSS ossClient;
    private final String bucketName;
    private final String endpoint;

    public AliyunFileOperator(String endpoint, String accessKeyId, String accessKeySecret, String bucketName) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        initBucket();
    }

    private void initBucket() {
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                ossClient.createBucket(bucketName);
                log.info("Created OSS bucket: {}", bucketName);
            }
        } catch (Exception e) {
            throw new FileServiceException("Failed to initialize OSS bucket", e);
        }
    }

    public OSS getClient() {
        return ossClient;
    }

    private String getKey(String filePath) {
        return filePath;
    }

    @Override
    public boolean isExistingFile(String filePath) {
        return ossClient.doesObjectExist(bucketName, getKey(filePath));
    }

    @Override
    public String storageFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        ossClient.putObject(bucketName, objectName, file.getInputStream());
        log.info("File stored in OSS: {}/{}", bucketName, objectName);
        return objectName;
    }

    @Override
    public String storageFile(byte[] data, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(data));
        log.info("File stored in OSS: {}/{}", bucketName, objectName);
        return objectName;
    }

    @Override
    public String storageFile(InputStream inputStream, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        ossClient.putObject(bucketName, objectName, inputStream);
        log.info("File stored in OSS: {}/{}", bucketName, objectName);
        return objectName;
    }

    @Override
    public byte[] getFileBytes(String filePath) throws IOException {
        OSSObject ossObject = ossClient.getObject(bucketName, getKey(filePath));
        try (InputStream in = ossObject.getObjectContent()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int n;
            while ((n = in.read(data)) != -1) {
                buffer.write(data, 0, n);
            }
            return buffer.toByteArray();
        }
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        ossClient.deleteObject(bucketName, getKey(filePath));
        log.info("File deleted from OSS: {}/{}", bucketName, filePath);
    }

    @Override
    public String getFileAuthUrl(String filePath) throws IOException {
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000L);
        URL url = ossClient.generatePresignedUrl(bucketName, getKey(filePath), expiration);
        return url.toString();
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        return ossClient.doesBucketExist(bucketName);
    }

    @Override
    public void setBucketAcl(String bucketName, BucketAuthEnum auth) {
        CannedAccessControlList acl;
        switch (auth) {
            case PUBLIC_READ:
                acl = CannedAccessControlList.PublicRead;
                break;
            case PUBLIC_READ_WRITE:
                acl = CannedAccessControlList.PublicReadWrite;
                break;
            default:
                acl = CannedAccessControlList.Private;
        }
        ossClient.setBucketAcl(bucketName, acl);
    }

    @Override
    public void setFileAcl(String filePath, BucketAuthEnum auth) {
        CannedAccessControlList acl;
        switch (auth) {
            case PUBLIC_READ:
                acl = CannedAccessControlList.PublicRead;
                break;
            case PUBLIC_READ_WRITE:
                acl = CannedAccessControlList.PublicReadWrite;
                break;
            default:
                acl = CannedAccessControlList.Private;
        }
        ossClient.setObjectAcl(bucketName, getKey(filePath), acl);
    }
}
