package uk.co.codeloft.ripl.core;

import lombok.Getter;

@Getter
public abstract class UpdatedEvent<T extends AggregateRoot> extends Event<T> {

    /**
     * The aggregate root entity that this event relates to
     */
    private final T aggregateRoot;

    public UpdatedEvent(UpdateCommand<T> command) {
        super(command);
        this.aggregateRoot = command.getTarget();
    }

    protected T updateAggregateRoot() {
        // TODO: Take a deep copy of the object
        T deepCopy = aggregateRoot; // TODO: here!

        deepCopy.setVersion(aggregateRoot.getVersion() + 1);
        // TODO: set lastUpdate on the aggregate
        return deepCopy;
    }
}
