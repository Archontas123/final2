package com.tavuc.exceptions;

public class GameJoinException extends Exception {

    /**
     * Constructs a new GameJoinException with the specified detail message.
     * @param message the detail message
     */
    public GameJoinException(String message) {
        super(message);
    }

    /**
     * Constructs a new GameJoinException with the specified detail message and cause.
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public GameJoinException(String message, Throwable cause) {
        super(message, cause);
    }
}
