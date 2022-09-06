package uk.co.codeloft.ripl.core;

import lombok.AllArgsConstructor;
import java.util.function.BiFunction;

public class AggregateRootManager<T extends AggregateRoot, K> {
    private final AggregateRootRepository<T> repository;

    public AggregateRootManager(AggregateRootRepository<T> repo) {
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
}
