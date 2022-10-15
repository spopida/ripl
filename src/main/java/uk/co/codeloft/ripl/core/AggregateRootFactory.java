package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.Optional;

@Getter
public class AggregateRootFactory<T extends AggregateRoot> {

    public static class InvalidCommandTargetException extends Exception {
        public InvalidCommandTargetException(String message) { super(message); }
    }

    private final AggregateRootRepository<T> repository;

    public AggregateRootFactory(AggregateRootRepository<T> repo) {
        this.repository = repo;
    }

    public T perform(Command<T> command) throws Command.PreConditionException {
        // store the command
        this.repository.storeCommand(command);

        // evaluate pre-conditions (might throw up)
        command.checkPreConditions();

        // store the event
        this.repository.storeEvent(command.getEvent());

        // TODO: attention needed here - don't always store snapshot!
        // store the snapshot
        T snapshot = command.getEvent().apply(null);
        this.repository.storeSnapshot(snapshot);

        return snapshot;
    }

    protected T getLatest(String id) throws InvalidCommandTargetException {
        T latest = this.repository.getLatest(id);
        if (latest == null)
            throw new InvalidCommandTargetException(String.format("Id %s does not identify valid aggregate root entity in the repository", id));
        else
            return this.repository.getLatest(id);
    }
}
