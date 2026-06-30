package cn.yusiwen.spring.commons.file.core.operator.huawei;

import cn.yusiwen.spring.commons.file.core.enums.BucketAuthEnum;
import cn.yusiwen.spring.commons.file.core.exception.FileServiceException;
import cn.yusiwen.spring.commons.file.core.operator.FileOperator;
import com.obs.services.ObsClient;
import com.obs.services.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

public class HuaweiFileOperator implements FileOperator {

    private static final Logger log = LoggerFactory.getLogger(HuaweiFileOperator.class);

    private final ObsClient obsClient;
    private final String bucketName;
    private final String storagePath;

    public HuaweiFileOperator(String endpoint, String ak, String sk, String bucketName, String storagePath) {
        this.bucketName = bucketName;
        this.storagePath = storagePath;
        this.obsClient = new ObsClient(ak, sk, endpoint);
        initBucket();
    }

    private void initBucket() {
        try {
            if (!obsClient.headBucket(bucketName)) {
                obsClient.createBucket(bucketName);
                log.info("Created OBS bucket: {}", bucketName);
            }
        } catch (Exception e) {
            throw new FileServiceException("Failed to initialize OBS bucket", e);
        }
    }

    public ObsClient getClient() {
        return obsClient;
    }

    private String getKey(String filePath) {
        return storagePath + filePath;
    }

    @Override
    public boolean isExistingFile(String filePath) {
        return obsClient.doesObjectExist(bucketName, getKey(filePath));
    }

    @Override
    public String storageFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        PutObjectResult result = obsClient.putObject(bucketName, getKey(objectName), file.getInputStream());
        log.info("File stored in OBS: {}/{} (etag: {})", bucketName, objectName, result.getEtag());
        return objectName;
    }

    @Override
    public String storageFile(byte[] data, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        PutObjectResult result = obsClient.putObject(bucketName, getKey(objectName), new ByteArrayInputStream(data));
        log.info("File stored in OBS: {}/{} (etag: {})", bucketName, objectName, result.getEtag());
        return objectName;
    }

    @Override
    public String storageFile(InputStream inputStream, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        PutObjectResult result = obsClient.putObject(bucketName, getKey(objectName), inputStream);
        log.info("File stored in OBS: {}/{} (etag: {})", bucketName, objectName, result.getEtag());
        return objectName;
    }

    @Override
    public byte[] getFileBytes(String filePath) throws IOException {
        ObsObject obsObject = obsClient.getObject(bucketName, getKey(filePath));
        try (InputStream in = obsObject.getObjectContent()) {
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
        obsClient.deleteObject(bucketName, getKey(filePath));
        log.info("File deleted from OBS: {}/{}", bucketName, filePath);
    }

    @Override
    public String getFileAuthUrl(String filePath) throws IOException {
        TemporarySignatureRequest request = new TemporarySignatureRequest();
        request.setMethod(HttpMethodEnum.GET);
        request.setBucketName(bucketName);
        request.setObjectKey(getKey(filePath));
        request.setExpires(3600);
        TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
        return response.getSignedUrl();
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        return obsClient.headBucket(bucketName);
    }

    @Override
    public void setBucketAcl(String bucketName, BucketAuthEnum auth) {
        AccessControlList acl = obsClient.getBucketAcl(bucketName);
        switch (auth) {
            case PUBLIC_READ:
                acl.grantPermission(GroupGrantee.ALL_USERS, Permission.PERMISSION_READ);
                break;
            case PUBLIC_READ_WRITE:
                acl.grantPermission(GroupGrantee.ALL_USERS, Permission.PERMISSION_READ);
                acl.grantPermission(GroupGrantee.ALL_USERS, Permission.PERMISSION_WRITE);
                break;
            default:
                break;
        }
        obsClient.setBucketAcl(bucketName, acl);
    }
}
