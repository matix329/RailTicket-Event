package com.railgraph.dto.input;

import com.railgraph.model.DiscountType;

public class TicketInputDTO {
    private Long routeId;
    private int quantity;
    private DiscountType discountType;

    public TicketInputDTO() {}

    public TicketInputDTO(Long routeId, int quantity, DiscountType discountType) {
        this.routeId = routeId;
        this.quantity = quantity;
        this.discountType = discountType;
    }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
}