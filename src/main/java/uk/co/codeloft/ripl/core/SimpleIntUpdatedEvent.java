package uk.co.codeloft.ripl.core;

@Deprecated
public class SimpleIntUpdatedEvent<T extends AggregateRoot> extends UpdatedEvent<T>{

    public SimpleIntUpdatedEvent(SimpleIntUpdateCommand<T> command) {
        super(command);
    }

    @Override
    public T apply() {
        T newVersion = this.updateAggregateRoot(); // TODO: change how this is done?

        SimpleIntUpdateCommand<T> cmd = (SimpleIntUpdateCommand<T>) this.getCommand();
        cmd.getFunc().accept(newVersion, cmd.getParam());
        return cmd.getTarget();
    }
}
