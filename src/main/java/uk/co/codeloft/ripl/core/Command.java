package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.UUID;

/**
 * A command that can be performed on an AggregateRoot entity
 * @param <T> The type of the AggregateRoot entity that is either created or updated
 */
@Getter
public abstract class Command<T extends AggregateRoot> {

    public static class PreConditionException extends Exception {
        public PreConditionException(String message) {
            super(message);
        }
    }

    private final String id;

    private final AggregateRootFactory<T> origin;

    protected Command(AggregateRootFactory<T> origin)
    {
        this.id = UUID.randomUUID().toString();
        this.origin = origin;
    }

    /**
     * No-op implementation for simple commands to inherit
     * @throws PreConditionException
     */
    public void checkPreConditions() throws PreConditionException {}

    /**
     * Create an event reflecting successful execution of this command
     * @return
     */
    public abstract Event<T> getEvent();
}
