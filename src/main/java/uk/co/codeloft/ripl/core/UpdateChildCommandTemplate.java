package uk.co.codeloft.ripl.core;

import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class UpdateChildCommandTemplate<R extends AggregateRoot, P extends Entity, C extends ChildEntity, O> {

    private final BiPredicate<C, O> preCondition;

    private final BiConsumer<C, O> applyFunc;

    private final R aggregateRoot;

    private final P immediateParent;

    private final String role;

    public UpdateChildCommandTemplate(
            final R root,
            final P parentEntity,
            final String role,
            final BiPredicate<C, O> preCondition,
            final BiConsumer<C, O> applyFunc) {
        this.aggregateRoot = root;
        this.immediateParent = parentEntity;
        this.role = role;
        this.preCondition = preCondition;
        this.applyFunc = applyFunc;
    }

    public UpdateChildCommand<R, P, C, O> using(C targetChild, O param) {
        return new UpdateChildCommand<>(this.aggregateRoot, this.role, targetChild, this.preCondition, this.applyFunc, param);
    }
}
