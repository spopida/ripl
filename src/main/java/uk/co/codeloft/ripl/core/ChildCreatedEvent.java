package uk.co.codeloft.ripl.core;

import java.util.function.BiFunction;

public class ChildCreatedEvent<R extends AggregateRoot, P extends Entity, C extends ChildEntity, K> extends Event<R> {

    private final BiFunction<ChildCreatedEvent<R, P, C, K>, K, C> constructor;
    private final K kernel;
    private final P parent;
    private final String role;

    public ChildCreatedEvent(
            CreateChildCommand<R, P, C, K> command,
            P parent,
            String role,
            K kernel,
            BiFunction<ChildCreatedEvent<R, P, C, K>, K, C> ctor) {
        super(command);
        this.parent = parent;
        this.role = role;
        this.constructor = ctor;
        this.kernel = kernel;
    }

    // TODO: Not sure if we need this override?
    @Override
    public CreateChildCommand<R, P, C, K> getCommand() {
        return (CreateChildCommand<R, P, C, K> ) super.getCommand();
    }

    @Override
    public R apply(R target) {
        // Get the root of the parent (type = R)
        R root = this.parent.getRoot();

        // Create a new report (type = C) with a kernel (type =K)
        C newChild = this.constructor.apply(this, kernel);


        // Add the report to the immediate parent (type = P)

        // Return the aggregate root

    }

}
