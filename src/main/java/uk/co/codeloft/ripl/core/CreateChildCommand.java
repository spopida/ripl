package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A command to create a child entity
 * @param <R> The type of the aggregate root at the top of the entity tree
 * @param <P> the type of the immediate parent of the child entity
 * @param <C> the type of the child entity
 */
@Getter
public class CreateChildCommand<R extends AggregateRoot, P extends Entity, C extends ChildEntity, K> extends Command<R> {

    private final P parent;
    private final String role;
    private final K kernel;
    BiFunction<ChildCreatedEvent<R, P, C, K>, K, C> constructor;
    Predicate<K> preCondition;


    // NOTE - we don't need to pass in the AggregateRoot as target because it should be derivable from the P parent
    public CreateChildCommand(Predicate<K> preCondition, K kernel, P parent, String role, BiFunction<ChildCreatedEvent<R, P, C, K>, K, C> ctor) {
        super();
        this.parent = parent;
        this.role = role;
        this.kernel = kernel;
        this.constructor = ctor;
        this.preCondition = preCondition;
  }

    @Override
    public void checkPreConditions() throws PreConditionException {
        super.checkPreConditions();

        Class<?> parentClass = parent.getClass();
        Class<?> childClass = kernel.getClass().getEnclosingClass();

        if (!Entity.isAllowedRelationship(parentClass, childClass, role))
            throw new Entity.InvalidRelationshipInstanceException(
                    String.format(
                            "Invalid attempt to relate %s with %s using role %s%n",
                            parentClass.getName(), childClass.getName(), role));

        if (!preCondition.test(kernel)) throw new PreConditionException("Child pre-condition failed");
    }

    @Override
    public ChildCreatedEvent<R, P, C, K> getEvent() {
        return new ChildCreatedEvent<>(
                this,
                this.parent,
                this.role,
                this.kernel,
                this.constructor);
    }

}
