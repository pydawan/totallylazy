package com.googlecode.totallylazy.parser;

import com.googlecode.totallylazy.Pair;
import org.junit.Test;

import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Segment.constructors.characters;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.googlecode.totallylazy.parser.CharacterParser.character;
import static org.hamcrest.MatcherAssert.assertThat;

public class PairParserTest {
    @Test
    public void canCombineTwoParsers() throws Exception {
        Success<Pair<Character, Character>> result = cast(PairParser.pairOf(character('A'), character('B')).parse(characters("ABC")));
        assertThat(result.value(), is(pair('A', 'B')));
        assertThat(result.remainder(), is(characters("C")));
    }

}
