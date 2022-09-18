package uk.co.codeloft.ripl.core;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class UpdateCommandTemplate<T extends AggregateRoot, O> {
    private final BiPredicate<T, O> preCondition;
    private final BiConsumer<T, O> eventFunc;

    public UpdateCommandTemplate(BiConsumer<T, O> eventFunc) {
        this(null, eventFunc);
    }

    public UpdateCommandTemplate(BiPredicate<T, O> preCondition, BiConsumer<T, O> eventFunc) {
        this.preCondition = preCondition;
        this.eventFunc = eventFunc;
    }

    public UpdateCommand<T, O> using(T target, O o) {
        return new UpdateCommand<>(target, this.preCondition, this.eventFunc, o);
    }
}