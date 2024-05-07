package com.hong.ForPaw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // 데이터 저장 (유효 기간 존재)
    public void storeValue(String type, String id, String value, Long expirationTime) {
        redisTemplate.opsForValue().set(buildKey(type, id), value, expirationTime, TimeUnit.MILLISECONDS);
    }

    // 유효 기간 X
    public void storeValue(String type, String id, String value) {
        redisTemplate.opsForValue().set(buildKey(type, id), value);
    }

    public void addSetElement(String key, Long userId) {
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        setOps.add(key, String.valueOf(userId));
    }

    public void addSetElement(String key, Long userId, Long expirationTime) {
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        setOps.add(key, String.valueOf(userId));
        redisTemplate.expire(key, expirationTime, TimeUnit.SECONDS);
    }

    public void incrementCnt(String type, String id, Long cnt){
        redisTemplate.opsForValue().increment(buildKey(type, id), cnt);
    }

    public void decrementCnt(String type, String id, Long cnt){
        redisTemplate.opsForValue().decrement(buildKey(type, id), cnt);
    }

    public void setExpireDate(String type, String id, Long expirationTime){
        redisTemplate.expire(buildKey(type, id), expirationTime, TimeUnit.MILLISECONDS);
    }

    // 데이터 존재 여부
    public boolean isDateExist(String type, String id) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(type, id)));
    }

    // 저장된 데이터와 일치여부 비교
    public boolean validateData(String type, String id, String value){
        String storedData = redisTemplate.opsForValue().get(buildKey(type, id));

        if(storedData == null){
            return false;
        }
        return storedData.equals(value);
    }

    // 데이터 삭제
    public void removeData(String type, String id) { redisTemplate.delete(buildKey(type, id)); }

    public void removeData(String key) { redisTemplate.delete(key); }

    // 데이터 반환 - Long 반환
    public Long getDataInLong(String type, String id){
        String value = redisTemplate.opsForValue().get(buildKey(type, id));

        if(value == null) return 0L;
        return Long.valueOf(value);
    }

    public Long getDataInLongWithNull(String type, String id){
        String value = redisTemplate.opsForValue().get(buildKey(type, id));

        if(value != null) return Long.valueOf(value);
        else { return null;}
    }

    // 데이터 반환 - String 반환
    public String getDataInStr(String type, String id){ return redisTemplate.opsForValue().get(buildKey(type, id)); }

    public Set<String> getMembersOfSet(String key) {
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        return setOps.members(key);
    }

    private String buildKey(String type, String id){
        return type + ":" + id;
    }

}