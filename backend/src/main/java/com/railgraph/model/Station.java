package com.railgraph.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "stations")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @OneToMany(mappedBy = "stationFrom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Route> routesFrom;

    @OneToMany(mappedBy = "stationTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Route> routesTo;

    public Station() {}

    public Station(String name, String city) {
        this.name = name;
        this.city = city;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public List<Route> getRoutesFrom() { return routesFrom; }
    public void setRoutesFrom(List<Route> routesFrom) { this.routesFrom = routesFrom; }

    public List<Route> getRoutesTo() { return routesTo; }
    public void setRoutesTo(List<Route> routesTo) { this.routesTo = routesTo; }
}