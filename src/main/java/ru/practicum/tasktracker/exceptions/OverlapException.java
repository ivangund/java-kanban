package main.java.ru.practicum.tasktracker.exceptions;

public class OverlapException extends RuntimeException {

    public OverlapException(final String message) {
        super(message);
    }
}