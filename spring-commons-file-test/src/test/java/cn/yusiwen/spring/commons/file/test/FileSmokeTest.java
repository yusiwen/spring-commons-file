package cn.yusiwen.spring.commons.file.test;

import cn.yusiwen.spring.commons.file.core.operator.FileOperator;
import cn.yusiwen.spring.commons.file.core.operator.local.LocalFileOperator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FileSmokeTest {

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private FileOperator fileOperator;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void defaultFileOperatorIsLocal() {
        assertThat(fileOperator).isNotNull();
        assertThat(fileOperator).isInstanceOf(LocalFileOperator.class);
    }

    @Test
    void localFileOperatorGetStoragePath() {
        LocalFileOperator local = new LocalFileOperator("/tmp/test-upload");
        assertThat(local.getStoragePath()).isEqualTo("/tmp/test-upload");
    }
}
