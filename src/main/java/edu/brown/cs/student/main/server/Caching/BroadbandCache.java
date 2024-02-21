package edu.brown.cs.student.main.server.Caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.concurrent.TimeUnit;

public class BroadbandCache {
  private final Cache<String, String> cache;

  public BroadbandCache(int timeAmount, TimeUnit timeUnit, int maxSize) {
    cache =
            CacheBuilder.newBuilder().expireAfterWrite(timeAmount, timeUnit).maximumSize(maxSize).recordStats().build();
  }

  public void putData(String stateCode, String county, String data) {
    if(stateCode.equals("") || county.equals("") || data.equals(""))
      throw new NullPointerException("cannot store a null value");
    String key = generateKey(stateCode, county);
    cache.put(key, data);
  }

  public String getData(String stateCode, String county) {
    if(stateCode.equals("") || county.equals(""))
      throw new NullPointerException("cannot search a null value");
    String key = generateKey(stateCode, county);
    return cache.getIfPresent(key);
  }

  public void invalidateAll(){
    cache.invalidateAll();
  }

  public static String generateKey(String stateCode, String county) {
    return stateCode + "-" + county;
  }

  public CacheStats stats() {
    return cache.stats();
  }
}
