package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.*;

/**
 * The base class for all entities, whether they are aggregates or children
 */
@Getter
public abstract class Entity {

    /**
     * Represents an occurrence of an attempt to register an invalid relationship type.  If this happens
     * There is little the client can do; a coding fix is needed, hence this is a RuntimeException
     */
    private static class InvalidRelationshipException extends RuntimeException {

        protected InvalidRelationshipException(String message) {
            super(message);
        }
    }

    /**
     * Nested class used to represent a possible parent-child relationship between a parent class and a child class
     */
    private static class ParentChildRelationship {
        private Class parentClass;
        private Class childClass;

        protected ParentChildRelationship(Class parent, Class child) {
            // The parent class must be Entity or a sub-type
            if (!Entity.class.isAssignableFrom(parent)) {
                throw new InvalidRelationshipException(Entity.class.getName() + " must be assignable from " + parent.getName());
            }

            // The child class must be ChildEntity or a sub-type
            if (!ChildEntity.class.isAssignableFrom(child)) {
                throw new InvalidRelationshipException(ChildEntity.class.getName() + " must be assignable from " + child.getName());
            }

            this.parentClass = parent;
            this.childClass = child;
        }
    }

    /**
     * A static map of declared parent-child relationships.  Such relationships are class-level so
     * there is no need for instance-level values
     */
    private static Map<String, ParentChildRelationship> allowedRelationships = new HashMap<>();

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
    public static void allowCollection(Class parentClass, Class childClass, String role) {

        // TODO: throw an exception if role already exists for the parent class

        ParentChildRelationship rel = new ParentChildRelationship(parentClass, childClass);
        Entity.allowedRelationships.put(role, rel);
    }

    //-- Non-static members --//

    /**
     * The id of this entity.  This remains immutable over the lifetime of
     * the entity, and as it evolves through different versions
     */
    private String id;

    /**
     * The collections of children, kept in a Map keyed by the locally-unique role that
     * describes the parent-child relationship (for example, a Person entity might have "achieved"
     * multiple qualifications; here the role is "achieved").  If there were two sets of qualifications (say,
     * "secondary", and "higher level", then these collections would have different roles.
     */
    private Map<String, ChildCollection<ChildEntity>> childCollections = new HashMap<>();

    protected Entity(String id) {
        this.id = id;
        this.childCollections = new HashMap<>();

        // Initialise all the allowable child collections
        Entity.allowedRelationships.forEach((s, parentChildRelationship) -> {
            this.childCollections.put(s, new ChildCollection<>());
        });
    }

    public final Entity setId(String id) {
        this.id = id;
        return this;
    }

    public List<ChildEntity> getChildren(String role) {

        // TODO: throw exception if no map entry for role

        ChildCollection<ChildEntity> children = childCollections.get(role);
        return children.asList();
    }
}
