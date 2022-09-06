package uk.co.codeloft.ripl.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Currently a prototype class ... will need to be split into an interface and an in-memory repository
 * @param <T>
 */
public class InMemoryRepository<T extends AggregateRoot> {

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
    private Map<String, AggregateRoot> snapshots;

    private Map<String, String> latestSnapshotsKeyedByAggregateRootId;

    public InMemoryRepository() {
        this.commands = new HashMap<>();
        this.events = new HashMap<>();
        this.snapshots = new HashMap<>();
        this.latestSnapshotsKeyedByAggregateRootId = new HashMap<>();
    }

    /**
     * Create a new aggregate, returning the creation event if successful, or throwing an exception otherwise
     * @param command the
     * @return the event that represents successful creation
     * @throws Command.PreConditionException
     */
    public AggregateRoot createAggregate(CreateCommand<T> command) throws Command.PreConditionException {
        // Store the command as something that was instructed, whether it works or not
        this.commands.put(command.getId(), command);

        // Check the pre-conditions on the command
        command.checkPreConditions();

        // Capture the event and return it
        CreatedEvent<T> evt = (CreatedEvent<T>) command.getEvent(); // TODO: is this the right way?
        events.put(evt.getId(), evt);

        // Since we have created a new instance, we'd better store a snapshot
        AggregateRoot root = evt.apply();
        this.snapshots.put(root.getSnapshotId(), root);
        this.latestSnapshotsKeyedByAggregateRootId.put(root.getId(), root.getSnapshotId());

        return root;
    }

    /**
     * Capture a command, evaluate its pre-conditions, and if successful, create a corresponding event
     * @param command the command to evaluate
     * @throws Command.PreConditionException
     */
    public void apply(Command<T> command) throws Command.PreConditionException {
        // Store the command as something that was instructed, whether it works or not
        this.commands.put(command.getId(), command);

        // Check the pre-conditions on the command
        command.checkPreConditions();

        // Create commands have a special side effect of creating a snapshot if they are successful
        // This is then passed into the event, which has no effect when applied!

        // To do that we'll need a function that creates the object ...

        // Record the event
        Event<T> evt = command.getEvent();
        events.put(evt.getId(), evt);

        return;
    }

    public AggregateRoot getLatest() {

    }

    public AggregateRoot updateAggregate(Command<T> command) throws Command.PreConditionException {
        // Store the command as something that was instructed, whether it works or not
        this.commands.put(command.getId(), command);

        // Check the pre-conditions on the command
        command.checkPreConditions();

        // Record the event
        Event<T> evt = command.getEvent();
        events.put(evt.getId(), evt);

        // Apply the event, returning the new version of the AggregateRoot
        return evt.apply();
    }


    /**
     * Apply a command to an existing aggregate, returning an event if successful, or throwing an exception otherwise
     * @param aggregate
     * @param command
     * @return
     * @throws Command.PreConditionException
     */
    public Event<T> applyCommand(AggregateRoot aggregate, Command<T> command) throws Command.PreConditionException {
        // Store the command as something that was instructed, whether it works or not
        this.commands.put(command.getId(), command);

        // Check the pre-conditions on the command
        command.checkPreConditions();

        // Capture the event and return it
        Event<T> evt = command.getEvent();
        events.put(evt.getId(), evt);

        return evt;
    }

    /**
     * Get the latest incarnation of an AggregateRoot
     * @param id the identity of the AggregateRoot entity
     * @return the AggregateRoot instance if found, or null
     */
    public T getAggregateRoot(String id, Class<T> clazz ) {
        String snapshotId = this.latestSnapshotsKeyedByAggregateRootId.get(id);

        if (this.snapshots.containsKey(snapshotId)) {
            AggregateRoot root = this.snapshots.get(snapshotId);

            // TODO: INFLATE THE SNAPSHOT TO THE LATEST VERSION
            return clazz.cast(root);
        } else {
            return null;
        }
    }
}
