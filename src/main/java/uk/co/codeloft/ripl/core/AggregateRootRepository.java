package uk.co.codeloft.ripl.core;

import java.util.Optional;

/**
 * A repository for {@link AggregateRoot} entities in which commands, events and snapshots are persisted
 * @param <T> the sub-type of the {@link AggregateRoot} class that the repository handles
 */
public interface AggregateRootRepository<T extends AggregateRoot> {
    /**
     * Get the latest version of a specific aggregate root entity
     * @param id the identity of the aggregate root entity required
     * @return an {@link Optional} containting the sub-type instance of {@link AggregateRoot} if it exists
     */
    public Optional<T> getLatest(String id);

    /**
     * Store a command
     * @param command the command to store
     */
    public void storeCommand(Command<T> command);

    /**
     * Store an event
     * @param event the event to store
     */
    public void storeEvent(Event<T> event);

    /**
     * Store a snapshot of an {@link AggregateRoot} sub-type entity
     * @param snapshot
     */
    public void storeSnapshot(T snapshot);
}
