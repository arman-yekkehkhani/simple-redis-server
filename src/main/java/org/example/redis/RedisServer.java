package org.example.redis;

import org.example.cache.Cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class RedisServer {

    private final List<Cache<String, String>> databases;
    private final List<Map<String, Long>> expirations = Collections.synchronizedList(new ArrayList<>());
    private int dbIdx;

    private RedisServer() {
        databases = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            databases.add(new Cache<>(3));
            expirations.add(new ConcurrentHashMap<>());
        }

        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(() -> {
            for (int i = 0; i < expirations.size(); i++) {
                Cache<String, String> db = databases.get(i);
                Map<String, Long> dbExpirations = expirations.get(i);
                for (String key : dbExpirations.keySet()) {
                    if (dbExpirations.get(key) < new Date().getTime()) {
                        db.remove(key);
                        dbExpirations.remove(key);
                    }
                }
            }
        }, 100, 100, TimeUnit.MILLISECONDS);

    }

    private static RedisServer instance;

    public static RedisServer getInstance() {
        if (instance == null)
            instance = new RedisServer();
        return instance;
    }

    public String put(String key, String value) {
        if (value == null)
            throw new IllegalArgumentException("value cannot be null!");
        return databases.get(dbIdx).put(key, value);
    }

    public String get(String key) {
        return databases.get(dbIdx).get(key);
    }

    public int delete(String... keys) {
        int deletedCount = 0;
        for (String key : keys) {
            expirations.get(dbIdx).remove(key);
            if (databases.get(dbIdx).containsKey(key))
                deletedCount++;
            databases.get(dbIdx).remove(key);
        }
        return deletedCount;
    }

    public void setExpire(String key, int seconds) {
        long expirationTime = new Date().getTime() + seconds * 1000L;
        expirations.get(dbIdx).put(key, expirationTime);
    }

    public long countExistingKeys(String... keys) {
        return Arrays.stream(keys).filter(k -> databases.get(dbIdx).containsKey(k)).count();
    }

    public long increment(String key) {
        return incrementBy(key, 1L);
    }

    public long incrementBy(String key, Long increment) {
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

    public String getKeys(String regex) {
        Pattern pattern = Pattern.compile(regex);

        return StreamSupport.stream(databases.get(dbIdx).keys().spliterator(), false)
                .filter((k) -> pattern.matcher(k).matches())
                .reduce(" ", (k1, k2) -> String.format("%s\n%s", k1, k2));
    }

    public int getDbIdx() {
        return dbIdx;
    }

    public int setDbIdx(int dbIdx) {
        this.dbIdx = dbIdx;
        return this.dbIdx;
    }
}
