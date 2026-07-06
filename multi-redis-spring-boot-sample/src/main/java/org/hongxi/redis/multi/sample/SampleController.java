package org.hongxi.redis.multi.sample;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SampleController {

    private final RedisTemplate<String, Object> orderRedisTemplate;
    private final RedisTemplate<String, Object> userRedisTemplate;

    public SampleController(RedisTemplate<String, Object> orderRedisTemplate,
                            RedisTemplate<String, Object> userRedisTemplate) {
        this.orderRedisTemplate = orderRedisTemplate;
        this.userRedisTemplate = userRedisTemplate;
    }

    @GetMapping("/set")
    public Map<String, Object> set(@RequestParam String cluster,
                                   @RequestParam String key,
                                   @RequestParam String value) {
        RedisTemplate<String, Object> template = "order".equals(cluster)
                ? orderRedisTemplate : userRedisTemplate;
        template.opsForValue().set(key, value);
        Map<String, Object> result = new HashMap<>();
        result.put("cluster", cluster);
        result.put("key", key);
        result.put("value", value);
        return result;
    }

    @GetMapping("/get")
    public Map<String, Object> get(@RequestParam String cluster,
                                   @RequestParam String key) {
        RedisTemplate<String, Object> template = "order".equals(cluster)
                ? orderRedisTemplate : userRedisTemplate;
        Object value = template.opsForValue().get(key);
        Map<String, Object> result = new HashMap<>();
        result.put("cluster", cluster);
        result.put("key", key);
        result.put("value", value);
        return result;
    }
}
