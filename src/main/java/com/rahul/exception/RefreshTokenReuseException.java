package com.rahul.exception;

public class RefreshTokenReuseException
        extends RuntimeException {

    public RefreshTokenReuseException(String message) {
        super(message);
    }
}