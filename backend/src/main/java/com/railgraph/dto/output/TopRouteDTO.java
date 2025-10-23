package com.railgraph.dto.output;

import java.math.BigDecimal;

public class TopRouteDTO {
    private Long routeId;
    private String routeName;  // e.g., "Köln -> Düsseldorf"
    private Integer totalTicketsSold;
    private BigDecimal totalRevenue;
    private Integer rank;

    public TopRouteDTO() {}

    public TopRouteDTO(Long routeId, String routeName, Integer totalTicketsSold,
                      BigDecimal totalRevenue, Integer rank) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.totalTicketsSold = totalTicketsSold;
        this.totalRevenue = totalRevenue;
        this.rank = rank;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Integer getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(Integer totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
