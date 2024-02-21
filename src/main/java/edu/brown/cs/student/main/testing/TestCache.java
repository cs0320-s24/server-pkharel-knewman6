package edu.brown.cs.student.main.testing;

import com.google.common.cache.CacheStats;
import edu.brown.cs.student.main.server.Caching.BroadbandCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;

public class TestCache {
    private BroadbandCache cache = new BroadbandCache(1, TimeUnit.SECONDS, 2);

    @Test
    public void testPutDataAndGetData() {
        String stateCode = "CA";
        String county = "001";
        String broadbandData = "{\"data\":\"sample data\"}";

        cache.putData(stateCode, county, broadbandData);
        String retrievedData = cache.getData(stateCode, county);

        assertEquals(broadbandData, retrievedData);
    }

    @Test
    public void testGetDataExpires() throws InterruptedException {
        String stateCode = "CA";
        String county = "001";
        String broadbandData = "{\"data\":\"sample data\"}";

        cache.putData(stateCode, county, broadbandData);
        Thread.sleep(2000);
        String retrievedData = cache.getData(stateCode, county);

        assertNull(retrievedData);
    }

    @Test
    public void testCacheEvictionBySize() {
        String broadbandData = "{\"data\":\"sample data\"}";

        cache.putData("CA", "001", broadbandData);
        cache.putData("NY", "001", broadbandData);
        cache.putData("TX", "001", broadbandData);

        String retrievedDataForFirstEntry = cache.getData("CA", "001");
        String retrievedDataForSecondEntry = cache.getData("NY", "001");
        String retrievedDataForThirdEntry = cache.getData("TX", "001");

        assertNull(retrievedDataForFirstEntry);
        assertEquals(broadbandData, retrievedDataForSecondEntry);
        assertEquals(broadbandData, retrievedDataForThirdEntry);
    }

    @Test
    public void testPutandGetDataWithNullValues() {
        assertThrows(NullPointerException.class, () -> {
            cache.putData(null, "001", "sample data");
        });

        assertThrows(NullPointerException.class, () -> {
            cache.putData("CA", null, "sample data");
        });

        assertThrows(NullPointerException.class, () -> {
            cache.putData("CA", "001", null);
        });
        assertThrows(NullPointerException.class, () -> {
            cache.getData(null, "001");
        });

        assertThrows(NullPointerException.class, () -> {
            cache.getData("CA", null);
        });

    }

    @Test
    public void testOverwritingData() {
        String initialData = "{\"data\":\"initial data\"}";
        String newData = "{\"data\":\"new data\"}";

        cache.putData("CA", "001", initialData);
        cache.putData("CA", "001", newData);

        String retrievedData = cache.getData("CA", "001");
        assertNotEquals(initialData, retrievedData);
        assertEquals(newData, retrievedData);
    }

    @Test
    public void testRetrievalOfNonExistentData() {
        String retrievedData = cache.getData("NonExistentState", "NonExistentCounty");
        assertNull(retrievedData, "Retrieving data with non-existent state and county should return null.");
    }

    @Test
    public void testRetrievalAfterClearingCache() {
        String stateCode = "CA";
        String county = "001";
        String broadbandData = "{\"data\":\"sample data\"}";

        cache.putData(stateCode, county, broadbandData);
        cache.invalidateAll();

        String retrievedData = cache.getData(stateCode, county);
        assertNull(retrievedData, "After clearing the cache, retrieval should return null.");
    }

    @Test
    public void testCacheStatsRecording() {
        String stateCode = "CA";
        String county = "001";
        String broadbandData = "{\"data\":\"sample data\"}";

        cache.putData(stateCode, county, broadbandData);
        cache.getData(stateCode, county); // Hit
        cache.getData(stateCode, "999"); // Miss

        CacheStats stats = cache.stats();

        assertTrue(stats.hitCount() > 0, "Cache stats should record hits.");
        assertTrue(stats.missCount() > 0, "Cache stats should record misses.");
        assertTrue(stats.loadSuccessCount() > 0, "Cache stats should record successful loads.");
    }

    @Test
    public void testKeyGenerationConsistency() {
        String stateCode = "CA";
        String county = "001";

        String key1 = BroadbandCache.generateKey(stateCode, county);
        String key2 = BroadbandCache.generateKey(stateCode, county);

        assertEquals(key1, key2);
    }
}
