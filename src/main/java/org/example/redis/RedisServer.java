package org.example.redis;

import org.example.cache.Cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RedisServer extends BaseAbstractRedisServer {

    private RedisServer() {
        super();
    }

    private static RedisServer instance;

    public static RedisServer getInstance() {
        if (instance == null)
            instance = new RedisServer();
        return instance;
    }

    @Override
    String internalPut(String key, String value) {
        return databases.get(dbIdx).put(key, value);
    }

    @Override
    String internalGet(String key) {
        return databases.get(dbIdx).get(key);
    }

    @Override
    List<String> internalDelete(String... keys) {
        List<String> deletedKeys = new ArrayList<>();
        for (String key : keys) {
            expirations.get(dbIdx).remove(key);
            if (databases.get(dbIdx).containsKey(key))
                deletedKeys.add(key);
            databases.get(dbIdx).remove(key);
        }
        return deletedKeys;
    }

    @Override
    void internalSetExpire(String key, long expirationTime) {
        expirations.get(dbIdx).put(key, expirationTime);
    }

    public long countExistingKeys(String... keys) {
        return Arrays.stream(keys).filter(k -> databases.get(dbIdx).containsKey(k)).count();
    }

    public long increment(String key) {
        return incrementBy(key, 1L);
    }

    @Override
    long internalIncrementBy(String key, Long increment) {
        Cache<String, String> cache = databases.get(dbIdx);
        if (cache.containsKey(key)) {
            long value = Long.parseLong(cache.get(key));
            value += increment;
            cache.put(key, String.valueOf(value));
            return value;
        }
        cache.put(key, String.valueOf(increment));
        return increment;
    }

    @Override
    List<String> internalGetKeys(String regex) {
        Pattern pattern = Pattern.compile(regex);

        return StreamSupport.stream(databases.get(dbIdx).keys().spliterator(), false)
                .filter((k) -> pattern.matcher(k).matches())
                .collect(Collectors.toList());
    }

    public Long remainingTime(String key) {
        if (!databases.get(dbIdx).containsKey(key))
            return -2L;
        if (!expirations.get(dbIdx).containsKey(key))
            return -1L;
        return (expirations.get(dbIdx).get(key) - new Date().getTime()) / 1000;
    }
}
