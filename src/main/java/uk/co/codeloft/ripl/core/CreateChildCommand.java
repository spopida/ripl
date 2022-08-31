package uk.co.codeloft.ripl.core;

import lombok.Getter;

/**
 * A command to create a child entity
 * @param <R> The type of the aggregate root at the top of the entity tree
 * @param <P> the type of the immediate parent of the child entity
 * @param <C> the type of the child entity
 */
@Getter
public abstract class CreateChildCommand<R extends AggregateRoot, P extends Entity, C extends ChildEntity> extends CreateCommand<R> {

    private final R root;
    private final P parent;
    private final String role;
    private final Class<C> childClass;

    public CreateChildCommand(R root, P parent, String role, Class<C> childClass) {
        super();
        this.root = root;
        this.parent = parent;
        this.role = role;
        this.childClass = childClass;

        //TODO: Check that type C is allowed to be a child of type P according to the given role - if not throw an exception; this might have
        // to be delegated to the repository unless we move relationships into the entities!
    }
}
