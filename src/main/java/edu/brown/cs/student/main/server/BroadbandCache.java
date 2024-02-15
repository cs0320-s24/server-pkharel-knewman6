package edu.brown.cs.student.main.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class BroadbandCache {
    private static final Cache<String, String> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS) // Example: expire after 1 hour
                .maximumSize(100) // Example: limit the cache size to 100 entries
                .build();
    }

    public static void putData(String stateCode, String county, String data) {
        String key = generateKey(stateCode, county);
        cache.put(key, data);
        printCache();
    }

    public static String getData(String stateCode, String county) {
        String key = generateKey(stateCode, county);
        System.out.println("SUCCESS");
        return cache.getIfPresent(key);
    }

    private static String generateKey(String stateCode, String county) {
        return stateCode + "-" + county;
    }

    //testing purposes
    private static void printCache() {
        System.out.println("Current cache content:");
        for (String key : cache.asMap().keySet()) {
            System.out.println("Key: " + key + ", Value: " + cache.getIfPresent(key));
        }
        System.out.println();
    }
}
