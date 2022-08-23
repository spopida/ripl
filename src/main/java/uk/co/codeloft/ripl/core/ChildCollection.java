package uk.co.codeloft.ripl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChildCollection<T extends ChildEntity> {
    AtomicInteger lastId;

    List<T> collection;

    protected ChildCollection() {
        collection = new ArrayList<>();
        lastId = new AtomicInteger();
    }

    protected List<T> asList() {
        return this.collection;
    }
}
