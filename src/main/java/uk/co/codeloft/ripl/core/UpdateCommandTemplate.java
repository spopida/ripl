package uk.co.codeloft.ripl.core;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class UpdateCommandTemplate<T extends AggregateRoot, O> {
    private final BiPredicate<T, O> preCondition;
    private final BiConsumer<T, O> eventFunc;

    private final AggregateRootFactory<T> factory;

    public UpdateCommandTemplate(AggregateRootFactory<T> factory, BiConsumer<T, O> eventFunc) {
        this( factory,null, eventFunc);
    }

    public UpdateCommandTemplate(
            AggregateRootFactory<T> factory,
            BiPredicate<T, O> preCondition,
            BiConsumer<T, O> eventFunc) {
        this.factory = factory;
        this.preCondition = preCondition;
        this.eventFunc = eventFunc;
    }

    public UpdateCommand<T, O> using(T target, O o) {
        return new UpdateCommand<>(this.factory, target, this.preCondition, this.eventFunc, o);
    }
}