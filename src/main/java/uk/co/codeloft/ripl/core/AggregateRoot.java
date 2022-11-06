package uk.co.codeloft.ripl.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class AggregateRoot extends Entity {

    /**
     * Represents an occurrence of an attempt to register an invalid relationship type.  If this happens
     * There is little the client can do; a coding fix is needed, hence this is a RuntimeException
     */
/*
    static class InvalidRelationshipTypeException extends RuntimeException {

        protected InvalidRelationshipTypeException(String message) {
            super(message);
        }
    }
*/

    /**
     * Represents an occurrence of an attempt to register an invalid relationship instance.
     */
/*
    public static class InvalidRelationshipInstanceException extends Command.PreConditionException {

        protected InvalidRelationshipInstanceException(String message) {
            super(message);
        }
    }
*/

    /**
     * Nested class used to represent a parent-child relationship between a parent class and a child class
     */
/*
    @Getter
    protected static class ParentChildRelationship {
        private final Class<?> parentClass;
        private final Class<?> childClass;

        protected ParentChildRelationship(Class<?> parent, Class<?> child) {
            // The parent class must be Entity or a sub-type
            if (!Entity.class.isAssignableFrom(parent)) {
                throw new AggregateRoot.InvalidRelationshipTypeException(Entity.class.getName() + " must be assignable from " + parent.getName());
            }

            // The child class must be ChildEntity or a sub-type
            if (!ChildEntity.class.isAssignableFrom(child)) {
                throw new AggregateRoot.InvalidRelationshipTypeException(ChildEntity.class.getName() + " must be assignable from " + child.getName());
            }

            this.parentClass = parent;
            this.childClass = child;
        }
    }
*/

    /**
     * A static map of declared parent-child relationships.  Such relationships are class-level so
     * there is no need for instance-level values
     */
/*
    protected static final Map<String, AggregateRoot.ParentChildRelationship> allowedRelationships = new HashMap<>();
*/

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
/*
    public static void allowRelationship(Class<?> parentClass, Class<?> childClass, String role) {

        // TODO: throw an exception if role already exists for the parent class

        AggregateRoot.ParentChildRelationship rel = new AggregateRoot.ParentChildRelationship(parentClass, childClass);
        AggregateRoot.allowedRelationships.put(role, rel);
    }

    public static boolean isAllowedRelationship(Class<?> expectedParentClass, Class<?> expectedChildClass, String role) {
        AggregateRoot.ParentChildRelationship rel = AggregateRoot.allowedRelationships.get(role);

        return rel != null && rel.childClass == expectedChildClass && rel.parentClass == expectedParentClass;
    }
*/

    /**
     * The id of the snapshot that underpins this version of the aggregate.
     */
    private final String snapshotId;

    private final CreatedEvent<?, ?> createdEvent;

    /**
     * A flat map of all descendents
     */
    private final Map<String, Entity> allDescendents;

    private AggregateRootFactory<?> factory;

    protected void setFactory(AggregateRootFactory<?> f) {
        this.factory = f;
    }

    public AggregateRoot(CreatedEvent<?, ?> evt) {
        super(evt.getFactory(), UUID.randomUUID().toString());
        this.snapshotId = UUID.randomUUID().toString();
        this.createdEvent = evt;
        this.allDescendents = new HashMap<>();
        // Special case - we put this AggregateRoot instance in its map of descendents
        this.allDescendents.put(this.getId(), this);
    }

    public final void addDescendent(ChildEntity child) {
        this.allDescendents.put(child.getId(), child);
    }

    public final Optional<Entity> getDescendent(String entityId, Class<?> clazz) throws InvalidObjectTypeException {

        Entity e = allDescendents.get(entityId);
        if ((e != null) && (!e.getClass().isAssignableFrom(clazz)))
            throw new InvalidObjectTypeException(entityId, clazz.getName(), e.getClass().getName());

        return Optional.ofNullable(allDescendents.get(entityId));
    }

    public String toString() {
        return super.toString() + this.asString();
    }

    private String asString() {
        return
                String.format("From Snapshot: %s%n", this.getSnapshotId());
    }
}
