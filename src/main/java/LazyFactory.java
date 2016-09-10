import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class LazyFactory {
    public static <T> Lazy<T> createLazyMono(Supplier<T> sup) {
        return new Lazy<T>() {
            boolean isReady = false;
            T value = null;

            @Override
            public final T get() {
                if (isReady) {
                    return value;
                } else {
                    value = sup.get();
                    isReady = true;
                    return value;
                }
            }
        };
    }

    public static <T> Lazy<T> createLazyConcurrent(Supplier<T> sup) {
        return new Lazy<T>() {
            boolean isReady = false;
            T value = null;

            @Override
            public final synchronized T get() {
                if (isReady) {
                    return value;
                } else {
                    value = sup.get();
                    isReady = true;
                    return value;
                }
            }
        };
    }

    public static <T> Lazy<T> createLazyLockFree(Supplier<T> sup) {
        return new Lazy<T>() {
            private final AtomicReference<Holder<T>> holderReference = new AtomicReference<>();

            @Override
            public T get() {
                if (holderReference.get() == null) {
                    holderReference.compareAndSet(null, new Holder<>(sup.get()));
                }
                return holderReference.get().storedValue;
            }

            class Holder<T> {
                T storedValue;

                public Holder(T storedValue) {
                    this.storedValue = storedValue;
                }
            }
        };
    }
}
