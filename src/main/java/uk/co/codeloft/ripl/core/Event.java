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

    private final AggregateRootFactory factory;

    protected Event(AggregateRootFactory factory, Command<T> command) {
        this.factory = factory;
        this.id = UUID.randomUUID().toString();
        this.command = command;
        this.timestamp = Instant.now();
    }

    public abstract T apply(T rootEntity);

}
