package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Getter
@Deprecated
public class SimpleIntUpdateCommand<T extends AggregateRoot> extends UpdateCommand<T> {

    private final BiPredicate<T, Integer> preCondition;
    private final BiConsumer<T, Integer> func;
    private final int param;

    public SimpleIntUpdateCommand(T target, BiConsumer<T, Integer> func, int param) {
        this(target, null, func, param);
    }

    public SimpleIntUpdateCommand(T target, BiPredicate<T, Integer> preCond, BiConsumer<T, Integer> func, int param) {
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
    public SimpleIntUpdatedEvent<T> getEvent() {
        return new SimpleIntUpdatedEvent<>(this);
    }
}
