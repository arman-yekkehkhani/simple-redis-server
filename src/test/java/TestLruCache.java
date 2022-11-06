import org.example.cache.Cache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestLruCache {

    @Test
    public void putTest() {
        Cache<Integer, Integer> cache = new Cache<>(3);
        for (int i = 0; i < 10; i++) {
            cache.put(i, i);
        }

        List<Integer> keys = new ArrayList<>();
        for (Integer key : cache.keys()) {
            keys.add(key);
        }
        Assertions.assertIterableEquals(keys, Arrays.asList(9, 8, 7));
    }

    @Test
    public void orderTest() {
        Cache<Integer, Integer> cache = createPopulatedCache();

        cache.get(2);
        List<Integer> keys = new ArrayList<>();
        for (Integer key : cache.keys()) {
            keys.add(key);
        }
        Assertions.assertIterableEquals(keys, Arrays.asList(2, 3, 1));
    }

    @Test
    public void updateTest() {
        Cache<Integer, Integer> cache = createPopulatedCache();

        cache.put(1, 4);
        assert cache.get(1) == 4;
    }

    @Test
    public void removeTest() {
        Cache<Integer, Integer> cache = createPopulatedCache();
        cache.remove(1);
        cache.remove(4);
    }

    @Test
    public void fetchTest() {
        Cache<Integer, Integer> cache = createPopulatedCache();
        assert cache.get(1) == 1;
        assert cache.get(4) == null;
    }

    private Cache<Integer, Integer> createPopulatedCache() {
        Cache<Integer, Integer> cache = new Cache<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        return cache;
    }
}
