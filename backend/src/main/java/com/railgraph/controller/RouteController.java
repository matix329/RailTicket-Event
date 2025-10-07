package com.railgraph.controller;

import com.railgraph.dto.input.RouteInputDTO;
import com.railgraph.dto.input.StationInputDTO;
import com.railgraph.dto.output.RouteOutputDTO;
import com.railgraph.dto.output.StationOutputDTO;
import com.railgraph.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
    
    @Autowired
    private RouteService routeService;
    
    @PostMapping("/stations")
    public StationOutputDTO createStation(@RequestBody StationInputDTO stationInput) {
        return routeService.createStation(stationInput);
    }

    @GetMapping("/stations")
    public List<StationOutputDTO> getAllStations() {
        return routeService.getAllStations();
    }

    @PostMapping
    public RouteOutputDTO createRoute(@RequestBody RouteInputDTO routeInput) {
        return routeService.createRoute(routeInput);
    }

    @GetMapping
    public List<RouteOutputDTO> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @GetMapping("/path")
    public List<StationOutputDTO> findPath(@RequestParam("fromId") Long fromId, @RequestParam("toId") Long toId) {
        try {
            logger.info("Received path request: fromId={}, toId={}", fromId, toId);
            List<StationOutputDTO> result = routeService.findShortestPath(fromId, toId);
            logger.info("Path request completed successfully, result size: {}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in findPath endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/path/details")
    public List<RouteOutputDTO> findPathWithDetails(@RequestParam("fromId") Long fromId, @RequestParam("toId") Long toId) {
        try {
            logger.info("Received path with details request: fromId={}, toId={}", fromId, toId);
            List<RouteOutputDTO> result = routeService.findShortestPathWithDetails(fromId, toId);
            logger.info("Path with details request completed successfully, result size: {}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in findPathWithDetails endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/path/fastest")
    public List<RouteOutputDTO> findFastestPath(@RequestParam("fromId") Long fromId, @RequestParam("toId") Long toId) {
        try {
            logger.info("Received fastest path request: fromId={}, toId={}", fromId, toId);
            List<RouteOutputDTO> result = routeService.findFastestPathWithDetails(fromId, toId);
            logger.info("Fastest path request completed successfully, result size: {}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in findFastestPath endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/path/cheapest")
    public List<RouteOutputDTO> findCheapestPath(@RequestParam("fromId") Long fromId, @RequestParam("toId") Long toId) {
        try {
            logger.info("Received cheapest path request: fromId={}, toId={}", fromId, toId);
            List<RouteOutputDTO> result = routeService.findCheapestPathWithDetails(fromId, toId);
            logger.info("Cheapest path request completed successfully, result size: {}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in findCheapestPath endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/path/best-value")
    public List<RouteOutputDTO> findBestValuePath(@RequestParam("fromId") Long fromId, @RequestParam("toId") Long toId) {
        try {
            logger.info("Received best value path request: fromId={}, toId={}", fromId, toId);
            List<RouteOutputDTO> result = routeService.findBestValuePathWithDetails(fromId, toId);
            logger.info("Best value path request completed successfully, result size: {}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in findBestValuePath endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/{id}/seats/availability")
    public int getAvailableSeats(@PathVariable Long id) {
        try {
            logger.info("Received availability request for route ID: {}", id);
            int result = routeService.getAvailableSeats(id);
            logger.info("Availability request completed successfully, available seats: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error in getAvailableSeats endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/{id}/seats/capacity")
    public int getTotalCapacity(@PathVariable Long id) {
        try {
            logger.info("Received capacity request for route ID: {}", id);
            int result = routeService.getTotalCapacity(id);
            logger.info("Capacity request completed successfully, total capacity: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error in getTotalCapacity endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @PostMapping("/{id}/seats/reserve")
    public void reserveSeats(@PathVariable Long id, @RequestParam int quantity) {
        try {
            logger.info("Received seat reservation request for route ID: {}, quantity: {}", id, quantity);
            routeService.reserveSeats(id, quantity);
            logger.info("Seat reservation completed successfully");
        } catch (Exception e) {
            logger.error("Error in reserveSeats endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @PostMapping("/{id}/seats/release")
    public void releaseSeats(@PathVariable Long id, @RequestParam int quantity) {
        try {
            logger.info("Received seat release request for route ID: {}, quantity: {}", id, quantity);
            routeService.releaseSeats(id, quantity);
            logger.info("Seat release completed successfully");
        } catch (Exception e) {
            logger.error("Error in releaseSeats endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/search")
    public List<RouteOutputDTO> searchRoutes(@RequestParam("fromId") Long fromId, @RequestParam("toId") Long toId) {
        try {
            logger.info("Received search routes request: fromId={}, toId={}", fromId, toId);
            List<RouteOutputDTO> result = routeService.findRoutesBetweenStations(fromId, toId);
            logger.info("Search routes request completed successfully, found {} routes", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in searchRoutes endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/search/available")
    public List<RouteOutputDTO> searchAvailableRoutes(@RequestParam("fromId") Long fromId, @RequestParam("toId") Long toId) {
        try {
            logger.info("Received search available routes request: fromId={}, toId={}", fromId, toId);
            List<RouteOutputDTO> result = routeService.findAvailableRoutesBetweenStations(fromId, toId);
            logger.info("Search available routes request completed successfully, found {} available routes", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error in searchAvailableRoutes endpoint: {}", e.getMessage(), e);
            throw e;
        }
    }
}