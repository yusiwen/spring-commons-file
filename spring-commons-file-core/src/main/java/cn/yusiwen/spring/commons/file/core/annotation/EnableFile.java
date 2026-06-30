package cn.yusiwen.spring.commons.file.core.annotation;

import cn.yusiwen.spring.commons.file.core.config.FileAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(FileAutoConfiguration.class)
public @interface EnableFile {
}
