package com.railgraph.controller;

import com.railgraph.dto.output.RouteAnalyticsOutputDTO;
import com.railgraph.dto.output.TopRouteDTO;
import com.railgraph.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/routes/{routeId}")
    public ResponseEntity<List<RouteAnalyticsOutputDTO>> getRouteAnalytics(
            @PathVariable Long routeId,
            @RequestParam(required = false, defaultValue = "24") Integer lastHours) {
        List<RouteAnalyticsOutputDTO> analytics = analyticsService.getRouteAnalytics(routeId, lastHours);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteAnalyticsOutputDTO>> getAllAnalytics(
            @RequestParam(required = false, defaultValue = "24") Integer lastHours) {
        List<RouteAnalyticsOutputDTO> analytics = analyticsService.getAllAnalytics(lastHours);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/top-routes/sales")
    public ResponseEntity<List<TopRouteDTO>> getTopRoutesBySales(
            @RequestParam(required = false, defaultValue = "5") Integer topN,
            @RequestParam(required = false, defaultValue = "24") Integer lastHours) {
        List<TopRouteDTO> topRoutes = analyticsService.getTopRoutesBySales(topN, lastHours);
        return ResponseEntity.ok(topRoutes);
    }

    @GetMapping("/top-routes/revenue")
    public ResponseEntity<List<TopRouteDTO>> getTopRoutesByRevenue(
            @RequestParam(required = false, defaultValue = "5") Integer topN,
            @RequestParam(required = false, defaultValue = "24") Integer lastHours) {
        List<TopRouteDTO> topRoutes = analyticsService.getTopRoutesByRevenue(topN, lastHours);
        return ResponseEntity.ok(topRoutes);
    }

    @GetMapping("/routes/{routeId}/summary")
    public ResponseEntity<Map<String, Object>> getRouteSummary(
            @PathVariable Long routeId,
            @RequestParam(required = false, defaultValue = "24") Integer lastHours) {
        Map<String, Object> summary = analyticsService.getRouteSummary(routeId, lastHours);
        return ResponseEntity.ok(summary);
    }
}