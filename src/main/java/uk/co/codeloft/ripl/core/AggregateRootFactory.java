package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class AggregateRootFactory<T extends AggregateRoot> {

    // Thinking out loud...
    //
    // All relationships added must form a hierarchy with the AR at the top
    // - the AR must be the first one added
    // - every time a relationship is added,

    /**
     * Represents an occurrence of an attempt to register an invalid relationship type.  If this happens
     * There is little the client can do; a coding fix is needed, hence this is a RuntimeException
     */
    static class InvalidRelationshipTypeException extends RuntimeException {

        protected InvalidRelationshipTypeException(String message) {
            super(message);
        }
    }

    /**
     * Represents an occurrence of an attempt to register an invalid relationship instance.
     */
    public static class InvalidRelationshipInstanceException extends Command.PreConditionException {

        protected InvalidRelationshipInstanceException(String message) {
            super(message);
        }
    }

    /**
     * Nested class used to represent a parent-child relationship between a parent class and a child class
     */
    @Getter
    protected static class ParentChildRelationship {
        private final Class<?> parentClass;
        private final Class<?> childClass;

        protected ParentChildRelationship(Class<?> parent, Class<?> child) {
            // The parent class must be Entity or a sub-type
            if (!Entity.class.isAssignableFrom(parent)) {
                throw new AggregateRootFactory.InvalidRelationshipTypeException(Entity.class.getName() + " must be assignable from " + parent.getName());
            }

            // The child class must be ChildEntity or a sub-type
            if (!ChildEntity.class.isAssignableFrom(child)) {
                throw new AggregateRootFactory.InvalidRelationshipTypeException(ChildEntity.class.getName() + " must be assignable from " + child.getName());
            }

            this.parentClass = parent;
            this.childClass = child;
        }
    }

    /**
     * A map of declared parent-child relationships.  Such relationships are class-level so
     * there is no need for instance-level values
     */
    //protected static final Map<String, AggregateRootFactory.ParentChildRelationship> allowedRelationships = new HashMap<>();
    protected final Map<String, AggregateRootFactory.ParentChildRelationship> allowedChildRelationships = new HashMap<>();

    /**
     * Allow a parent-child relationship between two classes, distinguished by a role.  Note the invariants defined for each parameter
     * @param parentClass The class of object that act as the parent in the relationship
     * @param childClass the class of object that can act as the child
     * @param role the role that distinguishes this parent-child relationship.  This distinction is achieved because the role
     *             must be unique across all relationships with the same parent class.  For example, a Person may be the owner of many Cars.
     *             Here the Person class is the parent class, the Car class is the child class, and the role is owner.  A Person may have
     *             other kinds of relationships with cars, but not as the owner.  For example, they could be an insured driver (without
     *             necessarily being the owner).
     */
    public void allowRelationship(Class<?> parentClass, Class<?> childClass, String role) {

        // TODO: throw an exception if role already exists for the parent class

        AggregateRootFactory.ParentChildRelationship rel = new AggregateRootFactory.ParentChildRelationship(parentClass, childClass);
        this.allowedChildRelationships.put(role, rel);
    }

    public boolean isAllowedRelationship(Class<?> expectedParentClass, Class<?> expectedChildClass, String role) {
        AggregateRootFactory.ParentChildRelationship rel = this.allowedChildRelationships.get(role);

        return rel != null && rel.childClass == expectedChildClass && rel.parentClass == expectedParentClass;
    }


    //--------------------

    public static class InvalidCommandTargetException extends Exception {
        public InvalidCommandTargetException(String message) { super(message); }
    }

    private final AggregateRootRepository<T> repository;

    public AggregateRootFactory(AggregateRootRepository<T> repo) {
        this.repository = repo;
    }

    public T perform(Command<T> command) throws Command.PreConditionException {
        // store the command
        this.repository.storeCommand(command);

        // evaluate pre-conditions (might throw up)
        command.checkPreConditions();

        // store the event
        this.repository.storeEvent(command.getEvent());

        // TODO: attention needed here - don't always store snapshot!
        // store the snapshot
        T snapshot = command.getEvent().apply(null);
        this.repository.storeSnapshot(snapshot);

        return snapshot;
    }

    protected T getLatest(String id) throws InvalidCommandTargetException {
        T latest = this.repository.getLatest(id);
        if (latest == null)
            throw new InvalidCommandTargetException(String.format("Id %s does not identify valid aggregate root entity in the repository", id));
        else
            return this.repository.getLatest(id);
    }
}
