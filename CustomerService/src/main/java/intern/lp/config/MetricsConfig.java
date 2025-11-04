package intern.lp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Disable Tomcat Metrics để tránh lỗi cgroup v2
 * Chỉ cần để file này tồn tại với annotation @ConditionalOnProperty
 * hoặc XÓA FILE NÀY ĐI và dùng application.yml
 */
@Configuration
@ConditionalOnProperty(value = "management.metrics.enable.tomcat", havingValue = "false", matchIfMissing = true)
public class MetricsConfig {
}