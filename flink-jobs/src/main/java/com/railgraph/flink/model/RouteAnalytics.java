package com.railgraph.flink.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class RouteAnalytics {
    private Long routeId;
    private Timestamp windowStart;
    private Timestamp windowEnd;
    private Integer ticketsSold;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;

    public RouteAnalytics() {}

    public RouteAnalytics(Long routeId, Timestamp windowStart, Timestamp windowEnd,
                         Integer ticketsSold, BigDecimal totalRevenue, BigDecimal averagePrice) {
        this.routeId = routeId;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.ticketsSold = ticketsSold;
        this.totalRevenue = totalRevenue;
        this.averagePrice = averagePrice;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Timestamp getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Timestamp windowStart) {
        this.windowStart = windowStart;
    }

    public Timestamp getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(Timestamp windowEnd) {
        this.windowEnd = windowEnd;
    }

    public Integer getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(Integer ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    @Override
    public String toString() {
        return "RouteAnalytics{" +
                "routeId=" + routeId +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                ", ticketsSold=" + ticketsSold +
                ", totalRevenue=" + totalRevenue +
                ", averagePrice=" + averagePrice +
                '}';
    }
}
