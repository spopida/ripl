package uk.co.codeloft.ripl.core;

import lombok.Getter;

/**
 * A command to create a child entity
 * @param <R> The type of the aggregate root at the top of the entity tree
 * @param <P> the type of the immediate parent of the child entity
 * @param <C> the type of the child entity
 */
@Getter
public abstract class CreateChildCommand<R extends AggregateRoot, P extends Entity, C extends ChildEntity<R, P>> extends CreateCommand<R> {

    private final P parent;
    private final String role;

    public CreateChildCommand(P parent, String role) {
        super();
        this.parent = parent;
        this.role = role;
        //TODO: Check that type C is allowed to be a child of type P - if not throw an exception; this might have
        // to be delegated to the repository unless we move relationships into the entities!
    }
}
