package uk.co.codeloft.ripl.core;

public abstract class CreatedEvent<T extends AggregateRoot> extends Event<T> {

    public CreatedEvent(CreateCommand<T> createCmd) {
        // A CreatedEvent does not relate to an existing aggregate instance, therefore it must
        // generate an id and version before calling the superclass constructor
        super(createCmd);
    }

}
