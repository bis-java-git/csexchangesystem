package com.bis.es.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order Domain Class for Executing orders and Open orders
 */
public final class Order {

    /**
     * Reuters Instrument Code
     */
    private final String ricCode;

    /**
     * Order Buy/Sell price
     */
    private final BigDecimal price;

    /**
     * Number of rics to be bought or sold
     */
    private final int quantity;

    /**
     * Consumer buying or selling rics
     */
    private final String user;

    /**
     * OrderType buy or sell
     */
    private OrderType orderType;

    /**
     * Unique ID for the order
     */
    private final UUID uniqueId;


    public Order(final String ricCode,
                 final BigDecimal price,
                 final int quantity,
                 final OrderType orderType,
                 final String user) {
        this.ricCode = ricCode;
        this.price = price;
        this.quantity = quantity;
        this.orderType = orderType;
        this.user = user;
        uniqueId = UUID.randomUUID();

    }

    public String getRicCode() {
        return ricCode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;

        Order order = (Order) o;

        if (quantity != order.quantity) return false;
        if (orderType != order.orderType) return false;
        if (price != null ? !price.equals(order.price) : order.price != null) return false;
        if (ricCode != null ? !ricCode.equals(order.ricCode) : order.ricCode != null) return false;
        if (uniqueId != null ? !uniqueId.equals(order.uniqueId) : order.uniqueId != null) return false;
        if (user != null ? !user.equals(order.user) : order.user != null) return false;

        return true;

    }

    @Override
    public int hashCode() {
        int result = ricCode != null ? ricCode.hashCode() : 0;
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + quantity;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (orderType != null ? orderType.hashCode() : 0);
        result = 31 * result + (uniqueId != null ? uniqueId.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return new StringBuilder().append("Order{").append("ricCode='" + ricCode + '\'').append(", price=" + price).append(", quantity=" + quantity).append(", user='" + user + '\'').append(", orderType=" + orderType).append(", uniqueId=" + uniqueId).append("}").toString();
    }
}
