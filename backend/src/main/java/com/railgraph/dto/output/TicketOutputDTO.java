package com.railgraph.dto.output;

import com.railgraph.model.DiscountType;
import java.math.BigDecimal;

public class TicketOutputDTO {
    private Long id;
    private RouteOutputDTO route;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private DiscountType discountType;
    private int quantity;

    public TicketOutputDTO() {}

    public TicketOutputDTO(Long id, RouteOutputDTO route, BigDecimal basePrice, BigDecimal finalPrice, DiscountType discountType, int quantity) {
        this.id = id;
        this.route = route;
        this.basePrice = basePrice;
        this.finalPrice = finalPrice;
        this.discountType = discountType;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RouteOutputDTO getRoute() { return route; }
    public void setRoute(RouteOutputDTO route) { this.route = route; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}