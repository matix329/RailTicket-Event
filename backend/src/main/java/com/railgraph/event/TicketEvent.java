package com.railgraph.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketEvent {
    private Long routeId;
    private int quantity;
    private String discountType;
    private String userId;
    private LocalDateTime timestamp;
    private BigDecimal finalPrice;  // Price after discount applied

    public TicketEvent() {}

    public TicketEvent(Long routeId, int quantity, String discountType, String userId, LocalDateTime timestamp) {
        this.routeId = routeId;
        this.quantity = quantity;
        this.discountType = discountType;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public TicketEvent(Long routeId, int quantity, String discountType, String userId, LocalDateTime timestamp, BigDecimal finalPrice) {
        this.routeId = routeId;
        this.quantity = quantity;
        this.discountType = discountType;
        this.userId = userId;
        this.timestamp = timestamp;
        this.finalPrice = finalPrice;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
}