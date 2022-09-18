package uk.co.codeloft.ripl.core;

import lombok.Getter;

@Getter
public class ChildUpdatedEvent<R extends AggregateRoot, P extends Entity, C extends ChildEntity, O> extends Event<R> {

    private final C targetChild;
    private final O param;

    private final R aggregateRoot;
    private final UpdateChildCommand<R, P, C, O> command;

    public ChildUpdatedEvent(UpdateChildCommand<R, P, C, O> cmd) {
        super(cmd);
        this.targetChild = cmd.getTargetChild();
        this.param = cmd.getParam();
        this.command = cmd;
        aggregateRoot = cmd.getTargetRoot();
    }

    @Override
    public R apply(R rootEntity) {
        // Get the target root
        R newVersion = this.updateAggregateRoot(); // TODO: change how this is done?

        // Maybe: Check that the child exists in the target root??
        // - here we need to get a reference to the SAME child from the new copy

        // Get the command
        UpdateChildCommand<R, P, C, O> cmd = (UpdateChildCommand<R, P, C,  O>) this.getCommand();

        // Call the apply function
        cmd.getApplyFunc().accept(this.targetChild, this.param);

        // Return the root
        return newVersion;
    }


    // TODO: Change this - inflate from repo?
    protected R updateAggregateRoot() {
        // TODO: Take a deep copy of the object
        R deepCopy = (R)this.aggregateRoot; // TODO: here!

        //** Perhaps we should re-inflate a copy from the last snapshot?

        deepCopy.setVersion(deepCopy.getVersion() + 1);
        // TODO: set lastUpdate on the aggregate
        return deepCopy;
    }
}
