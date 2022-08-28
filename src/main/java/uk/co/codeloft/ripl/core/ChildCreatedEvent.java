package uk.co.codeloft.ripl.core;

public abstract class ChildCreatedEvent<R extends AggregateRoot, P extends Entity, C extends ChildEntity<R, P>> extends CreatedEvent<R> {

    public ChildCreatedEvent(CreateChildCommand<R, P, C> command) {
        super(command);
    }

    @Override
    public CreateChildCommand<R, P, C> getCommand() {
        return (CreateChildCommand<R, P, C> ) super.getCommand();
    }
}
