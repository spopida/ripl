package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Getter
public class SimpleUpdateCommand<T extends AggregateRoot, O> extends UpdateCommand<T> {

    private final BiPredicate<T, O> preCondition;
    private final BiConsumer<T, O> func;
    private final O param;

    public SimpleUpdateCommand(T target, BiConsumer<T, O> func, O param) {
        this(target, null, func, param);
    }

    public SimpleUpdateCommand(T target, BiPredicate<T, O> preCond, BiConsumer<T, O> func, O param) {
        super(target);
        this.preCondition = preCond;
        this.func = func;
        this.param = param;
    }

    @Override
    public void checkPreConditions() throws PreConditionException {

        if (this.preCondition != null && Boolean.FALSE.equals(preCondition.test(this.getTarget(), this.param)))
            throw new PreConditionException("Parameterised pre-condition was not met");
    }

    @Override
    public SimpleUpdatedEvent<T, O> getEvent() {
        return new SimpleUpdatedEvent<>(this);
    }
}
