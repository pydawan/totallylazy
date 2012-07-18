package com.googlecode.totallylazy.callables;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.returns;
import static com.googlecode.totallylazy.Callers.callConcurrently;
import static com.googlecode.totallylazy.callables.CountingCallable.counting;
import static com.googlecode.totallylazy.callables.LazyCallable.lazy;
import static com.googlecode.totallylazy.callables.TimeCallable.time;
import static com.googlecode.totallylazy.callables.TimeReport.time;
import static com.googlecode.totallylazy.matchers.NumberMatcher.between;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class LazyCallableTest {
    @Test
    public void isVeryFast() throws Exception {
        System.out.println("base = " + time(1000, returns(1)));
        System.out.println("lazy = " + time(1000, lazy(returns(1))));
    }

    @Test
    public void instancesDoNotInteract() throws Exception {
        TimeReport firstTimes = new TimeReport();
        Callable<Sequence<Integer>> firstLazy = time(counting().sleep(5).lazy().repeat().take(200), firstTimes);

        TimeReport secondTimes = new TimeReport();
        Callable<Sequence<Integer>> secondLazy = time(counting().sleep(50).lazy().repeat().take(100), secondTimes);

        callConcurrently(firstLazy, secondLazy).realise();

        System.out.println(firstTimes);
        System.out.println(secondTimes);

        assertThat(firstTimes.average(), is(between(5, 50)));
        assertThat(secondTimes.average(), is(between(50, 1000)));
    }

    @Test
    public void isThreadSafe() throws Exception {
        CountingCallable<Integer> callable = counting();
        Function<Integer> lazyCallable = callable.sleep(10).lazy();

        Sequence<Integer> result = callConcurrently(lazyCallable, lazyCallable).realise();

        assertThat(callable.count(), is(1));
        assertThat(result.first(), is(0));
        assertThat(result.second(), is(0));
    }

    @Test
    public void onlyCallsUnderlyingCallableOnce() throws Exception {
        CountingCallable<Integer> callable = counting();
        Callable<Integer> lazyCallable = callable.lazy();

        assertThat(lazyCallable.call(), is(0));
        assertThat(lazyCallable.call(), is(0));
        assertThat(callable.count(), is(1));
    }
}
