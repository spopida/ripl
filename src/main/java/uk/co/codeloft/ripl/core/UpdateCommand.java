package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Getter
public class UpdateCommand<T extends AggregateRoot, O> extends Command<T> {

    private final T target;
    private final BiPredicate<T, O> preCondition;
    private final BiConsumer<T, O> applyFunc;
    private final O param;

    public UpdateCommand(AggregateRootFactory<T> origin, T target, BiConsumer<T, O> applyFunc, O param) {
        this(origin, target, null, applyFunc, param);
    }

    public UpdateCommand(AggregateRootFactory<T> origin,T target, BiPredicate<T, O> preCond, BiConsumer<T, O> applyFunc, O param) {
        super(origin);
        this.target = target;
        this.preCondition = preCond;
        this.applyFunc = applyFunc;
        this.param = param;
    }

    @Override
    public void checkPreConditions() throws PreConditionException {

        if (this.preCondition != null && Boolean.FALSE.equals(preCondition.test(this.getTarget(), this.param)))
            throw new PreConditionException("Parameterised pre-condition was not met");
    }

    @Override
    public UpdatedEvent<T, O> getEvent() {
        return new UpdatedEvent<>(this.getOrigin(), this);
    }
}
