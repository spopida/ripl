package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Getter
public class UpdateChildCommand<R extends AggregateRoot, C extends ChildEntity, O> extends Command<R> {

    private final BiPredicate<C, O> preCondition;

    private final BiConsumer<C, O> applyFunc;

    private final C targetChild;

    private final O param;

    private final R targetRoot;

    private final String role;

    public UpdateChildCommand(AggregateRootFactory<R> origin, R targetRoot, String role, C targetChild, final BiPredicate<C, O> preCondition, final BiConsumer<C, O> applyFunc, O param) {
        super(origin);
        this.targetChild = targetChild;
        this.role = role;
        this.preCondition = preCondition;
        this.applyFunc = applyFunc;
        this.param = param;
        this.targetRoot = targetRoot;
    }

    public void checkPreConditions() throws PreConditionException {
        // TODO: We should get the role in the constructor, then check here that the child has the given parent according to that role

        if (this.preCondition != null && Boolean.FALSE.equals(preCondition.test(this.targetChild, this.param)))
            throw new PreConditionException("Parameterised pre-condition was not met");
    }


    @Override
    public ChildUpdatedEvent<R, C, O> getEvent() {
        return new ChildUpdatedEvent<R, C, O>( this.getOrigin(), this);
    }
}
