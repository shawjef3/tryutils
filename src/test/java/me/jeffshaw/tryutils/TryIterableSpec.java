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

    @Test
    public void testTryCloseAllSucceed() {
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
    public void testTryCloseAllFail() {
        for (int count: Arrays.asList(1, 2)) {
            final List<FailsClosed> cs =
                IntStream.range(0, count)
                    .mapToObj(i -> new FailsClosed())
                    .collect(Collectors.toList());
            final TryForeachException exception =
                Assert.expectThrows(TryForeachException.class, () -> TryIterable.tryClose(cs));

            Assert.assertEquals(exception.getFailures(), cs);
            Assert.assertTrue(exception.getCause() instanceof CloseException);
            Assert.assertEquals(exception.getCause().getSuppressed().length, count - 1);
        }
    }

    @Test
    public void testTryCloseSomeFail() {
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
        Assert.assertTrue(exception.getCause() instanceof CloseException);
        Assert.assertEquals(1, exception.getCause().getSuppressed().length);
    }

}
