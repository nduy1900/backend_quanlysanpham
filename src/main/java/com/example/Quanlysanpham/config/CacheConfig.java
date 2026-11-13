package com.example.Quanlysanpham.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory) {

        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.Quanlysanpham.entity")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.math")
                .allowIfSubType("java.time")
                .build();

        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .build();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer));

        // Cấu hình riêng cho Product
        RedisCacheConfiguration productListConfig = defaultConfig.entryTtl(Duration.ofMinutes(5));
        RedisCacheConfiguration productDetailConfig = defaultConfig.entryTtl(Duration.ofMinutes(10));


        // Cấu hình riêng cho Supplier
        RedisCacheConfiguration suppliersConfig = defaultConfig.entryTtl(Duration.ofMinutes(5));
        RedisCacheConfiguration supplierDetailConfig = defaultConfig.entryTtl(Duration.ofMinutes(10));
        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();

        // Đưa các cache của Product vào
        configurations.put("productPages", defaultConfig);
        configurations.put("productList", productListConfig);
        configurations.put("productDetail", productDetailConfig);

        // Đưa các cache của Supplier vào
        configurations.put("suppliers", suppliersConfig);
        configurations.put("supplierDetail", supplierDetailConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configurations)
                .build();
    }
}
