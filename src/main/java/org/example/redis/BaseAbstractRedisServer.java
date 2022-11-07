package org.example.redis;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.example.cache.Cache;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseAbstractRedisServer {

    private static final String GET_CMD = "GET";
    private static final String PUT_CMD = "PUT";
    private static final String DEL_CMD = "DELETE";
    private static final String EXP_CMD = "EXPIRE";

    WriteAheadLogger logger = WriteAheadLogger.getInstance();

    List<Cache<String, String>> databases = Collections.synchronizedList(new ArrayList<>());
    List<Map<String, Long>> expirations = Collections.synchronizedList(new ArrayList<>());
    int dbIdx;

    public BaseAbstractRedisServer() {
        initDbs();
        loadWal();
        createExpiredCleaner();
    }

    private void initDbs() {
        databases = Collections.synchronizedList(new ArrayList<>());
        expirations = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 15; i++) {
            databases.add(new Cache<>(3));
            expirations.add(new ConcurrentHashMap<>());
        }
    }

    /**
     * load write_ahead logs
     */
    private void loadWal() {
        try (CSVReader csvReader = new CSVReader(new FileReader(WriteAheadLogger.LOG_PATH))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                Long expire = values[4].isEmpty() ? 0 : Long.parseLong(values[4]);
                handleSingleLog(Integer.parseInt(values[0]), values[1], values[2], values[3], expire);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleSingleLog(int dbIdx, String command, String key, String value, Long expirationTime) {
        setDbIdx(dbIdx);
        switch (command) {
            case PUT_CMD:
                internalPut(key, value);
                break;
            case GET_CMD:
                internalGet(key);
                break;
            case DEL_CMD:
                internalDelete(key);
                break;
            case EXP_CMD:
                internalSetExpire(key, expirationTime);
                break;
            default:
                throw new IllegalStateException("Encountered an error while reading from log files.");
        }
    }

    private void createExpiredCleaner() {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(() -> {
            for (int dbIdx = 0; dbIdx < expirations.size(); dbIdx++) {
                Cache<String, String> db = databases.get(dbIdx);
                Map<String, Long> dbExpirations = expirations.get(dbIdx);
                for (String key : dbExpirations.keySet()) {
                    if (dbExpirations.get(key) < new Date().getTime()) {
                        db.remove(key);
                        dbExpirations.remove(key);
                        logger.log(dbIdx, DEL_CMD, key, null, null);
                    }
                }
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    abstract String internalPut(String key, String value);

    public String put(String key, String value) {
        if (value == null)
            throw new IllegalArgumentException("value cannot be null!");

        logger.log(getDbIdx(), PUT_CMD, key, value, null);
        return internalPut(key, value);
    }

    abstract String internalGet(String key);

    public String get(String key) {
        logger.log(getDbIdx(), GET_CMD, key, null, null);
        return internalGet(key);
    }

    abstract List<String> internalDelete(String... keys);

    public int delete(String... keys) {
        List<String> deletedKeys = internalDelete(keys);
        for (String key : deletedKeys) {
            logger.log(getDbIdx(), DEL_CMD, key, null, null);
        }
        return deletedKeys.size();
    }

    abstract void internalSetExpire(String key, long expirationTime);

    public void setExpire(String key, int seconds) {
        long expirationTime = new Date().getTime() + seconds * 1000L;
        logger.log(getDbIdx(), EXP_CMD, key, null, expirationTime);
        internalSetExpire(key, expirationTime);
    }

    abstract long internalIncrementBy(String key, Long increment);

    public long incrementBy(String key, Long increment) {
        long value = internalIncrementBy(key, increment);
        logger.log(getDbIdx(), PUT_CMD, key, String.valueOf(value), null);
        return value;
    }

    abstract List<String> internalGetKeys(String regex);

    public String getKeys(String regex) {
        List<String> keys = internalGetKeys(regex);
        keys.forEach(k -> logger.log(getDbIdx(), GET_CMD, k, null, null));
        return keys.stream().reduce(" ", (k1, k2) -> String.format("%s\n%s", k1, k2));
    }

    public int getDbIdx() {
        return dbIdx;
    }

    public int setDbIdx(int dbIdx) {
        this.dbIdx = dbIdx;
        return this.dbIdx;
    }

    public void clear() {
        this.dbIdx = 0;
        initDbs();
        logger.clear();
    }
}
