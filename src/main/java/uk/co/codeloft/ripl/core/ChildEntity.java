package uk.co.codeloft.ripl.core;

import java.util.UUID;

public class ChildEntity<T extends AggregateRoot> extends Entity {

    protected ChildEntity() {
        super(UUID.randomUUID().toString());
    }
}
