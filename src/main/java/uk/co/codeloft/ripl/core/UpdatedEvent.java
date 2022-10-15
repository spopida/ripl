package uk.co.codeloft.ripl.core;

public class UpdatedEvent<T extends AggregateRoot, O extends Object> extends Event<T>{

    /**
     * The aggregate root entity that this event relates to
     */
    private final T aggregateRoot;

    public UpdatedEvent(UpdateCommand<T, O> command) {
        super(command);
        this.aggregateRoot = command.getTarget();
    }

    /*
    protected T updateAggregateRoot() {
        // TODO: Take a deep copy of the object
        T deepCopy = aggregateRoot; // TODO: here!

        //** Perhaps we should re-inflate a copy from the last snapshot?

        deepCopy.mutate();
        //deepCopy.setVersion(aggregateRoot.getVersion() + 1);
        // TODO: set lastUpdate on the aggregate
        return deepCopy;
    }

     */

    @Override
    public T apply(T target) {
        //T newVersion = this.updateAggregateRoot(); // TODO: change how this is done?
        aggregateRoot.mutate();

        UpdateCommand<T, O> cmd = (UpdateCommand<T, O>) this.getCommand();
        cmd.getApplyFunc().accept(aggregateRoot, cmd.getParam());
        return cmd.getTarget();
    }
}

