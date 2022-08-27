package uk.co.codeloft.ripl.core;

import java.util.UUID;

public class ChildEntity<T extends AggregateRoot> extends Entity {

    ChildCreatedEvent<?, ?> createdEvent;

    protected ChildEntity(ChildCreatedEvent<?, ?> evt) {
        super(UUID.randomUUID().toString());
        this.createdEvent = evt;
    }
}
