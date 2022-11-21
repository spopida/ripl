package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class AggregateRootFactory<T extends AggregateRoot> {

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
     * A map of declared parent-child relationships.
     */
    protected final Map<String, AggregateRootFactory.ParentChildRelationship> allowedChildRelationships = new HashMap<>();

    /**
     * Allow a parent-child relationship between two classes, distinguished by a role.  Note the invariants defined for each parameter
     * @param parentClass The class of object that act as the parent in the relationship.  It is perfectly valid for this to be the same
     *                    as the {@code childClass} parameter, which would be used in the case of a recursive (aka 'involuted') relationship.
     * @param childClass the class of object that can act as the child.  It is perfectly valid for this to be the same as the {@code parentClass}
     *                   parameter
     * @param role the role that distinguishes this parent-child relationship.  This distinction is achieved because the role
     *             must be unique across all relationships with the same parent class.  For example, a Person may be the owner of many Cars.
     *             Here the Person class is the parent class, the Car class is the child class, and the role is owner.  A Person may have
     *             other kinds of relationships with cars, but not as the owner.  For example, they could be an insured driver (without
     *             necessarily being the owner).  This means that you can have more than one relationship between the <i>same</i> two
     *             entities, but they must have distinct roles. <b>It is suggested that nouns are used for roles, not verbs</b>.  For example,
     *             in this context, it is preferable to say "A <b>Person</b> is the <b>owner</b> of a <b>Car</b>", not "A <b>Person</b> <b>owns</b> a <b>Car</b>".
     *             This is purely because it is confusing to refer to specific roles as verbs.  Sometimes it is helpful to qualify the noun.
     */
    public void allowRelationship(Class<?> parentClass, Class<?> childClass, String role) {

        // TODO: throw an exception if role already exists for the parent class?

        AggregateRootFactory.ParentChildRelationship rel = new AggregateRootFactory.ParentChildRelationship(parentClass, childClass);
        this.allowedChildRelationships.put(role, rel);
    }

    /**
     * Determine whether two classes have been registered as participants in a parent-child relationship defined by
     * a given role
     * @param expectedParentClass the parent class
     * @param expectedChildClass the child class
     * @param role the role
     * @return {@code true} or {@code false}
     */
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

        // TODO: We might have to retrieve the latest version from the repo here

        // evaluate pre-conditions (might throw up)
        command.checkPreConditions();

        // TODO: Figure out how to store a 'snapshot' identifier in the event so that we can retrieve all events
        //       that succeed a given snapshot.  I think we should be able to store the aggregate root version of
        //       the last snapshot.
        //
        //       Basically, an AR has TWO versions.  The version of the AR, and the version of the last snapshot.
        //       When we create an AR, the two are the same.  When we mutate it, we only update the version, not the
        //       lsVersion.  When we store a new snapshot, we set them to be the same again.
        //
        //       When we create an Event, we set the lsVersion of the event to be the lsVersion of the AR that it is
        //       applying to
        //
        //       Thus when we retrieve the latest version of the AR, we get the most recent snapshot, then we get
        //       all events that are tied to this, in chronological order, then we apply them, then we return
        //       the latest snapshot.

        // store the event
        Event<T> event = command.getEvent();
        this.repository.storeEvent(event);

        // apply the event, getting the updated entity
        T snapshot = event.apply();

        // store a new snapshot, but only if necessary
        if (event.requiresSnapshot()) {
            this.repository.storeSnapshot(snapshot);
        }

        return snapshot;
    }

    protected T getLatest(String id) throws InvalidCommandTargetException {
        return this.repository.getLatest(id).orElseThrow(
                () -> new InvalidCommandTargetException(
                        String.format("Id %s does not identify valid aggregate root entity in the repository", id)) );
    }
}
