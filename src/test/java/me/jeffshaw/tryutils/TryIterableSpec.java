package me.jeffshaw.tryutils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.scalatestplus.testng.TestNGSuite;

public class TryIterableSpec extends TestNGSuite {

    public static class RequestThrows implements Runnable {
        private final boolean throwRequested;
        private boolean ran = false;

        public RequestThrows(final boolean throwRequested) {
            this.throwRequested = throwRequested;
        }

        @Override
        public void run() {
            if (throwRequested) {
                throw new TestException();
            }
            ran = true;
        }

        public boolean getRan() {
            return ran;
        }
    }

    @Test
    public void testTryCloseAllSucceed() throws TryForeachException {
        for (int count: Arrays.asList(0, 1, 2)) {
            final List<ReportsClosed> cs =
                IntStream.range(0, count)
                    .mapToObj(i -> new ReportsClosed())
                    .collect(Collectors.toList());
            TryIterable.tryClose(cs);
            Assert.assertTrue(cs.stream().allMatch(ReportsClosed::isClosed));
        }
    }

    @Test
    public void testTryCloseAllFail() throws TryForeachException  {
        for (int count: Arrays.asList(1, 2)) {
            final List<FailsClosed> cs =
                IntStream.range(0, count)
                    .mapToObj(i -> new FailsClosed())
                    .collect(Collectors.toList());
            final TryForeachException exception =
                Assert.expectThrows(TryForeachException.class, () -> TryIterable.tryClose(cs));

            Assert.assertEquals(exception.getFailures(), cs);
            Assert.assertTrue(exception.getCause() instanceof TestException);
            Assert.assertEquals(exception.getCause().getSuppressed().length, count - 1);
        }
    }

    @Test
    public void testTryCloseSomeFail() throws TryForeachException  {
        final List<AutoCloseable> cs =
            Arrays.asList(new ReportsClosed(), new FailsClosed(), new ReportsClosed(), new FailsClosed());
        final TryForeachException exception =
            Assert.expectThrows(TryForeachException.class, () -> TryIterable.tryClose(cs));

        Assert.assertTrue(
            cs.stream()
                .flatMap(c -> c instanceof ReportsClosed ? Stream.of((ReportsClosed) c) : Stream.empty())
                .allMatch(ReportsClosed::isClosed)
        );
        Assert.assertEquals(
            cs.stream().filter(c -> c instanceof FailsClosed).collect(Collectors.toList()),
            exception.getFailures()
        );
        Assert.assertTrue(exception.getCause() instanceof TestException);
        Assert.assertEquals(1, exception.getCause().getSuppressed().length);
    }

    @Test
    public void testTryForeach() throws TryForeachException {
        final List<Object> os = Arrays.asList(new Object(), new Object());

        final TryForeachException exception =
            Assert.expectThrows(
                TryForeachException.class,
                () -> TryIterable.tryForeach(
                    (Object o) -> {
                        throw new RuntimeException();
                    },
                    os
                )
            );
        Assert.assertEquals(exception.getFailures(), os);
    }

    @Test
    public void testTryForeachVarargs() throws TryForeachException {
        final Object[] os = new Object[] { new Object(), new Object() };

        final TryForeachException exception =
            Assert.expectThrows(
                TryForeachException.class,
                () -> TryIterable.tryForeach(
                    (Object o) -> {
                        throw new RuntimeException();
                    },
                    os
                )
            );

        boolean equals = exception.getFailures().equals(os);
        Assert.assertEquals(exception.getFailures(), Arrays.asList(os));
    }

    @Test
    public void testTryForeachInt() throws TryForeachException {
        final TryForeachException exception =
            Assert.expectThrows(
                TryForeachException.class,
                () -> TryIterable.tryForeachInt(
                    (int i) -> {
                        throw new RuntimeException();
                    },
                    0, 1
                )
            );
        Assert.assertEquals(exception.getFailures(), Arrays.asList(0, 1));
    }

    @Test
    public void testTryForeachSomeThrow() throws TryForeachException {
        final List<RequestThrows> requests = Arrays.asList(new RequestThrows(false), new RequestThrows(true));
        final TryForeachException exception =
            Assert.expectThrows(
                TryForeachException.class,
                () -> TryIterable.tryForeach(RequestThrows::run, requests)
            );
        Assert.assertEquals(exception.getFailures(), Arrays.asList(requests.get(1)));
        Assert.assertTrue(requests.get(0).getRan());
        Assert.assertFalse(requests.get(1).getRan());
    }

}
