import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

class Holder implements Supplier<Integer> {
    private final AtomicInteger counter = new AtomicInteger(0);

    public Integer get() {
        return counter.addAndGet(1);
    }
}

class HolderFirstNull implements Supplier<Integer> {
    private final AtomicInteger counter = new AtomicInteger(0);
    private int count;

    HolderFirstNull(){
        this.count = 1;
    }

    HolderFirstNull(int count){
        this.count = count;
    }


    public Integer get() {
        int result = counter.addAndGet(1);
        if (result <= this.count) {
            return null;
        } else {
            return result;
        }
    }
}

class HolderComputing implements Supplier<Integer> {
    private Holder hc = null;

    public HolderComputing(Holder hc){
        this.hc = hc;
    }

    public Integer get() {
        return hc.get();
    }
}