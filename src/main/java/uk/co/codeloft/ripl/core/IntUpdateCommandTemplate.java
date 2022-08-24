package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Getter
@Deprecated
public class IntUpdateCommandTemplate<T extends AggregateRoot> {
    private final BiPredicate<T, Integer> preCondition;
    private final BiConsumer<T, Integer> eventFunc;

    public IntUpdateCommandTemplate(BiConsumer<T, Integer> eventFunc) {
        this(null, eventFunc);
    }

    public IntUpdateCommandTemplate(BiPredicate<T, Integer> preCondition, BiConsumer<T, Integer> eventFunc) {
       this.preCondition = preCondition;
       this.eventFunc = eventFunc;
    }

    public SimpleIntUpdateCommand<T> using(T target, int i) {
        return new SimpleIntUpdateCommand<>(target, this.preCondition, this.eventFunc, i);
    }
}
