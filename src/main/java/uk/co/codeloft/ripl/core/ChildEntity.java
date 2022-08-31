package uk.co.codeloft.ripl.core;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ChildEntity extends Entity {

    private final Entity parent;

    public ChildEntity(ChildCreatedEvent<?, ?, ?> evt) {
        super(UUID.randomUUID().toString());
        this.parent = evt.getCommand().getParent();
    }
}
