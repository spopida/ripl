package uk.co.codeloft.ripl.core;

public class SimpleUpdatedEvent <T extends AggregateRoot, O extends Object> extends UpdatedEvent<T>{

    public SimpleUpdatedEvent(SimpleUpdateCommand<T, O> command) {
        super(command);
    }

    @Override
    public T apply(T target) {
        T newVersion = this.updateAggregateRoot(); // TODO: change how this is done?

        SimpleUpdateCommand<T, O> cmd = (SimpleUpdateCommand<T, O>) this.getCommand();
        cmd.getFunc().accept(newVersion, cmd.getParam());
        return cmd.getTarget();
    }
}

