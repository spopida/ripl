package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * An instance of this class represents a hierarchy of entities that form an aggregate entity that has long-lived
 * identity and may be referenced by other aggregates
 */
@Getter
public class AggregateRoot extends Entity {

    /**
     * The id of the snapshot that underpins this version of the aggregate.
     */
    private final String snapshotId;

    /**
     * A reference to the event that caused the creation of this instance
     */
    private final CreatedEvent<?, ?> createdEvent;

    /**
     * A flat map of all descendents (regardless of where they are in the hiearchy)
     */
    private final Map<String, Entity> allDescendents;

    /**
     * Create an instance attributable to a given {@link CreatedEvent}
     * @param evt the event that caused instantiation
     */

    /**
     * The number of versions that should be allowed between physically stored snapshots
     */
    private int snapshotInterval;

    public AggregateRoot(CreatedEvent<?, ?> evt, int snapshotInterval) {
        super(evt.getFactory(), UUID.randomUUID().toString());
        this.snapshotId = UUID.randomUUID().toString();
        this.createdEvent = evt;
        this.allDescendents = new HashMap<>();
        this.snapshotInterval = snapshotInterval;

        // Special case - we put this AggregateRoot instance in its map of descendents
        this.allDescendents.put(this.getId(), this);
    }

    /**
     * Record a descendent of this instance
     * @param child the descendent
     */
    protected final void addDescendent(ChildEntity child) {
        this.allDescendents.put(child.getId(), child);
    }

    /**
     * Get a descendent of this root entity, identified by its id
     * @param entityId the identity of the required descendent
     * @param clazz the expected class of the required descendent
     * @return an {@link Optional} instance of the required entity
     * @throws InvalidObjectTypeException
     */
    public final Optional<Entity> getDescendent(String entityId, Class<?> clazz) throws InvalidObjectTypeException {

        Entity e = allDescendents.get(entityId);
        if ((e != null) && (!e.getClass().isAssignableFrom(clazz)))
            throw new InvalidObjectTypeException(entityId, clazz.getName(), e.getClass().getName());

        return Optional.ofNullable(allDescendents.get(entityId));
    }

    /**
     * Get this instance in {@link String} form, including a representation of all inherited attributes
     * @return this instance (including inherited attributes) as a {@link String}
     */
    public String toString() {
        return super.toString() + this.asString();
    }

    /**
     * Get this instance (excluding inherited attributes) as a {@link String}
     * @return this instance as a {@link String}
     */
    private String asString() {
        return
                String.format("From Snapshot: %s%n", this.getSnapshotId());
    }
}
