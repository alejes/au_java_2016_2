import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class LazyFactoryTest {
    static int TEST_COUNT = 100000000;

    @Test
    public void monoTests() {
        Holder hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyMono(hold::get);
        int resultSum = IntStream.range(0, TEST_COUNT).map(x -> lazy.get()).sum();

        assert (resultSum == TEST_COUNT);
        assert (lazy.get() == 1);
    }

    @Test
    public void concurrentTests() {
        Holder hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyConcurrent(hold::get);
        int resultSum = IntStream.range(0, TEST_COUNT).parallel().map(x -> lazy.get()).sum();

        assert (resultSum == TEST_COUNT);
        assert (lazy.get() == 1);
    }

    @Test
    public void lockFreeTests() {
        Holder hold = new Holder();
        Lazy<Integer> lazy = LazyFactory.createLazyLockFree(hold::get);
        long resutList = IntStream.range(0, TEST_COUNT).parallel().map(x -> lazy.get()).distinct().count();

        assert (resutList == 1);
    }

    class Holder {
        AtomicInteger counter = new AtomicInteger(0);

        int get() {
            return counter.addAndGet(1);
        }
    }
}