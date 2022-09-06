package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A template for a CreateCommand that has a pre-condition predicate
 * @param <T> the type for which the generated CreateCommand will work
 */
@Getter
public class CreateCommandTemplate<T extends AggregateRoot, K> {

    /**
     * The preCondition is for a kernel object
     */
    private final Predicate<K> preCondition;

    private final BiFunction<CreatedEvent<T, K>, K, T> constructor;

    public CreateCommandTemplate(Predicate<K> preCondition, BiFunction<CreatedEvent<T, K>, K, T> ctor) {
        this.preCondition = preCondition;
        this.constructor = ctor;
    }

    public CreateCommand<T, K> using(K kernel) {
        return new CreateCommand<>(this.preCondition, kernel, this.constructor);
    }
}
