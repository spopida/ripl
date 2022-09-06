package uk.co.codeloft.ripl.core;

public interface AggregateRootRepository<T extends AggregateRoot> {
    public T getLatest(String id);
    public void storeCommand(Command<T> command);
    public void storeEvent(Event<T> event);
    public void storeSnapshot(T snapshot);
}
