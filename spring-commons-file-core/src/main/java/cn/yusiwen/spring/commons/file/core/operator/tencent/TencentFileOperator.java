package cn.yusiwen.spring.commons.file.core.operator.tencent;

import cn.yusiwen.spring.commons.file.core.enums.BucketAuthEnum;
import cn.yusiwen.spring.commons.file.core.exception.FileServiceException;
import cn.yusiwen.spring.commons.file.core.operator.FileOperator;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TencentFileOperator implements FileOperator {

    private static final Logger log = LoggerFactory.getLogger(TencentFileOperator.class);

    private final COSClient cosClient;
    private final TransferManager transferManager;
    private final String bucketName;
    private final String region;

    public TencentFileOperator(String region, String secretId, String secretKey, String bucketName) {
        this.region = region;
        this.bucketName = bucketName;
        BasicCOSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        this.cosClient = new COSClient(cred, clientConfig);
        ExecutorService executor = Executors.newFixedThreadPool(32);
        this.transferManager = new TransferManager(cosClient, executor);
        initBucket();
    }

    public COSClient getClient() {
        return cosClient;
    }

    private void initBucket() {
        try {
            if (!cosClient.doesBucketExist(bucketName)) {
                CreateBucketRequest request = new CreateBucketRequest(bucketName);
                request.setCannedAcl(CannedAccessControlList.Private);
                cosClient.createBucket(request);
                log.info("Created COS bucket: {}", bucketName);
            }
        } catch (Exception e) {
            throw new FileServiceException("Failed to initialize COS bucket", e);
        }
    }

    private String getKey(String filePath) {
        return filePath;
    }

    @Override
    public boolean isExistingFile(String filePath) {
        return cosClient.doesObjectExist(bucketName, getKey(filePath));
    }

    @Override
    public String storageFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType(new MimetypesFileTypeMap().getContentType(originalName));
        cosClient.putObject(bucketName, objectName, file.getInputStream(), meta);
        log.info("File stored in COS: {}/{}", bucketName, objectName);
        return objectName;
    }

    @Override
    public String storageFile(byte[] data, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(data.length);
        cosClient.putObject(bucketName, objectName, new ByteArrayInputStream(data), meta);
        log.info("File stored in COS: {}/{}", bucketName, objectName);
        return objectName;
    }

    @Override
    public String storageFile(InputStream inputStream, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;
        cosClient.putObject(bucketName, objectName, inputStream, null);
        log.info("File stored in COS: {}/{}", bucketName, objectName);
        return objectName;
    }

    @Override
    public byte[] getFileBytes(String filePath) throws IOException {
        COSObject cosObject = cosClient.getObject(bucketName, getKey(filePath));
        try (InputStream in = cosObject.getObjectContent()) {
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
        cosClient.deleteObject(bucketName, getKey(filePath));
        log.info("File deleted from COS: {}/{}", bucketName, filePath);
    }

    @Override
    public String getFileAuthUrl(String filePath) throws IOException {
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000L);
        URL url = cosClient.generatePresignedUrl(bucketName, getKey(filePath), expiration);
        return url.toString();
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        return cosClient.doesBucketExist(bucketName);
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
        cosClient.setBucketAcl(bucketName, acl);
    }
}
