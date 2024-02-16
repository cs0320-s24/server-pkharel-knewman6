package edu.brown.cs.student.main.server.Caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class BroadbandCache {
  private final Cache<String, String> cache;

  public BroadbandCache(int timeAmount, TimeUnit timeUnit, int maxSize) {
    cache =
            CacheBuilder.newBuilder().expireAfterWrite(timeAmount, timeUnit).maximumSize(maxSize).recordStats().build();
  }

  public void putData(String stateCode, String county, String data) {
    String key = generateKey(stateCode, county);
    cache.put(key, data);
  }

  public String getData(String stateCode, String county) {
    String key = generateKey(stateCode, county);
    return cache.getIfPresent(key);
  }

  private static String generateKey(String stateCode, String county) {
    return stateCode + "-" + county;
  }
}
