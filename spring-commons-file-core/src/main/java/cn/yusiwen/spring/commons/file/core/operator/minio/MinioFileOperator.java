package cn.yusiwen.spring.commons.file.core.operator.minio;

import cn.yusiwen.spring.commons.file.core.enums.BucketAuthEnum;
import cn.yusiwen.spring.commons.file.core.exception.FileServiceException;
import cn.yusiwen.spring.commons.file.core.operator.FileOperator;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MinioFileOperator implements FileOperator {

    private static final Logger log = LoggerFactory.getLogger(MinioFileOperator.class);

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public MinioFileOperator(MinioConfig minioConfig) {
        this.minioConfig = minioConfig;
        this.minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
        initBucket();
    }

    public MinioFileOperator(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
        initBucket();
    }

    public MinioClient getClient() {
        return minioClient;
    }

    private void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
                log.info("Created MinIO bucket: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            throw new FileServiceException("Failed to initialize MinIO bucket", e);
        }
    }

    @Override
    public boolean isExistingFile(String filePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String storageFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        return putObject(objectName, file.getInputStream(), file.getSize(), file.getContentType());
    }

    @Override
    public String storageFile(byte[] data, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        return putObject(objectName, new ByteArrayInputStream(data), data.length, null);
    }

    @Override
    public String storageFile(InputStream inputStream, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        return putObject(objectName, inputStream, 0, null);
    }

    private String putObject(String objectName, InputStream stream, long size, String contentType) {
        try {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(stream, size, -1);
            if (contentType != null) {
                builder.contentType(contentType);
            }
            minioClient.putObject(builder.build());
            log.info("File stored in MinIO: {}/{}", minioConfig.getBucketName(), objectName);
            return objectName;
        } catch (Exception e) {
            throw new FileServiceException("Failed to store file in MinIO", e);
        }
    }

    @Override
    public byte[] getFileBytes(String filePath) throws IOException {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(filePath)
                        .build())) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int n;
            while ((n = stream.read(data)) != -1) {
                buffer.write(data, 0, n);
            }
            return buffer.toByteArray();
        } catch (Exception e) {
            throw new FileServiceException("Failed to read file from MinIO", e);
        }
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .build());
            log.info("File deleted from MinIO: {}/{}", minioConfig.getBucketName(), filePath);
        } catch (Exception e) {
            throw new FileServiceException("Failed to delete file from MinIO", e);
        }
    }

    @Override
    public String getFileAuthUrl(String filePath) throws IOException {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            throw new FileServiceException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        try {
            return minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void setBucketAcl(String bucketName, BucketAuthEnum auth) {
    }
}
