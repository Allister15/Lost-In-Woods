package com.LostInWoods.app.exception;

/**
 * Thrown when an invalid game action is attempted
 */
public class InvalidGameStateException extends RuntimeException {

    public InvalidGameStateException(String message) {
        super(message);
    }

    public InvalidGameStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
