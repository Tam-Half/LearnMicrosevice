package intern.lp.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Redis Sentinel configuration with password
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel(new RedisNode("redis-sentinel-node-0.redis-sentinel-headless.storage-redis.svc.cluster.local", 26379))
                .sentinel(new RedisNode("redis-sentinel-node-1.redis-sentinel-headless.storage-redis.svc.cluster.local", 26379))
                .sentinel(new RedisNode("redis-sentinel-node-2.redis-sentinel-headless.storage-redis.svc.cluster.local", 26379));

        sentinelConfig.setPassword(RedisPassword.of("redispsswd"));
        sentinelConfig.setSentinelPassword(RedisPassword.of("redispsswd"));

        return new LettuceConnectionFactory(sentinelConfig);
    }

    /**
     * Lettuce client configuration
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return clientConfigurationBuilder -> {
            SocketOptions socketOptions = SocketOptions.builder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .keepAlive(true)
                    .build();

            TimeoutOptions timeoutOptions = TimeoutOptions.builder()
                    .fixedTimeout(Duration.ofSeconds(10))
                    .build();

            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(socketOptions)
                    .timeoutOptions(timeoutOptions)
                    .autoReconnect(true)
                    .build();

            clientConfigurationBuilder
                    .clientOptions(clientOptions)
                    .readFrom(ReadFrom.REPLICA_PREFERRED)
                    .commandTimeout(Duration.ofSeconds(10));
        };
    }

    /**
     * RedisTemplate for manual Redis operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        ObjectMapper objectMapper = new ObjectMapper();
        // Use modern polymorphic type validator
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * CacheManager for @Cacheable
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues()
                .prefixCacheNameWith("app:");

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}
