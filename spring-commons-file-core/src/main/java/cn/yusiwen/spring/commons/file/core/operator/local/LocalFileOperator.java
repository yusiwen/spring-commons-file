package cn.yusiwen.spring.commons.file.core.operator.local;

import cn.yusiwen.spring.commons.file.core.operator.FileOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LocalFileOperator implements FileOperator {

    private static final Logger log = LoggerFactory.getLogger(LocalFileOperator.class);

    private final String basePath;

    public LocalFileOperator() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            this.basePath = "D:/upload";
        } else {
            this.basePath = "/data/upload";
        }
    }

    public LocalFileOperator(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public boolean isExistingFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return Files.exists(Paths.get(basePath, filePath));
    }

    @Override
    public String storageFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String newName = UUID.randomUUID().toString().replace("-", "") + suffix;
        Path dir = Paths.get(basePath);
        Files.createDirectories(dir);
        Path target = dir.resolve(newName);
        file.transferTo(target.toFile());
        log.info("File saved locally: {}", target);
        return newName;
    }

    @Override
    public String storageFile(byte[] data, String originalFileName) throws IOException {
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newName = UUID.randomUUID().toString().replace("-", "") + suffix;
        Path dir = Paths.get(basePath);
        Files.createDirectories(dir);
        Path target = dir.resolve(newName);
        Files.write(target, data);
        log.info("File saved locally: {}", target);
        return newName;
    }

    @Override
    public String storageFile(InputStream inputStream, String originalFileName) throws IOException {
        byte[] data = toByteArray(inputStream);
        return storageFile(data, originalFileName);
    }

    @Override
    public byte[] getFileBytes(String filePath) throws IOException {
        Path target = Paths.get(basePath, filePath);
        return Files.readAllBytes(target);
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        Path target = Paths.get(basePath, filePath);
        Files.deleteIfExists(target);
        log.info("File deleted locally: {}", target);
    }

    @Override
    public String getFileAuthUrl(String filePath) throws IOException {
        return Paths.get(basePath, filePath).toAbsolutePath().toString();
    }

    @Override
    public String getStoragePath() {
        return basePath;
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = inputStream.read(data)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }
}
