package uk.co.codeloft.ripl.core;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An event reflecting the construction of a sub-type of {@link AggregateRoot}
 * @param <T> the sub-type of {@link AggregateRoot} entity
 * @param <K> the type of the 'kernel' object from which the entity is constructed
 */
public class CreatedEvent<T extends AggregateRoot, K> extends Event<T> {

    /**
     * A constructor for type T. The constructor is a function that
     * accepts this event, a kernel object, and returns an object of type T
     */
    private BiFunction<CreatedEvent<T, K>, K, T> constructor;

    /**
     * The 'kernel' object used to seed the creation of the aggregate root entity
     */
    private K kernel;

    /**
     * Construct an instance of this event type
     * @param createCmd the command that led to the creation of this event
     * @param ctor the constructor needed to create the first version of the AggregateRoot
     */
    protected CreatedEvent(
            AggregateRootFactory<T> factory,
            CreateCommand<T, K> createCmd,
            BiFunction<CreatedEvent<T, K>, K, T> ctor) {
        super(factory, createCmd);
        this.kernel = createCmd.getKernel();
        this.constructor = ctor;
    }

    /**
     * Apply this event, creating an instance of the sub-type of {@link AggregateRoot}
     * @param target must always be null for creation events
     * @return a new instance of the sub-type of {@link AggregateRoot}
     */
    @Override
    public T apply(T target) {
        // TODO: Code smell - this method is overriding an abstraction that always expects a parameter, but this
        //       implementation MUST have a null value (because we are creating something)

        if (target != null) throw new IllegalArgumentException("Null target expected when creating a new entity");
        return constructor.apply(this, this.kernel);
    }

}
