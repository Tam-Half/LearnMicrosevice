package intern.lp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    // ==============================
    // 🔹 Redis Sentinel Config
    // ==============================
    @Primary
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel(new RedisNode("redis-sentinel-node-0.redis-sentinel-headless.storage-redis.svc.cluster.local", 26379))
                .sentinel(new RedisNode("redis-sentinel-node-1.redis-sentinel-headless.storage-redis.svc.cluster.local", 26379))
                .sentinel(new RedisNode("redis-sentinel-node-2.redis-sentinel-headless.storage-redis.svc.cluster.local", 26379));

        sentinelConfig.setPassword(RedisPassword.of("redispsswd"));
        sentinelConfig.setSentinelPassword(RedisPassword.of("redispsswd"));

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(10))
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .clientOptions(ClientOptions.builder()
                        .autoReconnect(true)
                        .socketOptions(SocketOptions.builder()
                                .connectTimeout(Duration.ofSeconds(10))
                                .keepAlive(true)
                                .build())
                        .timeoutOptions(TimeoutOptions.builder()
                                .fixedTimeout(Duration.ofSeconds(10))
                                .build())
                        .build())
                .clientResources(clientResources())
                .build();

        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.create();
    }

    // ==============================
    // 🔹 ObjectMapper for Redis (FIXED)
    // ==============================
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Tự động đăng ký modules (JavaTimeModule, etc.)
        mapper.findAndRegisterModules();

        // Tắt serialization timestamps cho Date/Time
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // KHÔNG dùng activateDefaultTyping() để tránh lỗi type mismatch
        // Chỉ serialize pure JSON, không thêm type information

        return mapper;
    }

    // ==============================
    // 🔹 RedisTemplate
    // ==============================
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    // ==============================
    // 🔹 CacheManager
    // ==============================
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                )
                .disableCachingNullValues()
                .prefixCacheNameWith("app:");

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}