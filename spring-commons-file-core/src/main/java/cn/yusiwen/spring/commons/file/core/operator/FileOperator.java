package cn.yusiwen.spring.commons.file.core.operator;

import cn.yusiwen.spring.commons.file.core.enums.BucketAuthEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileOperator {

    default boolean isExistingFile(String filePath) throws IOException {
        return false;
    }

    String storageFile(MultipartFile file) throws IOException;

    String storageFile(byte[] data, String originalFileName) throws IOException;

    String storageFile(InputStream inputStream, String originalFileName) throws IOException;

    byte[] getFileBytes(String filePath) throws IOException;

    void deleteFile(String filePath) throws IOException;

    String getFileAuthUrl(String filePath) throws IOException;

    default boolean doesBucketExist(String bucketName) {
        return true;
    }

    default void setBucketAcl(String bucketName, BucketAuthEnum auth) {
    }

    default void setFileAcl(String filePath, BucketAuthEnum auth) {
    }

    default String getStoragePath() {
        return "";
    }
}
