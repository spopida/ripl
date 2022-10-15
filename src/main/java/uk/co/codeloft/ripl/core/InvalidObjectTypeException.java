package uk.co.codeloft.ripl.core;

public class InvalidObjectTypeException extends Exception {
    public InvalidObjectTypeException(String id, String actualClass, String expectedClass) {
        super(String.format("Object with Id [%s] is expected to be of type %s, but is actually of type %s", id, expectedClass, actualClass));
    }
}
