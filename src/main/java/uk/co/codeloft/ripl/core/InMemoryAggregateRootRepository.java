package uk.co.codeloft.ripl.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public Optional<T> getLatest(String id) {
        String snapshotId = this.latestSnapshotsKeyedByAggregateRootId.get(id);

        if (this.snapshots.containsKey(snapshotId)) {
            T root = this.snapshots.get(snapshotId);

            // TODO: INFLATE THE SNAPSHOT TO THE LATEST VERSION

            // We have a little problem.  Commands and Events have been coded such that they have a reference to the
            // AR that they relate to.  This is not nice ... it means that whenever we retrieve either commands or
            // events, we need to retrieve or de-serialze the AR targeted or emerging.  This kind of defeats the point.
            // I think we should store the ID of the AR, but not a reference to it.  The implications of this need to be elaborated.
            //
            // - What happens when we 'perform' a command ?
            //  - we need to retrieve and inflate the AR, and check the pre-conditions
            // - What happens when we apply an event
            //  - we apply it to an AR - we need to pass the AR to apply!

            // This seems to be a case of calling a reduce() function ... in each iteration we want to pass the result
            // of applying the previous event until there are no more events left to apply

            //this.getEventsAfterVersion(root.getVersion()).map(e -> e.apply())
            return Optional.of(root);
        } else {
            return Optional.empty();
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
        snapshot.resetLsVersion();
        this.snapshots.put(snapshot.getSnapshotId(), snapshot);
        this.latestSnapshotsKeyedByAggregateRootId.put(snapshot.getId(), snapshot.getSnapshotId());
    }
}
