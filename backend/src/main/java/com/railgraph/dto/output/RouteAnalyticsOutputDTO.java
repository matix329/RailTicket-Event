package com.railgraph.dto.output;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RouteAnalyticsOutputDTO {
    private Long id;
    private Long routeId;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private Integer ticketsSold;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;
    private LocalDateTime createdAt;

    public RouteAnalyticsOutputDTO() {}

    public RouteAnalyticsOutputDTO(Long id, Long routeId, LocalDateTime windowStart, LocalDateTime windowEnd,
                                  Integer ticketsSold, BigDecimal totalRevenue, BigDecimal averagePrice,
                                  LocalDateTime createdAt) {
        this.id = id;
        this.routeId = routeId;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.ticketsSold = ticketsSold;
        this.totalRevenue = totalRevenue;
        this.averagePrice = averagePrice;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public LocalDateTime getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(LocalDateTime windowStart) {
        this.windowStart = windowStart;
    }

    public LocalDateTime getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(LocalDateTime windowEnd) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
