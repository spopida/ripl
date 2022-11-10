package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * An event that has happened to an AggregateRoot
 * @param <T> the type of the event
 */
@Getter
public abstract class Event<T extends AggregateRoot> {
    /**
     * The unique id of this event
     */
    private String id;

    /**
     * The instant that this event happened
     */
    private Instant timestamp;

    /**
     * The command that resulted in this event
     */
    private Command<T> command;

    /**
     * The factory that performed this event
     */
    private final AggregateRootFactory factory;

    /**
     * Create an instance
     * @param factory the factory that performed the event represented by an instance
     * @param command the command that gave rise to the event
     */
    protected Event(AggregateRootFactory factory, Command<T> command) {
        this.factory = factory;
        this.id = UUID.randomUUID().toString();
        this.command = command;
        this.timestamp = Instant.now();
    }

    /**
     * Apply the event to an aggregate root entity.
     * @return a new version of the aggregate root entity, after having had the event applied
     */
    public abstract T apply();

}
