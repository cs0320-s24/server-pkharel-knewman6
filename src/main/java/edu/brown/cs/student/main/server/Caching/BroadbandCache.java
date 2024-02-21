package edu.brown.cs.student.main.server.Caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.concurrent.TimeUnit;

/**
 * This class implements a caching mechanism for storing broadband data.
 * It uses a Guava Cache to temporarily store data about broadband speeds by state and county.
 * Entries in the cache are automatically removed after a param passed duration or trimmed
 * when the cache reaches its maximum size.
 */
public class BroadbandCache {
  private final Cache<String, String> cache;

  /**
   * Constructs a BroadbandCache with specified expiration time and maximum size.
   *
   * @param timeAmount the amount of time after which entries should expire
   * @param timeUnit the unit of time for the expiration amount
   * @param maxSize the maximum number of entries the cache can hold
   */
  public BroadbandCache(int timeAmount, TimeUnit timeUnit, int maxSize) {
    cache =
            CacheBuilder.newBuilder().expireAfterWrite(timeAmount, timeUnit).maximumSize(maxSize).recordStats().build();
  }

  /**
   * Stores the given broadband data in the cache.
   *
   * @param stateCode the state code to associate with the data
   * @param county the county name to associate with the data
   * @param data the broadband data to store
   * @throws NullPointerException if any of the parameters are empty
   */
  public void putData(String stateCode, String county, String data) {
    if(stateCode.equals("") || county.equals("") || data.equals(""))
      throw new NullPointerException("cannot store a null value");
    String key = generateKey(stateCode, county);
    cache.put(key, data);
  }

  /**
   * Retrieves broadband data from the cache for a given state and county.
   *
   * @param stateCode the state code of the data to retrieve
   * @param county the county name of the data to retrieve
   * @return the broadband data if present, or null otherwise
   * @throws NullPointerException if any of the parameters are empty
   */
  public String getData(String stateCode, String county) {
    if(stateCode.equals("") || county.equals(""))
      throw new NullPointerException("cannot search a null value");
    String key = generateKey(stateCode, county);
    return cache.getIfPresent(key);
  }

  /**
   * Invalidates all entries in the cache and purges it
   */
  public void invalidateAll(){
    cache.invalidateAll();
  }

  /**
   * Generates a unique key for storing data in the cache based on state code and county name.
   *
   * @param stateCode the state code part of the key
   * @param county the county name part of the key
   * @return a concatenated key of state code and county name
   */
  public static String generateKey(String stateCode, String county) {
    return stateCode + "-" + county;
  }

  /**
   * Wrapper to retrieve cache statistics, including hit rate, eviction count, and load times.
   *
   * @return a CacheStats object containing statistics about cache performance
   */
  public CacheStats stats() {
    return cache.stats();
  }
}
