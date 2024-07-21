package main.java.ru.practicum.tasktracker.exceptions;

public class ManagerLoadException extends RuntimeException {

    public ManagerLoadException(final String message) {
        super(message);
    }
}