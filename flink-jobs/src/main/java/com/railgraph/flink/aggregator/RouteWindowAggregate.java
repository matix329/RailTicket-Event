package com.railgraph.flink.aggregator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RouteWindowAggregate {
    private Long routeId;
    private int totalTicketsSold;
    private BigDecimal totalRevenue;
    private List<BigDecimal> prices;  // To calculate average

    public RouteWindowAggregate() {
        this.totalTicketsSold = 0;
        this.totalRevenue = BigDecimal.ZERO;
        this.prices = new ArrayList<>();
    }

    public RouteWindowAggregate(Long routeId) {
        this.routeId = routeId;
        this.totalTicketsSold = 0;
        this.totalRevenue = BigDecimal.ZERO;
        this.prices = new ArrayList<>();
    }

    public void addTicket(int quantity, BigDecimal price) {
        this.totalTicketsSold += quantity;
        if (price != null) {
            this.totalRevenue = this.totalRevenue.add(price);
            this.prices.add(price);
        }
    }

    public BigDecimal getAveragePrice() {
        if (prices.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(prices.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public int getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(int totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<BigDecimal> getPrices() {
        return prices;
    }

    public void setPrices(List<BigDecimal> prices) {
        this.prices = prices;
    }

    public void merge(RouteWindowAggregate other) {
        this.totalTicketsSold += other.totalTicketsSold;
        this.totalRevenue = this.totalRevenue.add(other.totalRevenue);
        this.prices.addAll(other.prices);
    }

    @Override
    public String toString() {
        return "RouteWindowAggregate{" +
                "routeId=" + routeId +
                ", totalTicketsSold=" + totalTicketsSold +
                ", totalRevenue=" + totalRevenue +
                ", averagePrice=" + getAveragePrice() +
                '}';
    }
}
