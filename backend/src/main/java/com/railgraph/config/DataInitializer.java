package com.railgraph.config;

import com.railgraph.model.Route;
import com.railgraph.model.Station;
import com.railgraph.repository.RouteRepository;
import com.railgraph.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
public class DataInitializer {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private RouteRepository routeRepository;

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void init() {
        try {
        routeRepository.deleteAll();
        stationRepository.deleteAll();
        
        stationRepository.resetSequence();
        routeRepository.resetSequence();

        Station koln = new Station("Köln Hauptbahnhof", "Köln");
        Station dusseldorf = new Station("Düsseldorf Hauptbahnhof", "Düsseldorf");
        Station dortmund = new Station("Dortmund Hauptbahnhof", "Dortmund");
        Station essen = new Station("Essen Hauptbahnhof", "Essen");
        Station bochum = new Station("Bochum Hauptbahnhof", "Bochum");
        Station wuppertal = new Station("Wuppertal Hauptbahnhof", "Wuppertal");
        Station bielefeld = new Station("Bielefeld Hauptbahnhof", "Bielefeld");
        Station munster = new Station("Münster Hauptbahnhof", "Münster");
        Station aachen = new Station("Aachen Hauptbahnhof", "Aachen");
        Station bonn = new Station("Bonn Hauptbahnhof", "Bonn");

        koln = stationRepository.save(koln);
        dusseldorf = stationRepository.save(dusseldorf);
        dortmund = stationRepository.save(dortmund);
        essen = stationRepository.save(essen);
        bochum = stationRepository.save(bochum);
        wuppertal = stationRepository.save(wuppertal);
        bielefeld = stationRepository.save(bielefeld);
        munster = stationRepository.save(munster);
        aachen = stationRepository.save(aachen);
        bonn = stationRepository.save(bonn);
        routeRepository.save(new Route(koln, dusseldorf, 25, new BigDecimal("12.50"), null, null, 100));
        routeRepository.save(new Route(dusseldorf, koln, 25, new BigDecimal("12.50"), null, null, 100));
        routeRepository.save(new Route(koln, dortmund, 45, new BigDecimal("18.00"), null, null, 80));
        routeRepository.save(new Route(dortmund, koln, 45, new BigDecimal("18.00"), null, null, 80));
        routeRepository.save(new Route(dusseldorf, essen, 20, new BigDecimal("10.00"), null, null, 120));
        routeRepository.save(new Route(essen, dusseldorf, 20, new BigDecimal("10.00"), null, null, 120));
        routeRepository.save(new Route(essen, bochum, 15, new BigDecimal("8.50"), null, null, 150));
        routeRepository.save(new Route(bochum, essen, 15, new BigDecimal("8.50"), null, null, 150));
        routeRepository.save(new Route(bochum, wuppertal, 25, new BigDecimal("12.00"), null, null, 90));
        routeRepository.save(new Route(wuppertal, bochum, 25, new BigDecimal("12.00"), null, null, 90));
        routeRepository.save(new Route(dortmund, bielefeld, 35, new BigDecimal("15.50"), null, null, 70));
        routeRepository.save(new Route(bielefeld, dortmund, 35, new BigDecimal("15.50"), null, null, 70));
        routeRepository.save(new Route(bielefeld, munster, 30, new BigDecimal("14.00"), null, null, 110));
        routeRepository.save(new Route(munster, bielefeld, 30, new BigDecimal("14.00"), null, null, 110));
        routeRepository.save(new Route(koln, aachen, 40, new BigDecimal("16.50"), null, null, 60));
        routeRepository.save(new Route(aachen, koln, 40, new BigDecimal("16.50"), null, null, 60));
        routeRepository.save(new Route(koln, bonn, 20, new BigDecimal("9.50"), null, null, 200));
        routeRepository.save(new Route(bonn, koln, 20, new BigDecimal("9.50"), null, null, 200));
        routeRepository.save(new Route(dusseldorf, aachen, 35, new BigDecimal("15.00"), null, null, 85));
        routeRepository.save(new Route(aachen, dusseldorf, 35, new BigDecimal("15.00"), null, null, 85));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}