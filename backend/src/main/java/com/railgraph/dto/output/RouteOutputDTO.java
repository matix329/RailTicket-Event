package com.railgraph.dto.output;

import com.railgraph.model.TrainCategory;
import java.math.BigDecimal;

public class RouteOutputDTO {
    private Long id;
    private StationOutputDTO stationFrom;
    private StationOutputDTO stationTo;
    private int travelTimeMinutes;
    private BigDecimal price;
    private TrainCategory trainCategory;
    private String trainNumber;
    private int capacity;
    private int availableSeats;

    public RouteOutputDTO() {}

    public RouteOutputDTO(Long id, StationOutputDTO stationFrom, StationOutputDTO stationTo, int travelTimeMinutes, BigDecimal price) {
        this.id = id;
        this.stationFrom = stationFrom;
        this.stationTo = stationTo;
        this.travelTimeMinutes = travelTimeMinutes;
        this.price = price;
    }

    public RouteOutputDTO(Long id, StationOutputDTO stationFrom, StationOutputDTO stationTo, int travelTimeMinutes, BigDecimal price, TrainCategory trainCategory, String trainNumber, int capacity, int availableSeats) {
        this.id = id;
        this.stationFrom = stationFrom;
        this.stationTo = stationTo;
        this.travelTimeMinutes = travelTimeMinutes;
        this.price = price;
        this.trainCategory = trainCategory;
        this.trainNumber = trainNumber;
        this.capacity = capacity;
        this.availableSeats = availableSeats;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StationOutputDTO getStationFrom() { return stationFrom; }
    public void setStationFrom(StationOutputDTO stationFrom) { this.stationFrom = stationFrom; }

    public StationOutputDTO getStationTo() { return stationTo; }
    public void setStationTo(StationOutputDTO stationTo) { this.stationTo = stationTo; }

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

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}