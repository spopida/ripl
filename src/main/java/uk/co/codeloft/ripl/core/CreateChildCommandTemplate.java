package uk.co.codeloft.ripl.core;


import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A template for a CreateChildCommand for a Child Entity
 */
public class CreateChildCommandTemplate<R extends AggregateRoot, P extends Entity, C extends ChildEntity, K> {

    private final Predicate<K> preCondition;
    private final BiFunction<ChildCreatedEvent<R, P, C,K>, K, C> constructor;

    public CreateChildCommandTemplate(
            Predicate<K> preCondition,
            BiFunction<ChildCreatedEvent<R, P, C, K>, K, C> constructor) {
        this.preCondition = preCondition;
        this.constructor = constructor;
    }

    //TODO: we might be able to get rid of R as an explicit parameter as it should be derivable from the parent
    public CreateChildCommand<R, P, C, K> using(R root, P parent, K kernel, String role) throws Entity.InvalidRelationshipInstanceException {

        return new CreateChildCommand<>(
                this.preCondition,
                kernel,
                parent,
                role,
                this.constructor);
    }
}
