package com.railgraph.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_analytics")
public class RouteAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "tickets_sold", nullable = false)
    private Integer ticketsSold;

    @Column(name = "total_revenue", precision = 10, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "average_price", precision = 10, scale = 2)
    private BigDecimal averagePrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public RouteAnalytics() {
        this.createdAt = LocalDateTime.now();
    }

    public RouteAnalytics(Long routeId, LocalDateTime windowStart, LocalDateTime windowEnd,
                         Integer ticketsSold, BigDecimal totalRevenue, BigDecimal averagePrice) {
        this.routeId = routeId;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.ticketsSold = ticketsSold;
        this.totalRevenue = totalRevenue;
        this.averagePrice = averagePrice;
        this.createdAt = LocalDateTime.now();
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