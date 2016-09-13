import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class LazyFactoryConcurrentTest {
    static final int TEST_COUNT = 100000000;

    @Test
    public void concurrentTests() {
        Holder hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(hold::get);
        int resultSum = IntStream.range(0, TEST_COUNT).parallel().map(x -> lazy.get()).sum();

        assertEquals(TEST_COUNT, resultSum);
        assertEquals(1, lazy.get().intValue());
    }

    @Test
    public void onDemandTests() {
        Supplier<Integer> hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(hold);
        long resultSum = IntStream.range(0, TEST_COUNT).parallel().mapToObj(x -> lazy).count();

        assertEquals(TEST_COUNT, resultSum);
        assertEquals(1, hold.get().intValue());
    }

    @Test
    public void nullTests() {
        Supplier<Integer> hold = new HolderFirstNull();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(hold);
        long resultSum = IntStream.range(0, TEST_COUNT).parallel().mapToObj(x -> lazy.get()).count();

        assertEquals(TEST_COUNT, resultSum);
        assertEquals(null, lazy.get());
    }

    @Test
    public void countCalculationTests() {
        Supplier<Integer> hold = new HolderComputing(new Holder());
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(hold);
        int resultSum = IntStream.range(0, TEST_COUNT).parallel().map(x -> lazy.get()).sum();

        assertEquals(TEST_COUNT, resultSum);
        assertEquals(2, hold.get().intValue());
    }
}