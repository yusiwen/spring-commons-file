package cn.yusiwen.spring.commons.file.starter;

import cn.yusiwen.spring.commons.file.core.config.FileAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FileAutoConfiguration.class)
public class FileStarterAutoConfiguration {
}
