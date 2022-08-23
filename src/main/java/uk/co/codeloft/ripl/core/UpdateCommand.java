package uk.co.codeloft.ripl.core;

import lombok.Getter;

@Getter
public abstract class UpdateCommand<T extends AggregateRoot> extends Command<T> {

    private final T target;

    protected UpdateCommand(T target) {
        super();
        this.target = target;
    }
}
