package com.tavuc.exceptions;

public class ServerStartException extends Exception {

    /**
     * Constructs a new ServerStartException with the specified detail message.
     * @param message the detail message
     */
    public ServerStartException(String message) {
        super(message);
    }

    /**
     * Constructs a new ServerStartException with the specified detail message and cause.
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ServerStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
