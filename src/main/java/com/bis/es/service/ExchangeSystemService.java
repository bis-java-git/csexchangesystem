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
     * Adds incoming orders to op[en order list
     * @param incomingOrder
     */
    void addToOpenOrder(final Order incomingOrder) throws OrderExecutionNotFoundException;

    /**
     * Process incoming order whether it can be executes or no
     *
     * @param incomingOrder
     */
    void tryToExecuteOrder(final Order incomingOrder) throws OrderExecutionNotFoundException;

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
     * @return average price
     */
    BigDecimal calculateAverageExchangedPrice(final String ricCode);

    /**
     * Returns all the executed orders.
     * @param ricCode
     * @return
     */
    List<Order> getExecutedOrders(final String ricCode);

}
