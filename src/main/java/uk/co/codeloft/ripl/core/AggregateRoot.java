package uk.co.codeloft.ripl.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class AggregateRoot extends Entity {

    /**
     * The id of the snapshot that underpins this version of the aggregate.
     */
    private String snapshotId;

    /**
     * The version number of this instance
     */
    @Setter(value = AccessLevel.PROTECTED)
    private int version;

    private CreatedEvent<?> createdEvent;

    public AggregateRoot(CreatedEvent<?> evt) {
        super(UUID.randomUUID().toString());
        this.snapshotId = UUID.randomUUID().toString();
        this.version = 1;
        this.createdEvent = evt;
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
