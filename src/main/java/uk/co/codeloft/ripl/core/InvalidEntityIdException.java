package uk.co.codeloft.ripl.core;

public class InvalidEntityIdException extends Exception {
    public InvalidEntityIdException(String id) {
        super(String.format("Id [%s] is not a valid entity identifier", id));
    }
}
