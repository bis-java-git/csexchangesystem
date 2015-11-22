package com.bis.es.service;

/**
 * When order does not match any of the opne order exception class is used.
 */
public class OrderExecutionNotFoundException extends RuntimeException {
    public OrderExecutionNotFoundException(final String message) {
        super(message);
    }
}
