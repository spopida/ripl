package uk.co.codeloft.ripl.core;

import lombok.Getter;

@Getter
public class ChildUpdatedEvent<R extends AggregateRoot, C extends ChildEntity, O> extends Event<R> {

    private final C targetChild;
    private final O param;

    private final R aggregateRoot;
    private final UpdateChildCommand<R, C, O> command;

    public ChildUpdatedEvent(AggregateRootFactory<R> factory, UpdateChildCommand<R, C, O> cmd) {
        super(factory, cmd);
        this.targetChild = cmd.getTargetChild();
        this.param = cmd.getParam();
        this.command = cmd;
        aggregateRoot = cmd.getTargetRoot();
    }

    @Override
    public R apply() {

        // Maybe: Check that the child exists in the target root??
        // - here we need to get a reference to the SAME child from the new copy

        // Get the command
        UpdateChildCommand<R, C, O> cmd = (UpdateChildCommand<R, C,  O>) this.getCommand();

        // Call the apply function
        cmd.getApplyFunc().accept(this.targetChild, this.param);
        this.targetChild.mutate();

        // Return the root
        return this.aggregateRoot;
    }

    @Override
    public boolean requiresSnapshot() {
        return
                this.aggregateRoot.getSnapshotInterval() == 0 ||
                this.aggregateRoot.getVersion() % this.aggregateRoot.getSnapshotInterval() == 0;
    }
}
