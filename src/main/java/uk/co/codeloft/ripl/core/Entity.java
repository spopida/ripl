package uk.co.codeloft.ripl.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * The base class for all entities, whether they are aggregates or children
 */
@Getter
public abstract class Entity {

    /**
     * Represents an occurrence of an attempt to register an invalid relationship type.  If this happens
     * There is little the client can do; a coding fix is needed, hence this is a RuntimeException
     */
    private static class InvalidRelationshipTypeException extends RuntimeException {

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
    private static class ParentChildRelationship {
        private final Class<?> parentClass;
        private final Class<?> childClass;

        protected ParentChildRelationship(Class<?> parent, Class<?> child) {
            // The parent class must be Entity or a sub-type
            if (!Entity.class.isAssignableFrom(parent)) {
                throw new InvalidRelationshipTypeException(Entity.class.getName() + " must be assignable from " + parent.getName());
            }

            // The child class must be ChildEntity or a sub-type
            if (!ChildEntity.class.isAssignableFrom(child)) {
                throw new InvalidRelationshipTypeException(ChildEntity.class.getName() + " must be assignable from " + child.getName());
            }

            this.parentClass = parent;
            this.childClass = child;
        }
    }

    /**
     * A static map of declared parent-child relationships.  Such relationships are class-level so
     * there is no need for instance-level values
     */
    private static final Map<String, ParentChildRelationship> allowedRelationships = new HashMap<>();

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
    public static void allowRelationship(Class<?> parentClass, Class<?> childClass, String role) {

        // TODO: throw an exception if role already exists for the parent class

        ParentChildRelationship rel = new ParentChildRelationship(parentClass, childClass);
        Entity.allowedRelationships.put(role, rel);
    }

    public static boolean isAllowedRelationship(Class<?> expectedParentClass, Class<?> expectedChildClass, String role) {
        ParentChildRelationship rel = Entity.allowedRelationships.get(role);

        return rel != null && rel.childClass == expectedChildClass && rel.parentClass == expectedParentClass;
    }
    //-- Non-static members --//

    /**
     * The id of this entity.  This remains immutable over the lifetime of
     * the entity, and as it evolves through different versions
     */
    private String id;

    /**
     * The version number of this instance
     */
    private int version;

    /**
     * The instant of creation
     */
    private Instant createdAt;

    /**
     * The instant of the last update
     */
    private Instant updatedAt;

    /**
     * The collections of children, kept in a Map keyed by the locally-unique role that
     * describes the parent-child relationship (for example, a Person entity might have "achieved"
     * multiple qualifications; here the role is "achieved").  If there were two sets of qualifications (say,
     * "secondary", and "higher level", then these collections would have different roles.
     */
    private Map<String, ChildCollection<ChildEntity>> childCollections = new HashMap<>();

    protected Entity(String id) {
        this.id = id;
        this.version = 1;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.childCollections = new HashMap<>();

        // Initialise all the allowable child collections
        Entity.allowedRelationships.forEach((s, parentChildRelationship) -> {
            // Only create a child collection if the class of this entity is the parent (or a sub-type)
            if (parentChildRelationship.parentClass.isAssignableFrom(this.getClass()))
                this.childCollections.put(s, new ChildCollection<>());
        });
    }

    public final Entity setId(String id) {
        this.id = id;
        return this;
    }

    protected void mutate() {
        // recurse up the parents until we find one that cannot be assigned to ChildEntity - that must be the root
        if (ChildEntity.class.isAssignableFrom(this.getClass())) {
            ((ChildEntity)this).getParent().mutate();
        }

        this.evolve();
    }

    private void evolve() {
        this.version += 1;
        this.updatedAt = Instant.now();
    }

    public List<ChildEntity> getChildren(String role) {

        // TODO: throw exception if no map entry for role

        ChildCollection<ChildEntity> children = childCollections.get(role);
        return children.asList();
    }

    public void addChild(String role, ChildEntity child) {
        Class<?> parentClass = this.getClass();
        Class<?> childClass = child.getClass();

        ParentChildRelationship rel = new ParentChildRelationship(this.getClass(), child.getClass());

        ParentChildRelationship found = allowedRelationships.get(role);
        if (found == null) {
            throw new InvalidRelationshipTypeException(String.format("Class %s cannot be a child of %s%n", childClass.getName(), parentClass.getName()));
        }

        ChildCollection<ChildEntity> children = childCollections.get(role);
        children.add(child);
        AggregateRoot root = this.getRoot();
        root.addDescendent(child);
        this.mutate();
    }

    public AggregateRoot getRoot() {
        // recurse up the parents until we find one that cannot be assigned to ChildEntity - that must be the root
        if (ChildEntity.class.isAssignableFrom(this.getClass())) {
            return ((ChildEntity)this).getParent().getRoot();
        } else
            return (AggregateRoot)this;
    }

    @SuppressWarnings("unchecked") // TODO: See body
    public <T extends ChildEntity> List<T> findChildren(String role, Predicate<T> p) {
        List<T> result = new ArrayList<>();

        // Get the relationship meta data using the role
        ParentChildRelationship rel = Entity.allowedRelationships.get(role);

        if (rel != null) {
            // Get all children that match the predicate
            ChildCollection<ChildEntity> children = this.childCollections.get(role);

            if (children != null) {
                for (ChildEntity child : children.asList()) {
                    if (child.getClass().isAssignableFrom(rel.childClass)) {
                        // See if the predicate holds
                        if (p.test((T)child)) {
                            // TODO: Try to resolve this warning (the one that happens without @SuppressWarnings)
                            result.add((T) child);
                        }
                    } else {
                        throw new ClassCastException(
                                String.format("Class %s is not assignable from class %s%n", child.getClass().getName(), rel.childClass.getName()));
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(String.format("Role %s does not identify a valid relationship", role));
        }

        return result;
    }

    public String toString() {
        return
                String.format("Entity Id: %s%n", this.getId()) +
                String.format("Version: %d%n", this.version) +
                String.format("Created At: %s%n", this.createdAt) +
                String.format("Updated At: %s%n", this.updatedAt);

    }

    public String allChildren() {
        StringBuilder sb = new StringBuilder();

        // Iterate through each child collection, stringifying the children in each one
        this.childCollections.forEach((role, collection) -> {
            List<ChildEntity> list = collection.asList();
            if (!list.isEmpty()) {
                sb.append(String.format(">>>>%n"));
                sb.append(String.format("** Role: %s **%n", role));
                list.forEach(child -> {
                    sb.append(String.format("----------%n"));
                    sb.append(child.toString());
                });
            }
        });

        return sb.toString();
    }

}
