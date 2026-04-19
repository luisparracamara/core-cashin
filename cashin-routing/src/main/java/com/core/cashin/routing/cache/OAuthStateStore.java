package com.core.cashin.routing.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class OAuthStateStore {

    private static final String KEY_PREFIX = "oauth:state:";

    @Value("${oauth.state.ttl-minutes:10}")
    private long ttlMinutes;

    private final StringRedisTemplate redisTemplate;

    public OAuthStateStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String state) {
        redisTemplate.opsForValue().set(KEY_PREFIX + state, "1", Duration.ofMinutes(ttlMinutes));
    }

    public boolean consumeIfValid(String state) {
        Boolean deleted = redisTemplate.delete(KEY_PREFIX + state);
        return Boolean.TRUE.equals(deleted);
    }

}
