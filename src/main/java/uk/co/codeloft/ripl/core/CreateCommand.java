package uk.co.codeloft.ripl.core;

/**
 * A command to create an instance of an AggregateRoot sub-class.  Create commands do not target an existing
 * instance (unlike update commands)
 * @param <T> the type of the AggregateRoot
 */
public abstract class CreateCommand<T extends AggregateRoot> extends Command<T> {
    public CreateCommand() {
        super();
    }

}
