package uk.co.codeloft.ripl.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class AggregateRoot extends Entity {

    /**
     * The id of the snapshot that underpins this version of the aggregate.
     */
    private final String snapshotId;

    /**
     * The version number of this instance
     */
    @Setter(value = AccessLevel.PROTECTED)
    private int version;

    private final CreatedEvent<?, ?> createdEvent;

    /**
     * A flat map of all descendents
     */
    private final Map<String, Entity> allDescendents;

    public AggregateRoot(CreatedEvent<?, ?> evt) {
        super(UUID.randomUUID().toString());
        this.snapshotId = UUID.randomUUID().toString();
        this.version = 1;
        this.createdEvent = evt;
        this.allDescendents = new HashMap<>();
        // Special case - we put this AggregateRoot instance in its map of descendents
        this.allDescendents.put(this.getId(), this);
    }

    public final void addDescendent(ChildEntity child) {
        this.allDescendents.put(child.getId(), child);
    }

    public final Optional<Entity> getDescendent(String entityId, Class<?> clazz) throws InvalidObjectTypeException {

        Entity e = allDescendents.get(entityId);
        if ((e != null) && (!e.getClass().isAssignableFrom(clazz)))
            throw new InvalidObjectTypeException(entityId, clazz.getName(), e.getClass().getName());

        return Optional.ofNullable(allDescendents.get(entityId));
    }

    public String toString() {
        return super.toString() + this.asString();
    }

    private String asString() {
        return
                String.format("Version: %d%n", this.getVersion()) +
                String.format("From Snapshot: %s%n", this.getSnapshotId());
    }

    public void evolve() {
        this.setVersion(this.version + 1);
    }
}
