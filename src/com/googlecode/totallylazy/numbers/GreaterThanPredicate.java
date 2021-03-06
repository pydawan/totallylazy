package com.googlecode.totallylazy.numbers;

import com.googlecode.totallylazy.predicates.GreaterThan;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

public class GreaterThanPredicate extends LogicalPredicate<Number> implements GreaterThan<Number> {
    private final Number value;

    public GreaterThanPredicate(Number value) {
        this.value = value;
    }

    public boolean matches(Number other) {
        return Numbers.greaterThan(other, value);
    }

    public Number value() {
        return value;
    }
}
