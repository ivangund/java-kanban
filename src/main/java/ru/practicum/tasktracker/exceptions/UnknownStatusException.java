package main.java.ru.practicum.tasktracker.exceptions;

public class UnknownStatusException extends RuntimeException {

    public UnknownStatusException(final String message) {
        super(message);
    }
}