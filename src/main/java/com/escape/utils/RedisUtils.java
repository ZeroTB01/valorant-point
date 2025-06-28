package com.escape.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.Collection;

/**
 * Redis工具类
 * 封装常用的Redis操作
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@Component
public class RedisUtils {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置缓存
     */
    public void set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis设置缓存失败, key: {}, 错误: {}", key, e.getMessage());
        }
    }

    /**
     * 设置缓存并指定过期时间
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Redis设置缓存失败, key: {}, 错误: {}", key, e.getMessage());
        }
    }

    /**
     * 获取缓存
     */
    public String get(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis获取缓存失败, key: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
        } catch (Exception e) {
            log.error("Redis删除缓存失败, key: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 批量删除缓存
     */
    public long delete(Collection<String> keys) {
        try {
            Long count = stringRedisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis批量删除缓存失败, keys: {}, 错误: {}", keys, e.getMessage());
            return 0;
        }
    }

    /**
     * 检查key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis检查key失败, key: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 设置key的过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("Redis设置过期时间失败, key: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 获取key的过期时间
     */
    public long getExpire(String key, TimeUnit unit) {
        try {
            Long expire = stringRedisTemplate.getExpire(key, unit);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("Redis获取过期时间失败, key: {}, 错误: {}", key, e.getMessage());
            return -1;
        }
    }

    /**
     * 根据pattern获取所有匹配的key
     */
    public Set<String> keys(String pattern) {
        try {
            return stringRedisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Redis获取keys失败, pattern: {}, 错误: {}", pattern, e.getMessage());
            return null;
        }
    }

    /**
     * 原子性递增
     */
    public long increment(String key) {
        try {
            Long result = stringRedisTemplate.opsForValue().increment(key);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis递增失败, key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * 原子性递增指定值
     */
    public long increment(String key, long delta) {
        try {
            Long result = stringRedisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis递增失败, key: {}, delta: {}, 错误: {}", key, delta, e.getMessage());
            return 0;
        }
    }

    /**
     * 原子性递减
     */
    public long decrement(String key) {
        try {
            Long result = stringRedisTemplate.opsForValue().decrement(key);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis递减失败, key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * Hash操作 - 设置
     */
    public void hSet(String key, String field, String value) {
        try {
            stringRedisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis Hash设置失败, key: {}, field: {}, 错误: {}", key, field, e.getMessage());
        }
    }

    /**
     * Hash操作 - 获取
     */
    public String hGet(String key, String field) {
        try {
            Object value = stringRedisTemplate.opsForHash().get(key, field);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Redis Hash获取失败, key: {}, field: {}, 错误: {}", key, field, e.getMessage());
            return null;
        }
    }

    /**
     * List操作 - 左侧推入
     */
    public long leftPush(String key, String value) {
        try {
            Long result = stringRedisTemplate.opsForList().leftPush(key, value);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis List左推失败, key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * List操作 - 右侧推入
     */
    public long rightPush(String key, String value) {
        try {
            Long result = stringRedisTemplate.opsForList().rightPush(key, value);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis List右推失败, key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * Set操作 - 添加成员
     */
    public long sAdd(String key, String... values) {
        try {
            Long result = stringRedisTemplate.opsForSet().add(key, values);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis Set添加失败, key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    /**
     * Set操作 - 获取所有成员
     */
    public Set<String> sMembers(String key) {
        try {
            return stringRedisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis Set获取成员失败, key: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }
}