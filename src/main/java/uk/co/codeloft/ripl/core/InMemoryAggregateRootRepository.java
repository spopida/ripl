package uk.co.codeloft.ripl.core;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAggregateRootRepository<T extends AggregateRoot> implements AggregateRootRepository<T> {

    /**
     * Somewhere to store the commands
     */
    private Map<String, Command<T>> commands;

    /**
     * Somewhere to store the events
     */
    private Map<String, Event<T>> events;

    /**
     * Somewhere to store snapshots.  A snapshot is a fully-hydrated version of an aggregate to which
     * later events can be applied to obtain later versions of the same entity
     */
    private Map<String, T> snapshots;

    private Map<String, String> latestSnapshotsKeyedByAggregateRootId;

    public InMemoryAggregateRootRepository() {
        this.commands = new HashMap<>();
        this.events = new HashMap<>();
        this.snapshots = new HashMap<>();
        this.latestSnapshotsKeyedByAggregateRootId = new HashMap<>();
    }

    @Override
    public T getLatest(String id) {
        String snapshotId = this.latestSnapshotsKeyedByAggregateRootId.get(id);

        if (this.snapshots.containsKey(snapshotId)) {
            T root = this.snapshots.get(snapshotId);

            // TODO: INFLATE THE SNAPSHOT TO THE LATEST VERSION
            // - get all events since the version of the snapshot in the right order and apply them
            return root;
        } else {
            return null;
        }
    }

    @Override
    public void storeCommand(Command<T> command) {
        this.commands.put(command.getId(), command);
    }

    @Override
    public void storeEvent(Event<T> event) {
        events.put(event.getId(), event);
    }

    @Override
    public void storeSnapshot(T snapshot) {
        this.snapshots.put(snapshot.getSnapshotId(), snapshot);
        this.latestSnapshotsKeyedByAggregateRootId.put(snapshot.getId(), snapshot.getSnapshotId());
    }
}
