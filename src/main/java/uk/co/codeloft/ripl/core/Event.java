package uk.co.codeloft.ripl.core;

import lombok.Getter;

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
     * The command that resulted in this event
     */
    private Command<T> command;

    protected Event(Command<T> command) {
        this.id = UUID.randomUUID().toString();
        this.command = command;
    }

    public abstract AggregateRoot apply();

}
