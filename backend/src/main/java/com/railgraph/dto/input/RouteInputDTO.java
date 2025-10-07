package com.railgraph.dto.input;

import com.railgraph.model.TrainCategory;
import java.math.BigDecimal;

public class RouteInputDTO {
    private Long stationFromId;
    private Long stationToId;
    private int travelTimeMinutes;
    private BigDecimal price;
    private TrainCategory trainCategory;
    private String trainNumber;
    private int capacity;

    public RouteInputDTO() {}

    public RouteInputDTO(Long stationFromId, Long stationToId, int travelTimeMinutes, BigDecimal price) {
        this.stationFromId = stationFromId;
        this.stationToId = stationToId;
        this.travelTimeMinutes = travelTimeMinutes;
        this.price = price;
    }

    public Long getStationFromId() { return stationFromId; }
    public void setStationFromId(Long stationFromId) { this.stationFromId = stationFromId; }

    public Long getStationToId() { return stationToId; }
    public void setStationToId(Long stationToId) { this.stationToId = stationToId; }

    public int getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(int travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public TrainCategory getTrainCategory() {
        return trainCategory;
    }

    public void setTrainCategory(TrainCategory trainCategory) {
        this.trainCategory = trainCategory;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}