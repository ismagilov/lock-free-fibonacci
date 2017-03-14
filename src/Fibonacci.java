import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Fibonacci {
    private final AtomicReference<F> current =
        new AtomicReference<> (new F(BigInteger.ZERO, BigInteger.ONE));

    public BigInteger next() {
        BigInteger result;
        boolean isSet;

        do {
            F curF = current.get();
            result = curF.getCur();

            BigInteger cur = curF.getNext();
            BigInteger next = curF.getCur().add(curF.getNext());

            isSet = current.compareAndSet(curF, new F(cur, next));
        } while (!isSet);

        return result;
    }

    private static class F {
        private final BigInteger cur;
        private final BigInteger next;

        private F(BigInteger cur, BigInteger next) {
            this.cur = cur;
            this.next = next;
        }

        private BigInteger getCur() {
            return cur;
        }

        private BigInteger getNext() {
            return next;
        }
    }

    private static class TestFibonachi implements Runnable {
        private final Fibonacci fibonacci;
        private final List<BigInteger> result;

        public TestFibonachi(List<BigInteger> result, Fibonacci fibonacci) {
            this.result = result;
            this.fibonacci = fibonacci;
        }

        @Override
        public void run() {
            synchronized (result) {
                result.add(fibonacci.next());
            }
        }
    }

    public static void main(String[] args) {
        int expected[] = {0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987,
                1597, 2584, 4181, 6765, 10946, 17711, 28657, 46368, 75025, 121393, 196418, 317811};

        ArrayList<BigInteger> generated = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        Fibonacci fibonacci = new Fibonacci();

        for (int i = 0; i < expected.length; i++) {
            executor.execute(new TestFibonachi(generated, fibonacci));
        }

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        try {
            executor.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Generation check: " + compare(generated, expected));
    }

    private static boolean compare(List<BigInteger> generated, int expected[]) {
        if (generated.size() != expected.length) {
            System.out.println("Size differs");
            return false;
        }

        for (int i = 0; i < expected.length; i++) {
            if (generated.get(i).intValue() != expected[i]) {
                System.out.println("Different sequences");
                return false;
            }
        }

        return true;
    }
}
