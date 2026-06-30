package cn.yusiwen.spring.commons.file.core.config;

import cn.yusiwen.spring.commons.file.core.operator.FileOperator;
import cn.yusiwen.spring.commons.file.core.operator.local.LocalFileOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("cn.yusiwen.spring.commons.file.core")
@ConditionalOnProperty(prefix = "commons.file", name = "enabled", matchIfMissing = true)
public class FileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FileOperator.class)
    public FileOperator fileOperator() {
        return new LocalFileOperator();
    }
}
