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
    private final Instant createdAt;

    /**
     * The instant of the last update
     */
    private Instant updatedAt;

    private final AggregateRootFactory<?> factory;

    /**
     * The collections of children, kept in a Map keyed by the locally-unique role that
     * describes the parent-child relationship (for example, a Person entity might have "achieved"
     * multiple qualifications; here the role is "achieved").  If there were two sets of qualifications (say,
     * "secondary", and "higher level", then these collections would have different roles.
     */
    private Map<String, ChildCollection<ChildEntity>> childCollections = new HashMap<>();

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

    public String toString() {
        return
                String.format("Type: %s%n", this.getClass().getSimpleName()) +
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
                    sb.append(String.format("<end>%n"));
                    sb.append(child.allChildren());  // Recurse
                });
            }
        });

        return sb.toString();
    }

}
