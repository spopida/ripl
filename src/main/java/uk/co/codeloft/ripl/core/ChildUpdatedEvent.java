package uk.co.codeloft.ripl.core;

import lombok.Getter;

@Getter
public class ChildUpdatedEvent<R extends AggregateRoot, C extends ChildEntity, O> extends Event<R> {

    private final C targetChild;
    private final O param;

    private final R aggregateRoot;
    private final UpdateChildCommand<R, C, O> command;

    public ChildUpdatedEvent(UpdateChildCommand<R, C, O> cmd) {
        super(cmd);
        this.targetChild = cmd.getTargetChild();
        this.param = cmd.getParam();
        this.command = cmd;
        aggregateRoot = cmd.getTargetRoot();
    }

    @Override
    public R apply(R rootEntity) {
        // RE-THINK:
        // - there are too many arguments being passed...we shouldn't really need the root, parent, or role
        // - We should:
        //  - find out which role the child is related to its immediate parent via...this might have to be
        //    recursive in the general case - we might be nested down multiple levels!
        //  - Get a deep copy of the aggregate root with the version number instantiated
        //  - get a reference to the child in the deep copy
        //  - apply the applyFunc to the child

        // Get the target root
        R newVersion = this.updateAggregateRoot(); // TODO: change how this is done?

        // Maybe: Check that the child exists in the target root??
        // - here we need to get a reference to the SAME child from the new copy

        // Get the command
        UpdateChildCommand<R, C, O> cmd = (UpdateChildCommand<R, C,  O>) this.getCommand();

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
