import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LazyFactoryLockFreeTest {
    static final int TEST_COUNT = 100000000;

    @Test
    public void lockFreeTests() {
        Holder hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyLockFree(hold::get);
        long resultList = IntStream.range(0, TEST_COUNT).parallel().map(x -> lazy.get()).distinct().count();

        assertEquals(1, resultList);
    }

    @Test
    public void onDemandTests() {
        Supplier<Integer> hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyLockFree(hold);
        long resultSum = IntStream.range(0, TEST_COUNT).parallel().mapToObj(x -> lazy).count();

        assertEquals(TEST_COUNT, resultSum);
        assertEquals(1, hold.get().intValue());
    }

    @Test
    public void nullTests() {
        Supplier<Integer> hold = new HolderFirstNull(TEST_COUNT / 10000);
        Lazy<Integer> lazy = LazyFactory.createLazyLockFree(hold);
        long resultSum = IntStream.range(0, TEST_COUNT).parallel().mapToObj(x -> lazy.get()).count();

        assertEquals(TEST_COUNT, resultSum);
        assertEquals(null, lazy.get());
    }

    @Test
    public void countCalculationTests() {
        Supplier<Integer> hold = new HolderComputing(new Holder());
        Lazy<Integer> lazy = LazyFactory.createLazyLockFree(hold);
        int resultSum = IntStream.range(0, TEST_COUNT).parallel().map(x -> lazy.get()).sum();

        assertEquals(TEST_COUNT, resultSum);
        assertTrue(hold.get() >= 2);
    }
}
