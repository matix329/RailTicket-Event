package com.railgraph.service;

import com.railgraph.dto.output.RouteAnalyticsOutputDTO;
import com.railgraph.dto.output.TopRouteDTO;
import com.railgraph.model.Route;
import com.railgraph.model.RouteAnalytics;
import com.railgraph.repository.RouteAnalyticsRepository;
import com.railgraph.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private RouteAnalyticsRepository analyticsRepository;

    @Autowired
    private RouteRepository routeRepository;

    public List<RouteAnalyticsOutputDTO> getRouteAnalytics(Long routeId, Integer lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours != null ? lastHours : 24);
        List<RouteAnalytics> analytics = analyticsRepository.findByRouteIdAndWindowStartAfter(routeId, since);
        return analytics.stream()
                .map(this::mapToOutputDTO)
                .collect(Collectors.toList());
    }

    public List<RouteAnalyticsOutputDTO> getAllAnalytics(Integer lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours != null ? lastHours : 24);
        List<RouteAnalytics> analytics = analyticsRepository.findByWindowStartAfter(since);
        return analytics.stream()
                .map(this::mapToOutputDTO)
                .sorted(Comparator.comparing(RouteAnalyticsOutputDTO::getWindowStart).reversed())
                .collect(Collectors.toList());
    }

    public List<TopRouteDTO> getTopRoutesBySales(Integer topN, Integer lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours != null ? lastHours : 24);
        List<RouteAnalytics> analytics = analyticsRepository.findTopRoutesBySales(since);

        Map<Long, Integer> salesByRoute = new HashMap<>();
        Map<Long, BigDecimal> revenueByRoute = new HashMap<>();

        for (RouteAnalytics ra : analytics) {
            salesByRoute.merge(ra.getRouteId(), ra.getTicketsSold(), Integer::sum);
            revenueByRoute.merge(ra.getRouteId(), ra.getTotalRevenue(),
                    (a, b) -> a.add(b != null ? b : BigDecimal.ZERO));
        }

        List<TopRouteDTO> topRoutes = salesByRoute.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(topN != null ? topN : 5)
                .map(entry -> {
                    Long routeId = entry.getKey();
                    Route route = routeRepository.findById(routeId).orElse(null);
                    String routeName = route != null ?
                            route.getStationFrom().getName() + " → " + route.getStationTo().getName() :
                            "Route " + routeId;
                    return new TopRouteDTO(
                            routeId,
                            routeName,
                            entry.getValue(),
                            revenueByRoute.getOrDefault(routeId, BigDecimal.ZERO),
                            0
                    );
                })
                .collect(Collectors.toList());

        for (int i = 0; i < topRoutes.size(); i++) {
            topRoutes.get(i).setRank(i + 1);
        }

        return topRoutes;
    }

    public List<TopRouteDTO> getTopRoutesByRevenue(Integer topN, Integer lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours != null ? lastHours : 24);
        List<RouteAnalytics> analytics = analyticsRepository.findTopRoutesByRevenue(since);

        Map<Long, Integer> salesByRoute = new HashMap<>();
        Map<Long, BigDecimal> revenueByRoute = new HashMap<>();

        for (RouteAnalytics ra : analytics) {
            salesByRoute.merge(ra.getRouteId(), ra.getTicketsSold(), Integer::sum);
            revenueByRoute.merge(ra.getRouteId(), ra.getTotalRevenue(),
                    (a, b) -> a.add(b != null ? b : BigDecimal.ZERO));
        }

        List<TopRouteDTO> topRoutes = revenueByRoute.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(topN != null ? topN : 5)
                .map(entry -> {
                    Long routeId = entry.getKey();
                    Route route = routeRepository.findById(routeId).orElse(null);
                    String routeName = route != null ?
                            route.getStationFrom().getName() + " → " + route.getStationTo().getName() :
                            "Route " + routeId;
                    return new TopRouteDTO(
                            routeId,
                            routeName,
                            salesByRoute.getOrDefault(routeId, 0),
                            entry.getValue(),
                            0
                    );
                })
                .collect(Collectors.toList());

        for (int i = 0; i < topRoutes.size(); i++) {
            topRoutes.get(i).setRank(i + 1);
        }

        return topRoutes;
    }

    public Map<String, Object> getRouteSummary(Long routeId, Integer lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours != null ? lastHours : 24);

        Integer totalTickets = analyticsRepository.getTotalTicketsSoldForRoute(routeId, since);
        Double totalRevenue = analyticsRepository.getTotalRevenueForRoute(routeId, since);

        Map<String, Object> summary = new HashMap<>();
        summary.put("routeId", routeId);
        summary.put("periodHours", lastHours != null ? lastHours : 24);
        summary.put("totalTicketsSold", totalTickets != null ? totalTickets : 0);
        summary.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        summary.put("since", since);

        Route route = routeRepository.findById(routeId).orElse(null);
        if (route != null) {
            summary.put("routeName", route.getStationFrom().getName() + " → " + route.getStationTo().getName());
        }

        return summary;
    }

    private RouteAnalyticsOutputDTO mapToOutputDTO(RouteAnalytics analytics) {
        return new RouteAnalyticsOutputDTO(
                analytics.getId(),
                analytics.getRouteId(),
                analytics.getWindowStart(),
                analytics.getWindowEnd(),
                analytics.getTicketsSold(),
                analytics.getTotalRevenue(),
                analytics.getAveragePrice(),
                analytics.getCreatedAt()
        );
    }
}