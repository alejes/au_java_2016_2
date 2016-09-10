import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class LazyFactoryTest {
    static final int TEST_COUNT = 100000000;

    @Test
    public void monoTests() {
        Holder hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyMono(hold::get);
        int resultSum = IntStream.range(0, TEST_COUNT).map(x -> lazy.get()).sum();

        assertEquals(TEST_COUNT, resultSum);
        assertEquals(1, lazy.get().intValue());
    }

    @Test
    public void concurrentTests() {
        Holder hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(hold::get);
        int resultSum = IntStream.range(0, TEST_COUNT).parallel().map(x -> lazy.get()).sum();

        assertEquals(TEST_COUNT, resultSum);
        assertEquals(1, lazy.get().intValue());
    }

    @Test
    public void lockFreeTests() {
        Holder hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyLockFree(hold::get);
        long resultList = IntStream.range(0, TEST_COUNT).parallel().map(x -> lazy.get()).distinct().count();

        assertEquals(1, resultList);
    }

    class Holder {
        private final AtomicInteger counter = new AtomicInteger(0);

        int get() {
            return counter.addAndGet(1);
        }
    }
}