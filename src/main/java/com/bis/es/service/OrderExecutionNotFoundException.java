package com.bis.es.service;

/**
 * When order does not match any of the open order runtime exception is used.
 */
public class OrderExecutionNotFoundException extends RuntimeException {
    public OrderExecutionNotFoundException(final String message) {
        super(message);
    }
}
