package uk.co.codeloft.ripl.core;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CreatedEvent<T extends AggregateRoot, K> extends Event<T> {

    /**
     * A constructor for type T. The constructor is a function that
     * accepts this event, a kernel object, and returns an object of type T
     */
    private BiFunction<CreatedEvent<T, K>, K, T> constructor;

    private K kernel;

    /**
     * Construct an instance of this event type
     * @param createCmd the command that led to the creation of this event
     * @param ctor the constructor needed to create the first version of the AggregateRoot
     */
    public CreatedEvent(CreateCommand<T, K> createCmd, BiFunction<CreatedEvent<T, K>, K, T> ctor) {
        super(createCmd);
        this.kernel = createCmd.getKernel();
        this.constructor = ctor;
    }

    /**
     * TODO: Slight concern that this has to be called with null (since there is no target for creation)
     */
    @Override
    public T apply(T target) {
        return constructor.apply(this, this.kernel);
    }

}
