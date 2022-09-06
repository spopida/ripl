package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A command to create an instance of an AggregateRoot sub-class.  Create commands do not target an existing
 * instance (unlike update commands)
 * @param <T> the type of the AggregateRoot
 * @param <K> the type of the kernel object
 */
@Getter
public class CreateCommand<T extends AggregateRoot, K> extends Command<T> {

    private final Predicate<K> preConditionFunc;
    private final Function<T, T> eventApplyFunc;
    private final K kernel;
    private final BiFunction<CreatedEvent<T, K>, K, T> constructor;

    public CreateCommand(Predicate<K> preConditionFunc, K kernel, BiFunction<CreatedEvent<T, K>, K, T> ctor) {
        super();
        this.preConditionFunc = preConditionFunc;
        this.eventApplyFunc = target -> target;
        this.kernel = kernel;
        this.constructor = ctor;
    }

    @Override
    public void checkPreConditions() throws PreConditionException {
        if (!preConditionFunc.test(kernel)) throw new PreConditionException("Pre-Condition failed");
    }

    @Override
    public CreatedEvent<T, K> getEvent() {
        return new CreatedEvent<>(this, this.constructor);
    }
}
