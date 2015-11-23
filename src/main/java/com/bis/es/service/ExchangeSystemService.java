package com.bis.es.service;

import com.bis.es.domain.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;

/**
 * Interface foe exchange service to provide pricing services
 */
public interface ExchangeSystemService {

    /**
     * Adds new order to open order list
     * @param newOrder new incoming order
     */
    void addToOpenOrder(final Order newOrder) throws OrderExecutionNotFoundException;

    /**
     * Process new order whether it can be executed or no
     *
     * @param newOrder new incoming order
     */
    void tryToExecuteOrder(final Order newOrder) throws OrderExecutionNotFoundException;

    /**
     * Gets all the open order based on the ric
     *
     * @param ricCode
     * @return open order Queue is returned.
     */
    Queue<Order> getOpenOrders(final String ricCode);

    /**
     * Calculates average execution price for a given ric.
     *
     * @param ricCode
     * @return average price for executed stock for ric
     */
    BigDecimal calculateAverageExchangedPrice(final String ricCode);

    /**
     * Returns all the executed orders.
     * @param ricCode
     * @return all executed orders matching buy/sell
     */
    List<Order> getExecutedOrders(final String ricCode);

}
