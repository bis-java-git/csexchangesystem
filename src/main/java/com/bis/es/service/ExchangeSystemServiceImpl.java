package com.bis.es.service;

import ch.qos.logback.classic.Logger;
import com.bis.es.domain.ExchangeSystemOrder;
import com.bis.es.domain.Order;
import com.bis.es.domain.OrderType;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Exchange service to provide best order matching based on the following rules
 * This excerpt is taken from the requirement
 * When two orders are matched they are said to be ‘executed’, and the price at which they are executed (the execution price) is the price of the newly added order
 * If there are multiple matching orders at different prices for a new sell order, it should be matched against the order with the highest price
 * If there are multiple matching orders at the best price for a new order, it should be matched against the earliest matching existing orders
 * If there are multiple matching orders at different prices for a new buy order, it should be matched against the order with the lowest price
 */
public class ExchangeSystemServiceImpl implements ExchangeSystemService {

    final static Logger logger = (Logger) LoggerFactory.getLogger(ExchangeSystemServiceImpl.class);

    static final int DECIMAL_PLACES = 4;

    //In memory store to for all the open orders
    private final ConcurrentMap<String, Queue<Order>> openOrders = new ConcurrentHashMap<String, Queue<Order>>();

    //In memory store for all the executed orders
    private final ConcurrentMap<String, Queue<ExchangeSystemOrder>> exchangeOrders = new ConcurrentHashMap<String, Queue<ExchangeSystemOrder>>();


    /**
     * Matches new order with the open orders.
     */
    private Order matches(final Order newOrder, final List<Order> orderToProcessList) throws OrderExecutionNotFoundException {
        //Check whether order matches
        //1. Rules apply i.e. if sell ==2000 and buy ==2000
        //2. If there are multiple matching orders at different prices for a new sell order, it matches against the order with the highest price
        //3. If there are multiple matching orders at the best price for a new order, it matches against the earliest matching existing orders

        Order bestMatch = null;
        BigDecimal lastPrice = newOrder.getPrice();
        for (Order orderToMatch : orderToProcessList) {

            if ((orderToMatch.getQuantity() + newOrder.getQuantity()) != 0) {
                throw new OrderExecutionNotFoundException("Quantity for order differnt " + newOrder);
            }

            if (newOrder.getOrderType().equals(OrderType.BUY)) {
                if (orderToMatch.getPrice().compareTo(lastPrice) <= 0) {
                    lastPrice = orderToMatch.getPrice();
                    bestMatch = orderToMatch;
                }
            } else {
                if (orderToMatch.getPrice().compareTo(lastPrice) >= 0) {
                    lastPrice = orderToMatch.getPrice();
                    bestMatch = orderToMatch;
                }
            }
        }

        if (bestMatch != null) {
            logger.debug("Best match found {}", bestMatch);
            return bestMatch;
        }
        throw new OrderExecutionNotFoundException("Matching order not found " + newOrder);
    }

    /**
     * Adds new order to this queue for executing open orders
     */
    public void addToOpenOrder(final Order newOrder) throws OrderExecutionNotFoundException {
        Queue<Order> orderQueue = openOrders.get(newOrder.getRicCode());

        if (orderQueue == null) {
            orderQueue = new ConcurrentLinkedQueue<>();
            openOrders.put(newOrder.getRicCode(), orderQueue);
        }
        orderQueue.add(newOrder);
        logger.debug("Added new order to the open order list {} ",newOrder);
        //try to execute order
        tryToExecuteOrder(newOrder);
    }

    /**
     * It checks whether new order can be processed or no, if as per business rules order can be processed,
     * it will update open orders and Update execution order.
     */
    public void tryToExecuteOrder(final Order newOrder) throws OrderExecutionNotFoundException {
        final Order foundBestOrder = tryToFindBestOrderFit(newOrder);
        if (foundBestOrder != null) {
            Queue executionOrderQueue = exchangeOrders.get(foundBestOrder.getRicCode());
            if (executionOrderQueue == null) {
                executionOrderQueue = new ConcurrentLinkedQueue<Order>();
                exchangeOrders.put(foundBestOrder.getRicCode(), executionOrderQueue);
            }
            // Add it to the execution order queue
            executionOrderQueue.add(new ExchangeSystemOrder(foundBestOrder, newOrder));
            //remove it from the open orders queue
            Queue<Order> openOrderQueue = openOrders.get(foundBestOrder.getRicCode());
            openOrderQueue.remove(foundBestOrder);
            openOrderQueue.remove(newOrder);
            logger.debug("Found best order {} ", foundBestOrder.toString());
        } else {
            logger.debug("best order match is not found {} ",newOrder);
        }
    }

    /**
     * Gets all the open orders, used to provide open interest for a given RIC and direction
     * @param ricCode
     * @returns open orders
     */
    public Queue<Order> getOpenOrders(final String ricCode) {
        return openOrders.get(ricCode);

    }

    /**
     * Provide the average execution price for a given RIC
     * @return average execution price for a ric
     */
    public BigDecimal calculateAverageExchangedPrice(final String ricCode) {
        Queue<ExchangeSystemOrder> executionOrderQueue = exchangeOrders.get(ricCode);

        final BigDecimal totalAmount = executionOrderQueue.stream().map((x) -> x.getSellOrder().getPrice().
                multiply(new BigDecimal(x.getSellOrder().getQuantity()).abs(MathContext.DECIMAL128))).reduce((x, y) -> x.add(y)).get();

        final BigDecimal totalQuantity = executionOrderQueue.stream().map((x) -> new BigDecimal(x.getSellOrder().getQuantity()).abs(MathContext.DECIMAL128)).reduce((x, y) -> x.add(y)).get();

        return totalAmount.divide(totalQuantity, MathContext.DECIMAL128).setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    /**
     * Get all th executed orders for a ric
     */
    public List<Order> getExecutedOrders(final String ricCode) {
        Queue<ExchangeSystemOrder> executionOrderQueue = exchangeOrders.get(ricCode);
        List<Order> orderList = new ArrayList<>();
        for (ExchangeSystemOrder exchangeSystemOrder : executionOrderQueue) {
            orderList.add(exchangeSystemOrder.getSellOrder());
        }
        return orderList;
    }

    /**
     * Try to find best order fit for an new order.
     */
    private Order tryToFindBestOrderFit(final Order newOrder) throws OrderExecutionNotFoundException {
        Queue<Order> orderQueue = openOrders.get(newOrder.getRicCode());
        List<Order> orderToProcess;
        if (newOrder.getOrderType().equals(OrderType.BUY)) {
            //find all SELL orders
            orderToProcess = orderQueue.stream().filter(o -> o.getOrderType().equals(OrderType.SELL)).collect(Collectors.toList());
        } else { //Must be SELL
            orderToProcess = orderQueue.stream().filter(o -> o.getOrderType().equals(OrderType.BUY)).collect(Collectors.toList());
        }
        //Now match new order with the open order list
        return matches(newOrder, orderToProcess);
    }
}
