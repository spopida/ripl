package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * The base class for all entities, whether they are aggregates or children
 */
@Getter
public abstract class Entity {

    /**
     * The id of this entity.  This remains immutable over the lifetime of
     * the entity, and as it evolves through different versions
     */
    private String id;

    /**
     * The version number of an instance
     */
    private int version;

    /**
     * The instant of creation
     */
    private final Instant createdAt;

    /**
     * The instant of the last update
     */
    private Instant updatedAt;

    /**
     * The {@link AggregateRootFactory} that was used to create this entity, and that governs any changes to it.
     */
    private final AggregateRootFactory<?> factory;

    /**
     * The collections of children, kept in a Map keyed by the factory-specific role that
     * describes the parent-child relationship (for example, a Person entity might have "achieved"
     * multiple qualifications; here the role is "achieved").
     *
     * In this context, factory-specific, means specific to the {@link AggregateRootFactory} that governs this entity.
     */
    private Map<String, ChildCollection<ChildEntity>> childCollections = new HashMap<>();

    /**
     * Construct an instance under the control of a given {@link AggregateRootFactory}
     * @param factory the {@link AggregateRootFactory that governs changes to this instance}
     * @param id the globally-unique identity of this entity
     */
    protected Entity(AggregateRootFactory<?> factory, String id) {
        this.factory = factory;
        this.id = id;
        this.version = 1;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.childCollections = new HashMap<>();

        // Initialise all the allowable child collections
        this.factory.getAllowedChildRelationships().forEach((s, parentChildRelationship) -> {
            // Only create a child collection if the class of this entity is the parent (or a sub-type)
            if (parentChildRelationship.getParentClass().isAssignableFrom(this.getClass()))
                this.childCollections.put(s, new ChildCollection<>());
        });
    }


    /**
     * Record a mutation in this, and all parent instances.  A mutation results in the version number of each affected
     * instance being incremented, and the last-update time being recorded.
     */
    protected void mutate() {
        // recurse up the parents until we find one that cannot be assigned to ChildEntity - that must be the root
        if (ChildEntity.class.isAssignableFrom(this.getClass())) {
            ((ChildEntity)this).getParent().mutate();
        }

        this.evolve();
    }

    /**
     * Increment the version number and reset the updatedAt instant
     */
    private void evolve() {
        this.version += 1;
        this.updatedAt = Instant.now();
    }

    /**
     * Get a {@link List} of child entities relating to a given role
     * @param role the role played by the parent entity in the relationships with children
     * @return a {@link List} of {@link ChildEntity} instances.  The list may be empty
     */
    public List<ChildEntity> getChildren(String role) {

        // TODO: throw exception if no map entry for role

        ChildCollection<ChildEntity> children = childCollections.get(role);
        return children.asList();
    }

    /**
     * Add a {@link ChildEntity} instance to this instance, associated with the given parent role
     * @param role the role of this (parent) instance with which to associate the child
     * @param child the {@link ChildEntity} to associate
     */
    public void addChild(String role, ChildEntity child) {
        Class<?> parentClass = this.getClass();
        Class<?> childClass = child.getClass();

        AggregateRootFactory.ParentChildRelationship rel = new AggregateRootFactory.ParentChildRelationship(this.getClass(), child.getClass());

        AggregateRootFactory.ParentChildRelationship found = this.factory.allowedChildRelationships.get(role);
        if (found == null) {
            throw new AggregateRootFactory.InvalidRelationshipTypeException(String.format("Class %s cannot be a child of %s%n", childClass.getName(), parentClass.getName()));
        }

        ChildCollection<ChildEntity> children = childCollections.get(role);
        children.add(child);
        AggregateRoot root = this.getRoot();
        root.addDescendent(child);
        this.mutate();
    }

    /**
     * Get the {@link AggregateRoot} instance of this entity
     * @return  the {@link AggregateRoot} entity at the root of the hierarchy of entities that this instance belongs to.
     *          If this instance <i>is</i> the aggregate root, then it will be returned.
     */
    public AggregateRoot getRoot() {
        // recurse up the parents until we find one that cannot be assigned to ChildEntity - that must be the root
        if (ChildEntity.class.isAssignableFrom(this.getClass())) {
            return ((ChildEntity)this).getParent().getRoot();
        } else
            return (AggregateRoot)this;
    }

    /**
     * Get all the entities that are children if this instance and are associated with a specific role, and that match a
     * predicate function.
     * @param role the role assocated with the parent-child relationships
     * @param p the predicate function used for matching
     * @return all matching children in the form of {@link ChildEntity} sub-class instances contained in a {@link List}
     * @param <T> the sub-type of the {@link ChildEntity} instances
     */
    @SuppressWarnings("unchecked") // TODO: See body
    public <T extends ChildEntity> List<T> findChildren(String role, Predicate<T> p) {
        List<T> result = new ArrayList<>();

        // Get the relationship meta data using the role
        AggregateRootFactory.ParentChildRelationship rel = this.factory.allowedChildRelationships.get(role);

        if (rel != null) {
            // Get all children that match the predicate
            ChildCollection<ChildEntity> children = this.childCollections.get(role);

            if (children != null) {
                for (ChildEntity child : children.asList()) {
                    if (child.getClass().isAssignableFrom(rel.getChildClass())) {
                        // See if the predicate holds
                        if (p.test((T)child)) {
                            // TODO: Try to resolve this warning (the one that happens without @SuppressWarnings)
                            result.add((T) child);
                        }
                    } else {
                        throw new ClassCastException(
                                String.format("Class %s is not assignable from class %s%n", child.getClass().getName(), rel.getChildClass().getName()));
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(String.format("Role %s does not identify a valid relationship", role));
        }

        return result;
    }

    /**
     * Get a {@link String} representation of this instance
     * @return this instance as a {@link String}.
     */
    public String toString() {
        return
                String.format("Type: %s%n", this.getClass().getSimpleName()) +
                String.format("Entity Id: %s%n", this.getId()) +
                String.format("Version: %d%n", this.version) +
                String.format("Created At: %s%n", this.createdAt) +
                String.format("Updated At: %s%n", this.updatedAt);

    }

    /**
     * Get a {@link String} representation of all children of this instance.  This is intended for diagnostic usage.
     * @return a {@link String} representation of all children of this instance.
     */
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
                    sb.append(String.format("<end>%n"));
                    sb.append(child.allChildren());  // Recurse
                });
            }
        });

        return sb.toString();
    }

}
