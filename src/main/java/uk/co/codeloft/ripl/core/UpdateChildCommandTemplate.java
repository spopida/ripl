package uk.co.codeloft.ripl.core;

import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class UpdateChildCommandTemplate<R extends AggregateRoot, C extends ChildEntity, O> {

    private final BiPredicate<C, O> preCondition;

    private final BiConsumer<C, O> applyFunc;

    private final String role;

    public UpdateChildCommandTemplate(
            final String role,
            final BiPredicate<C, O> preCondition,
            final BiConsumer<C, O> applyFunc) {
        this.role = role;
        this.preCondition = preCondition;
        this.applyFunc = applyFunc;
    }

    public UpdateChildCommand<R, C, O> using(R aggregateRoot, C targetChild, O param) {
        return new UpdateChildCommand<>(aggregateRoot, this.role, targetChild, this.preCondition, this.applyFunc, param);
    }
}
