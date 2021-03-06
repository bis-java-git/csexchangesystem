package com.bis.es.domain;

/**
 * ExchangeOrder
 * To keep integrity of executed order buy and sell together.
 */
public final class ExchangeSystemOrder {

    private final Order buyOrder;

    private final Order sellOrder;

    public ExchangeSystemOrder(final Order buyOrder, final Order sellOrder) {
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
    }

    public Order getSellOrder() {
        return sellOrder;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ExchangeSystemOrder{").append("fromOrder=" + buyOrder).append(", toOrder=" + sellOrder).toString();
    }
}
