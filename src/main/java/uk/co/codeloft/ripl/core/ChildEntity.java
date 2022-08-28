package uk.co.codeloft.ripl.core;

import java.util.UUID;

/**
 * A Child Entity both IS an Entity (hence it extends Entity), but it is also a child of a given type of parent Entity,
 * hence it is parameterised with the type of its parent. A third Entity sub-class class is implied - the class of the ultimate
 * aggregate root.  It's not clear yet, whether we need to make this explicit.
 *
 * @param <P> the class of the immediate parent (which itself must sub-class Entity)
 */
public class ChildEntity<R extends AggregateRoot, P extends Entity> extends Entity {

    // We can't be explicit about the type of the child here
    private final ChildCreatedEvent<R, P, ?> createdEvent;

    private final P parent;

    public ChildEntity(ChildCreatedEvent<R, P, ?> evt) {
        super(UUID.randomUUID().toString());
        this.createdEvent = evt;
        this.parent = evt.getCommand().getParent();
    }

    public AggregateRoot getRootParent() {
        // recurse up the parents until we find one that cannot be assigned to ChildEntity - that must be the root
        if (ChildEntity.class.isAssignableFrom(parent.getClass()))
            return ((ChildEntity<?, ?>)parent).getRootParent();
        else
            return (AggregateRoot)parent;
    }
}
