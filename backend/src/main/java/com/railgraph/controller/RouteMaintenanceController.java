package com.railgraph.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/maintenance")
public class RouteMaintenanceController {

    @Autowired
    private DataSource dataSource;

    @PostMapping("/reset-seats")
    public ResponseEntity<Map<String, Object>> resetSeats() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            int updated = stmt.executeUpdate("UPDATE routes SET available_seats = capacity");

            var rs = stmt.executeQuery(
                "SELECT COUNT(*) as routes, " +
                "SUM(capacity) as total_capacity, " +
                "SUM(available_seats) as total_available " +
                "FROM routes"
            );

            Map<String, Object> result = new HashMap<>();
            result.put("updated_routes", updated);

            if (rs.next()) {
                result.put("total_routes", rs.getInt("routes"));
                result.put("total_capacity", rs.getInt("total_capacity"));
                result.put("total_available", rs.getInt("total_available"));
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/increase-capacity")
    public ResponseEntity<Map<String, Object>> increaseCapacity(
            @RequestParam(defaultValue = "10") int multiplier) {

        if (multiplier < 1 || multiplier > 100) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Multiplier must be between 1 and 100");
            return ResponseEntity.badRequest().body(error);
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            int updated = stmt.executeUpdate(
                "UPDATE routes SET capacity = capacity * " + multiplier +
                ", available_seats = capacity * " + multiplier
            );

            var rs = stmt.executeQuery(
                "SELECT MIN(capacity) as min_cap, " +
                "MAX(capacity) as max_cap, " +
                "SUM(capacity) as total_cap " +
                "FROM routes"
            );

            Map<String, Object> result = new HashMap<>();
            result.put("updated_routes", updated);
            result.put("multiplier", multiplier);

            if (rs.next()) {
                result.put("min_capacity", rs.getInt("min_cap"));
                result.put("max_capacity", rs.getInt("max_cap"));
                result.put("total_capacity", rs.getInt("total_cap"));
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/capacity-stats")
    public ResponseEntity<Map<String, Object>> getCapacityStats() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            var rs = stmt.executeQuery(
                "SELECT " +
                "COUNT(*) as total_routes, " +
                "SUM(capacity) as total_capacity, " +
                "SUM(available_seats) as total_available, " +
                "SUM(capacity - available_seats) as total_sold, " +
                "ROUND(AVG(available_seats::numeric / capacity * 100), 2) as avg_availability_pct " +
                "FROM routes"
            );

            Map<String, Object> stats = new HashMap<>();

            if (rs.next()) {
                stats.put("total_routes", rs.getInt("total_routes"));
                stats.put("total_capacity", rs.getInt("total_capacity"));
                stats.put("total_available", rs.getInt("total_available"));
                stats.put("total_sold", rs.getInt("total_sold"));
                stats.put("average_availability_percent", rs.getDouble("avg_availability_pct"));
            }

            var lowCapRs = stmt.executeQuery(
                "SELECT COUNT(*) as low_capacity_routes " +
                "FROM routes " +
                "WHERE available_seats::numeric / capacity < 0.2"
            );

            if (lowCapRs.next()) {
                stats.put("routes_below_20_percent", lowCapRs.getInt("low_capacity_routes"));
            }

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
