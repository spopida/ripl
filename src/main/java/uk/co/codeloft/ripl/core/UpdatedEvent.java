package uk.co.codeloft.ripl.core;

public class UpdatedEvent<T extends AggregateRoot, O extends Object> extends Event<T>{

    /**
     * The aggregate root entity that this event relates to
     */
    private final T aggregateRoot;

    public UpdatedEvent(AggregateRootFactory<T> factory, UpdateCommand<T, O> command) {
        super(factory, command);
        this.aggregateRoot = command.getTarget();
    }

    @Override
    public T apply() {
        //T newVersion = this.updateAggregateRoot(); // TODO: change how this is done?
        aggregateRoot.mutate();

        UpdateCommand<T, O> cmd = (UpdateCommand<T, O>) this.getCommand();
        cmd.getApplyFunc().accept(aggregateRoot, cmd.getParam());
        return cmd.getTarget();
    }

    @Override
    public boolean requiresSnapshot() {
        return
                this.aggregateRoot.getSnapshotInterval() != 0 &&
                this.aggregateRoot.getVersion() % this.aggregateRoot.getSnapshotInterval() == 0;
    }
}

