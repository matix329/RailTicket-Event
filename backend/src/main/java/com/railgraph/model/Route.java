package com.railgraph.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_from_id", nullable = false)
    private Station stationFrom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_to_id", nullable = false)
    private Station stationTo;

    @Column(name = "travel_time_minutes", nullable = false)
    private int travelTimeMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private TrainCategory trainCategory;

    private String trainNumber;
    private int capacity;
    private int availableSeats;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    public Route() {}

    public Route(Station stationFrom, Station stationTo, int travelTimeMinutes, BigDecimal price) {
        this.stationFrom = stationFrom;
        this.stationTo = stationTo;
        this.travelTimeMinutes = travelTimeMinutes;
        this.price = price;
    }

    public Route(Station stationFrom, Station stationTo, int travelTimeMinutes, BigDecimal price, TrainCategory trainCategory, String trainNumber, int capacity) {
        this.stationFrom = stationFrom;
        this.stationTo = stationTo;
        this.travelTimeMinutes = travelTimeMinutes;
        this.price = price;
        this.trainCategory = trainCategory;
        this.trainNumber = trainNumber;
        this.capacity = capacity;
        this.availableSeats = capacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Station getStationFrom() { return stationFrom; }
    public void setStationFrom(Station stationFrom) { this.stationFrom = stationFrom; }

    public Station getStationTo() { return stationTo; }
    public void setStationTo(Station stationTo) { this.stationTo = stationTo; }

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

    public boolean hasAvailableSeats(int quantity) {
        return availableSeats >= quantity;
    }

    public void reserveSeats(int quantity) {
        if (!hasAvailableSeats(quantity)) {
            throw new IllegalArgumentException("Not enough seats available");
        }
        this.availableSeats -= quantity;
    }

    public void releaseSeats(int quantity) {
        this.availableSeats = Math.min(capacity, availableSeats + quantity);
    }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
}