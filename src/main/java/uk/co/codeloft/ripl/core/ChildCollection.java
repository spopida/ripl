package uk.co.codeloft.ripl.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class ChildCollection<T extends ChildEntity> {

    private AtomicInteger lastId;
    private List<T> collection;

    protected ChildCollection() {
        collection = new ArrayList<>();
        lastId = new AtomicInteger();
    }

    protected List<T> asList() {
        return this.collection;
    }

    protected void add(T child) {
        collection.add(child);
    }
}
