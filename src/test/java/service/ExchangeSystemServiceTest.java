package service;

import com.bis.es.domain.Order;
import com.bis.es.domain.OrderType;
import com.bis.es.service.ExchangeSystemService;
import com.bis.es.service.ExchangeSystemServiceImpl;
import com.bis.es.service.OrderExecutionNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit and Acceptance Tests to see all the business criteria are met for exchange system.
 */
public class ExchangeSystemServiceTest {

    public static final String VOD_L = "VOD.l";

    public static final String USER_1 = "User1";

    public static final String USER_2 = "User2";

    private ExchangeSystemService exchangeSystemService = null;

    @Before
    public void setup() {
        exchangeSystemService = new ExchangeSystemServiceImpl();
    }

    @Test
    public void shouldFindPerfectMatchingOrderTestWhenbothStockPricesAreSame() throws OrderExecutionNotFoundException {
        shouldFindPerfectMatchInitialTest();
    }

    @Test
    public void shouldMatchExistingStockSellPriceWithTheHighestStockPriceTest() throws OrderExecutionNotFoundException {
        shouldFindMatchWithHighhOrderPriceTest();
    }

    @Test
    public void shouldMatchExistingStockBuyPriceWithTheLowestPriceTest() throws OrderExecutionNotFoundException {

        shouldFindMatchWithHighhOrderPriceTest();

        final Order sellOrder = new Order(VOD_L, new BigDecimal("98"), -1000, OrderType.SELL, USER_2);

        exchangeSystemService.addToOpenOrder(sellOrder);

        //Open interest is the total quantity of all open orders for the given RIC and direction at each price point
        testOpenOrders(VOD_L, 1);

        //Executed order should be 1
        assertTrue("Total executed orders  should be 3 ", exchangeSystemService.getExecutedOrders(VOD_L).size() == 3);

        //Test Quantity for execution
        List<Order> orderList = exchangeSystemService.getExecutedOrders(VOD_L);
        assertTrue("Total executed orders  should be 3", orderList.size() == 3);
        assertEquals(orderList.get(0).getQuantity(), 1000);
        assertEquals(orderList.get(1).getQuantity(), 500);
        assertEquals(orderList.get(2).getQuantity(), -1000);

        //Test Average execution price
        testAverageExecutionPrice(VOD_L, new BigDecimal("99.8800"));
    }

    //Provide open interest for a given RIC and direction
    private void testOpenOrders(final String ricCode, int totalOpenOrders) {
        final Queue<Order> openOrders = exchangeSystemService.getOpenOrders(ricCode);
        assertEquals("Total expected orders for [" + ricCode + "+]", totalOpenOrders, openOrders.size());
    }

    //Provide the average execution price for a given RIC
    private void testAverageExecutionPrice(final String ricCode, final BigDecimal expectedExecutionPrice) {
        final BigDecimal averagedPrice = exchangeSystemService.calculateAverageExchangedPrice(ricCode);
        assertEquals(expectedExecutionPrice, averagedPrice);
    }

    private void shouldFindMatchWithHighhOrderPriceTest() throws OrderExecutionNotFoundException {

        shouldFindPerfectMatchInitialTest();

        final Order buyOrder1 = new Order(VOD_L, new BigDecimal("99"), 1000, OrderType.BUY, USER_1);

        final Order buyOrder2 = new Order(VOD_L, new BigDecimal("101"), 1000, OrderType.BUY, USER_1);

        final Order buyOrder3 = new Order(VOD_L, new BigDecimal("103"), 500, OrderType.BUY, USER_1);

        final Order sellOrder = new Order(VOD_L, new BigDecimal("102"), -500, OrderType.SELL, USER_2);

        executeNewOrder(buyOrder1);

        executeNewOrder(buyOrder2);

        executeNewOrder(sellOrder);
        //Open interest is the total quantity of all open orders for the given RIC and direction at each price point
        testOpenOrders(VOD_L, 3);

        exchangeSystemService.addToOpenOrder(buyOrder3);
        //Open interest is the total quantity of all open orders for the given RIC and direction at each price point
        testOpenOrders(VOD_L, 2);

        //Executed order should be 1
        assertTrue("Total executed orders  should be 2 ", exchangeSystemService.getExecutedOrders(VOD_L).size() == 2);

        //Test Quantity for execution
        List<Order> orderList = exchangeSystemService.getExecutedOrders(VOD_L);
        assertTrue("Total executed orders  should be more than zero ", orderList.size() == 2);
        assertEquals(orderList.get(0).getQuantity(), 1000);
        assertEquals(orderList.get(1).getQuantity(), 500);

        //Test Average execution price
        testAverageExecutionPrice(VOD_L, new BigDecimal("101.1333"));
    }

    private void shouldFindPerfectMatchInitialTest() throws OrderExecutionNotFoundException {
        final Order buyOrder = new Order(VOD_L, new BigDecimal("100.2"), -1000, OrderType.SELL, USER_1);

        final Order sellOrder = new Order(VOD_L, new BigDecimal("100.2"), 1000, OrderType.BUY, USER_2);

        //Open interest is the total quantity of all open orders for the given RIC and direction at each price point
        //Find perfect matching order for USER_1
        executeNewOrder(buyOrder);

        //Open interest is the total quantity of all open orders for the given RIC and direction at each price point
        testOpenOrders(VOD_L, 1);

        //Find perfect matching order for USER_2, should be executed
        exchangeSystemService.addToOpenOrder(sellOrder);

        //Total open orders should be zero
        assertTrue("Total open orders  should be zero at this point ", exchangeSystemService.getOpenOrders(VOD_L).size() == 0);

        //Test Average execution price
        testAverageExecutionPrice(VOD_L, new BigDecimal("100.2000"));

        //Test Quantity for execution
        List<Order> orderList = exchangeSystemService.getExecutedOrders(VOD_L);
        //Executed order should be 1
        assertTrue("Total executed orders  should be 1 ", exchangeSystemService.getExecutedOrders(VOD_L).size() == 1);
        assertEquals(orderList.get(0).getQuantity(), 1000);

    }

    /**
     *  Execute new order and suppress Exception because order match may not be available at this point in time
     */
    private void executeNewOrder(final Order order) {
        try {
            exchangeSystemService.addToOpenOrder(order);
        } catch (OrderExecutionNotFoundException e) {
            //Nothing to do, we dont have matching order this point in time.
        }
    }

}
