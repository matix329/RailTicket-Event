package com.railgraph.flink;

import java.time.LocalDateTime;

public class TicketEvent {
    private Long routeId;
    private int quantity;
    private String discountType;
    private String userId;
    private LocalDateTime timestamp;

    public TicketEvent() {}

    public TicketEvent(Long routeId, int quantity, String discountType, String userId, LocalDateTime timestamp) {
        this.routeId = routeId;
        this.quantity = quantity;
        this.discountType = discountType;
        this.userId = userId;
        this.timestamp = timestamp;
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
}