package com.googlecode.totallylazy;

import com.googlecode.totallylazy.functions.CountCalls0;
import com.googlecode.totallylazy.functions.Function0;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Test;

import static com.googlecode.totallylazy.functions.Callables.call;
import static com.googlecode.totallylazy.Callers.callConcurrently;
import static com.googlecode.totallylazy.Runnables.doNothing;
import static com.googlecode.totallylazy.Sequences.memorise;
import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.functions.CountCalls0.counting;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
public class MemoriseTest {
    @Test
    public void supportsGetWithIndex() throws Exception {
        Sequence<Integer> counting = counting().repeat().memorise();
        assertThat(counting.get(0), is(0));
        assertThat(counting.get(0), is(0));
    }

    @Test
    public void canForget() throws Exception {
        CountCalls0<Integer> counting = counting();
        Sequence<Integer> memory = repeat(counting).memorise();
        assertThat(memory.head(), is(0));
        assertThat(counting.count(), is(1));
        
        ((Memory) memory).forget();
        assertThat(memory.head(), is(1));
        assertThat(memory.head(), is(1));
        assertThat(counting.count(), is(2));
    }

    @Test
    public void canTurnAnIteratorIntoAReUsableSequence() throws Exception {
        Sequence<Integer> reusable = memorise(asList(1, 2).iterator());
        assertThat(reusable, hasExactly(1, 2));
        assertThat(reusable, hasExactly(1, 2));
    }

    @Test
    public void memoriseIsThreadSafe() throws Exception {
        CountCalls0<Integer> counting = counting();
        final Sequence<Integer> number = repeat(counting.sleep(10)).memorise();

        Sequence<Integer> result = callConcurrently(callHead(number).sleep(10), callHead(number).sleep(10));

        assertThat(result.first(), is(0));
        assertThat(result.second(), is(0));
        assertThat(counting.count(), is(1));
    }

    private Function0<Integer> callHead(final Sequence<Integer> number) {
        return number::head;
    }

    @Test
    public void supportsMemorise() throws Exception {
        CountCalls0<Integer> counting = counting();
        Sequence<Integer> sequence = repeat(counting).memorise();
        assertThat(sequence.head(), is(0));
        assertThat(sequence.head(), is(0));
        assertThat(counting.count(), is(1));
    }
    
    @Test
    public void memorisingEach() throws InterruptedException {
        CountCalls0<Integer> counting = counting();
        Sequence<Integer> sequence = sequence(counting).map(call(Integer.class)).memorise();
        sequence.each(doNothing(Integer.class));
        sequence.each(doNothing(Integer.class));

        assertThat(counting.count(), is(1));
    }

    @Test
    public void memorisingSize() throws InterruptedException {
        CountCalls0<Integer> counting = counting();
        Sequence<Integer> sequence = sequence(counting).map(call(Integer.class)).memorise();
        assertThat(sequence.size(), NumberMatcher.is(1));
        assertThat(counting.count(), is(1));
        assertThat(sequence.size(), NumberMatcher.is(1));
        assertThat(counting.count(), is(1));
    }
}
