package cn.yusiwen.spring.commons.file.test;

import cn.yusiwen.spring.commons.file.core.operator.local.LocalFileOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileOperatorTest {

    private LocalFileOperator fileOperator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileOperator = new LocalFileOperator(tempDir.toString());
    }

    @Test
    void storageFileBytes() throws Exception {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        String path = fileOperator.storageFile(data, "test.txt");

        assertThat(path).isNotNull();
        assertThat(path).endsWith(".txt");
        assertThat(fileOperator.isExistingFile(path)).isTrue();
    }

    @Test
    void getFileBytes() throws Exception {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        String path = fileOperator.storageFile(data, "test.txt");

        byte[] retrieved = fileOperator.getFileBytes(path);
        assertThat(new String(retrieved, StandardCharsets.UTF_8)).isEqualTo("hello world");
    }

    @Test
    void deleteFile() throws Exception {
        byte[] data = "to be deleted".getBytes(StandardCharsets.UTF_8);
        String path = fileOperator.storageFile(data, "delete-me.txt");

        assertThat(fileOperator.isExistingFile(path)).isTrue();

        fileOperator.deleteFile(path);
        assertThat(fileOperator.isExistingFile(path)).isFalse();
    }

    @Test
    void isExistingFile_returnsFalseForNonExistent() {
        assertThat(fileOperator.isExistingFile("nonexistent.txt")).isFalse();
    }
}
