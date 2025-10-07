package com.railgraph.service;

import com.railgraph.dto.input.RouteInputDTO;
import com.railgraph.dto.input.StationInputDTO;
import com.railgraph.dto.output.RouteOutputDTO;
import com.railgraph.dto.output.StationOutputDTO;
import com.railgraph.model.Route;
import com.railgraph.model.Station;
import com.railgraph.repository.RouteRepository;
import com.railgraph.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RouteService {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired
    private RouteRepository routeRepository;
    
    public StationOutputDTO createStation(StationInputDTO stationInput) {
        Station station = new Station(stationInput.getName(), stationInput.getCity());
        Station savedStation = stationRepository.save(station);
        return mapToStationOutputDTO(savedStation);
    }

    public List<StationOutputDTO> getAllStations() {
        return stationRepository.findAll().stream()
                .map(this::mapToStationOutputDTO)
                .collect(Collectors.toList());
    }

    public RouteOutputDTO createRoute(RouteInputDTO routeInput) {
        Optional<Station> fromStation = stationRepository.findById(routeInput.getStationFromId());
        Optional<Station> toStation = stationRepository.findById(routeInput.getStationToId());

        if (fromStation.isEmpty()) {
            throw new IllegalArgumentException("Station with ID " + routeInput.getStationFromId() + " does not exist");
        }
        if (toStation.isEmpty()) {
            throw new IllegalArgumentException("Station with ID " + routeInput.getStationToId() + " does not exist");
        }

        Route route = new Route(
            fromStation.get(), 
            toStation.get(), 
            routeInput.getTravelTimeMinutes(), 
            routeInput.getPrice(),
            routeInput.getTrainCategory(),
            routeInput.getTrainNumber(),
            routeInput.getCapacity()
        );
        
        route.setAvailableSeats(routeInput.getCapacity());
        Route savedRoute = routeRepository.save(route);
        return mapToRouteOutputDTO(savedRoute);
    }

    public List<RouteOutputDTO> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::mapToRouteOutputDTO)
                .collect(Collectors.toList());
    }

    public List<StationOutputDTO> findShortestPath(Long fromStationId, Long toStationId) {
        return findShortestPathWithWeight(fromStationId, toStationId, route -> (long) route.getTravelTimeMinutes());
    }
    
    public List<RouteOutputDTO> findShortestPathWithDetails(Long fromStationId, Long toStationId) {
        try {
            logger.info("Finding shortest path with details from station {} to station {}", fromStationId, toStationId);
            
            if (fromStationId == null) {
                logger.error("fromStationId is null");
                throw new IllegalArgumentException("fromStationId cannot be null");
            }
            if (toStationId == null) {
                logger.error("toStationId is null");
                throw new IllegalArgumentException("toStationId cannot be null");
            }
            
            Optional<Station> fromStation = stationRepository.findById(fromStationId);
            Optional<Station> toStation = stationRepository.findById(toStationId);
            
            if (fromStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", fromStationId);
                throw new IllegalArgumentException("Station with ID " + fromStationId + " does not exist");
            }
            
            if (toStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", toStationId);
                throw new IllegalArgumentException("Station with ID " + toStationId + " does not exist");
            }
            
            if (fromStationId.equals(toStationId)) {
                logger.info("Same station requested, returning empty list");
                return List.of();
            }
            
            logger.info("Loading all routes from database");
            List<Route> allRoutes = routeRepository.findAll();
            logger.info("Loaded {} routes", allRoutes.size());
            
            Map<Long, List<Route>> graph = buildGraph(allRoutes);
            logger.info("Built graph with {} nodes", graph.size());
            
            logger.info("Running Dijkstra's algorithm for route details");
            List<Long> path = findShortestPathDijkstraWithWeight(graph, fromStationId, toStationId, route -> (long) route.getTravelTimeMinutes());
            logger.info("Dijkstra's algorithm completed, path length: {}", path.size());
            
            if (path.isEmpty()) {
                logger.info("No path found between stations {} and {}", fromStationId, toStationId);
                return List.of();
            }
            
            logger.info("Converting path to RouteOutputDTOs");
            List<RouteOutputDTO> routeDetails = new ArrayList<>();
            
            for (int i = 0; i < path.size() - 1; i++) {
                Long currentStationId = path.get(i);
                Long nextStationId = path.get(i + 1);
                
                Route route = findRouteBetweenStations(allRoutes, currentStationId, nextStationId);
                if (route != null) {
                    routeDetails.add(mapToRouteOutputDTO(route));
                }
            }
            
            logger.info("Path with details completed, found {} route segments", routeDetails.size());
            return routeDetails;
                    
        } catch (Exception e) {
            logger.error("Error in findShortestPathWithDetails: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private List<RouteOutputDTO> findShortestPathWithDetailsByWeight(Long fromStationId, Long toStationId, Function<Route, Long> weightFunction) {
        try {
            logger.info("Finding shortest path with details from station {} to station {} with custom weight", fromStationId, toStationId);
            
            if (fromStationId == null) {
                logger.error("fromStationId is null");
                throw new IllegalArgumentException("fromStationId cannot be null");
            }
            if (toStationId == null) {
                logger.error("toStationId is null");
                throw new IllegalArgumentException("toStationId cannot be null");
            }
            
            Optional<Station> fromStation = stationRepository.findById(fromStationId);
            Optional<Station> toStation = stationRepository.findById(toStationId);
            
            if (fromStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", fromStationId);
                throw new IllegalArgumentException("Station with ID " + fromStationId + " does not exist");
            }
            
            if (toStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", toStationId);
                throw new IllegalArgumentException("Station with ID " + toStationId + " does not exist");
            }
            
            if (fromStationId.equals(toStationId)) {
                logger.info("Same station requested, returning empty list");
                return List.of();
            }
            
            logger.info("Loading all routes from database");
            List<Route> allRoutes = routeRepository.findAll();
            logger.info("Loaded {} routes", allRoutes.size());
            
            Map<Long, List<Route>> graph = buildGraph(allRoutes);
            logger.info("Built graph with {} nodes", graph.size());
            
            logger.info("Running Dijkstra's algorithm for route details with custom weight");
            List<Long> path = findShortestPathDijkstraWithWeight(graph, fromStationId, toStationId, weightFunction);
            logger.info("Dijkstra's algorithm completed, path length: {}", path.size());
            
            if (path.isEmpty()) {
                logger.info("No path found between stations {} and {}", fromStationId, toStationId);
                return List.of();
            }
            
            logger.info("Converting path to RouteOutputDTOs");
            List<RouteOutputDTO> routeDetails = new ArrayList<>();
            
            for (int i = 0; i < path.size() - 1; i++) {
                Long currentStationId = path.get(i);
                Long nextStationId = path.get(i + 1);
                
                Route route = findRouteBetweenStations(allRoutes, currentStationId, nextStationId);
                if (route != null) {
                    routeDetails.add(mapToRouteOutputDTO(route));
                }
            }
            
            logger.info("Path with details completed, found {} route segments", routeDetails.size());
            return routeDetails;
                    
        } catch (Exception e) {
            logger.error("Error in findShortestPathWithDetailsByWeight: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private Route findRouteBetweenStations(List<Route> routes, Long fromStationId, Long toStationId) {
        return routes.stream()
                .filter(route -> route.getStationFrom().getId().equals(fromStationId) && 
                               route.getStationTo().getId().equals(toStationId))
                .min((r1, r2) -> Integer.compare(r1.getTravelTimeMinutes(), r2.getTravelTimeMinutes()))
                .orElse(null);
    }
    
    private Route findCheapestRouteBetweenStations(List<Route> routes, Long fromStationId, Long toStationId) {
        return routes.stream()
                .filter(route -> route.getStationFrom().getId().equals(fromStationId) && 
                               route.getStationTo().getId().equals(toStationId))
                .min((r1, r2) -> r1.getPrice().compareTo(r2.getPrice()))
                .orElse(null);
    }
    
    private Route findBestValueRouteBetweenStations(List<Route> routes, Long fromStationId, Long toStationId) {
        return routes.stream()
                .filter(route -> route.getStationFrom().getId().equals(fromStationId) && 
                               route.getStationTo().getId().equals(toStationId))
                .min((r1, r2) -> {
                    BigDecimal value1 = r1.getPrice().divide(BigDecimal.valueOf(r1.getTravelTimeMinutes()), 2, RoundingMode.HALF_UP);
                    BigDecimal value2 = r2.getPrice().divide(BigDecimal.valueOf(r2.getTravelTimeMinutes()), 2, RoundingMode.HALF_UP);
                    return value1.compareTo(value2);
                })
                .orElse(null);
    }
    
    public List<StationOutputDTO> findShortestPathByTime(Long fromStationId, Long toStationId) {
        return findShortestPathWithWeight(fromStationId, toStationId, route -> (long) route.getTravelTimeMinutes());
    }
    
    public List<StationOutputDTO> findCheapestPath(Long fromStationId, Long toStationId) {
        return findShortestPathWithWeight(fromStationId, toStationId, route -> route.getPrice().longValue());
    }
    
    public List<StationOutputDTO> findBestValuePath(Long fromStationId, Long toStationId) {
        return findShortestPathWithWeight(fromStationId, toStationId, route -> 
            route.getPrice().divide(BigDecimal.valueOf(route.getTravelTimeMinutes()), 2, RoundingMode.HALF_UP).longValue()
        );
    }
    
    public List<RouteOutputDTO> findFastestPathWithDetails(Long fromStationId, Long toStationId) {
        return findShortestPathWithDetailsByWeight(fromStationId, toStationId, route -> (long) route.getTravelTimeMinutes());
    }
    
    public List<RouteOutputDTO> findCheapestPathWithDetails(Long fromStationId, Long toStationId) {
        try {
            logger.info("Finding cheapest path with details from station {} to station {}", fromStationId, toStationId);
            
            if (fromStationId == null) {
                logger.error("fromStationId is null");
                throw new IllegalArgumentException("fromStationId cannot be null");
            }
            if (toStationId == null) {
                logger.error("toStationId is null");
                throw new IllegalArgumentException("toStationId cannot be null");
            }
            
            Optional<Station> fromStation = stationRepository.findById(fromStationId);
            Optional<Station> toStation = stationRepository.findById(toStationId);
            
            if (fromStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", fromStationId);
                throw new IllegalArgumentException("Station with ID " + fromStationId + " does not exist");
            }
            
            if (toStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", toStationId);
                throw new IllegalArgumentException("Station with ID " + toStationId + " does not exist");
            }
            
            if (fromStationId.equals(toStationId)) {
                logger.info("Same station requested, returning empty list");
                return List.of();
            }
            
            logger.info("Loading all routes from database");
            List<Route> allRoutes = routeRepository.findAll();
            logger.info("Loaded {} routes", allRoutes.size());
            
            Map<Long, List<Route>> graph = buildGraph(allRoutes);
            logger.info("Built graph with {} nodes", graph.size());
            
            logger.info("Running Dijkstra's algorithm for cheapest path");
            List<Long> path = findShortestPathDijkstraWithWeight(graph, fromStationId, toStationId, route -> route.getPrice().longValue());
            logger.info("Dijkstra's algorithm completed, path length: {}", path.size());
            
            if (path.isEmpty()) {
                logger.info("No path found between stations {} and {}", fromStationId, toStationId);
                return List.of();
            }
            
            logger.info("Converting path to RouteOutputDTOs");
            List<RouteOutputDTO> routeDetails = new ArrayList<>();
            
            for (int i = 0; i < path.size() - 1; i++) {
                Long currentStationId = path.get(i);
                Long nextStationId = path.get(i + 1);
                
                Route route = findCheapestRouteBetweenStations(allRoutes, currentStationId, nextStationId);
                if (route != null) {
                    routeDetails.add(mapToRouteOutputDTO(route));
                }
            }
            
            logger.info("Cheapest path with details completed, found {} route segments", routeDetails.size());
            return routeDetails;
                    
        } catch (Exception e) {
            logger.error("Error in findCheapestPathWithDetails: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public List<RouteOutputDTO> findBestValuePathWithDetails(Long fromStationId, Long toStationId) {
        try {
            logger.info("Finding best value path with details from station {} to station {}", fromStationId, toStationId);
            
            if (fromStationId == null) {
                logger.error("fromStationId is null");
                throw new IllegalArgumentException("fromStationId cannot be null");
            }
            if (toStationId == null) {
                logger.error("toStationId is null");
                throw new IllegalArgumentException("toStationId cannot be null");
            }
            
            Optional<Station> fromStation = stationRepository.findById(fromStationId);
            Optional<Station> toStation = stationRepository.findById(toStationId);
            
            if (fromStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", fromStationId);
                throw new IllegalArgumentException("Station with ID " + fromStationId + " does not exist");
            }
            
            if (toStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", toStationId);
                throw new IllegalArgumentException("Station with ID " + toStationId + " does not exist");
            }
            
            if (fromStationId.equals(toStationId)) {
                logger.info("Same station requested, returning empty list");
                return List.of();
            }
            
            logger.info("Loading all routes from database");
            List<Route> allRoutes = routeRepository.findAll();
            logger.info("Loaded {} routes", allRoutes.size());
            
            Map<Long, List<Route>> graph = buildGraph(allRoutes);
            logger.info("Built graph with {} nodes", graph.size());
            
            logger.info("Running Dijkstra's algorithm for best value path");
            List<Long> path = findShortestPathDijkstraWithWeight(graph, fromStationId, toStationId, route -> 
                route.getPrice().divide(BigDecimal.valueOf(route.getTravelTimeMinutes()), 2, RoundingMode.HALF_UP).longValue()
            );
            logger.info("Dijkstra's algorithm completed, path length: {}", path.size());
            
            if (path.isEmpty()) {
                logger.info("No path found between stations {} and {}", fromStationId, toStationId);
                return List.of();
            }
            
            logger.info("Converting path to RouteOutputDTOs");
            List<RouteOutputDTO> routeDetails = new ArrayList<>();
            
            for (int i = 0; i < path.size() - 1; i++) {
                Long currentStationId = path.get(i);
                Long nextStationId = path.get(i + 1);
                
                Route route = findBestValueRouteBetweenStations(allRoutes, currentStationId, nextStationId);
                if (route != null) {
                    routeDetails.add(mapToRouteOutputDTO(route));
                }
            }
            
            logger.info("Best value path with details completed, found {} route segments", routeDetails.size());
            return routeDetails;
                    
        } catch (Exception e) {
            logger.error("Error in findBestValuePathWithDetails: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public int getAvailableSeats(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route with ID " + routeId + " not found"));
        return route.getAvailableSeats();
    }
    
    public int getTotalCapacity(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route with ID " + routeId + " not found"));
        return route.getCapacity();
    }
    
    public void reserveSeats(Long routeId, int quantity) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route with ID " + routeId + " not found"));
        
        if (!route.hasAvailableSeats(quantity)) {
            throw new IllegalArgumentException("Not enough seats available. Available: " + route.getAvailableSeats() + ", Requested: " + quantity);
        }
        
        route.reserveSeats(quantity);
        routeRepository.save(route);
    }
    
    public void releaseSeats(Long routeId, int quantity) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route with ID " + routeId + " not found"));
        
        route.releaseSeats(quantity);
        routeRepository.save(route);
    }
    
    public List<RouteOutputDTO> findRoutesBetweenStations(Long fromStationId, Long toStationId) {
        try {
            logger.info("Finding all routes between stations {} and {}", fromStationId, toStationId);
            
            if (fromStationId == null) {
                logger.error("fromStationId is null");
                throw new IllegalArgumentException("fromStationId cannot be null");
            }
            if (toStationId == null) {
                logger.error("toStationId is null");
                throw new IllegalArgumentException("toStationId cannot be null");
            }
            
            Optional<Station> fromStation = stationRepository.findById(fromStationId);
            Optional<Station> toStation = stationRepository.findById(toStationId);
            
            if (fromStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", fromStationId);
                throw new IllegalArgumentException("Station with ID " + fromStationId + " does not exist");
            }
            if (toStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", toStationId);
                throw new IllegalArgumentException("Station with ID " + toStationId + " does not exist");
            }
            
            List<Route> allRoutes = routeRepository.findAll();
            List<Route> routesBetweenStations = allRoutes.stream()
                    .filter(route -> route.getStationFrom().getId().equals(fromStationId) && 
                                   route.getStationTo().getId().equals(toStationId))
                    .sorted((r1, r2) -> Integer.compare(r1.getTravelTimeMinutes(), r2.getTravelTimeMinutes()))
                    .collect(Collectors.toList());
            
            logger.info("Found {} routes between stations {} and {}", routesBetweenStations.size(), fromStationId, toStationId);
            
            return routesBetweenStations.stream()
                    .map(this::mapToRouteOutputDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error in findRoutesBetweenStations: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public List<RouteOutputDTO> findAvailableRoutesBetweenStations(Long fromStationId, Long toStationId) {
        try {
            logger.info("Finding available routes between stations {} and {}", fromStationId, toStationId);
            
            if (fromStationId == null) {
                logger.error("fromStationId is null");
                throw new IllegalArgumentException("fromStationId cannot be null");
            }
            if (toStationId == null) {
                logger.error("toStationId is null");
                throw new IllegalArgumentException("toStationId cannot be null");
            }
            
            Optional<Station> fromStation = stationRepository.findById(fromStationId);
            Optional<Station> toStation = stationRepository.findById(toStationId);
            
            if (fromStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", fromStationId);
                throw new IllegalArgumentException("Station with ID " + fromStationId + " does not exist");
            }
            if (toStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", toStationId);
                throw new IllegalArgumentException("Station with ID " + toStationId + " does not exist");
            }
            
            List<Route> allRoutes = routeRepository.findAll();
            List<Route> availableRoutes = allRoutes.stream()
                    .filter(route -> route.getStationFrom().getId().equals(fromStationId) && 
                                   route.getStationTo().getId().equals(toStationId) &&
                                   route.getAvailableSeats() > 0)
                    .sorted((r1, r2) -> Integer.compare(r1.getTravelTimeMinutes(), r2.getTravelTimeMinutes()))
                    .collect(Collectors.toList());
            
            logger.info("Found {} available routes between stations {} and {}", availableRoutes.size(), fromStationId, toStationId);
            
            return availableRoutes.stream()
                    .map(this::mapToRouteOutputDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error in findAvailableRoutesBetweenStations: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private List<StationOutputDTO> findShortestPathWithWeight(Long fromStationId, Long toStationId, Function<Route, Long> weightFunction) {
        try {
            logger.info("Finding shortest path from station {} to station {} with custom weight", fromStationId, toStationId);
            
            if (fromStationId == null) {
                logger.error("fromStationId is null");
                throw new IllegalArgumentException("fromStationId cannot be null");
            }
            if (toStationId == null) {
                logger.error("toStationId is null");
                throw new IllegalArgumentException("toStationId cannot be null");
            }
            
        Optional<Station> fromStation = stationRepository.findById(fromStationId);
        Optional<Station> toStation = stationRepository.findById(toStationId);
        
        if (fromStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", fromStationId);
            throw new IllegalArgumentException("Station with ID " + fromStationId + " does not exist");
        }
        
        if (toStation.isEmpty()) {
                logger.error("Station with ID {} does not exist", toStationId);
            throw new IllegalArgumentException("Station with ID " + toStationId + " does not exist");
            }
            
            if (fromStationId.equals(toStationId)) {
                logger.info("Same station requested, returning single station");
                return List.of(mapToStationOutputDTO(fromStation.get()));
            }
            
            logger.info("Loading all routes from database");
            List<Route> allRoutes = routeRepository.findAll();
            logger.info("Loaded {} routes", allRoutes.size());
            
            Map<Long, List<Route>> graph = buildGraph(allRoutes);
            logger.info("Built graph with {} nodes", graph.size());
            
            logger.info("Running Dijkstra's algorithm with custom weight");
            List<Long> path = findShortestPathDijkstraWithWeight(graph, fromStationId, toStationId, weightFunction);
            logger.info("Dijkstra's algorithm completed, path length: {}", path.size());
            
            if (path.isEmpty()) {
                logger.info("No path found between stations {} and {}", fromStationId, toStationId);
                return List.of();
            }
            
            logger.info("Converting path to StationOutputDTOs");
            return path.stream()
                    .map(stationId -> {
                        try {
                            Station station = stationRepository.findById(stationId)
                                    .orElseThrow(() -> new IllegalStateException("Station not found: " + stationId));
                            return mapToStationOutputDTO(station);
                        } catch (Exception e) {
                            logger.error("Error converting station ID {} to DTO: {}", stationId, e.getMessage());
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error in findShortestPathWithWeight: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private Map<Long, List<Route>> buildGraph(List<Route> routes) {
        try {
            logger.info("Building graph from {} routes", routes.size());
            Map<Long, List<Route>> graph = new HashMap<>();
            
            for (Route route : routes) {
                try {
                    if (route == null) {
                        logger.warn("Found null route, skipping");
                        continue;
                    }
                    
                    if (route.getStationFrom() == null) {
                        logger.warn("Found route with null stationFrom, skipping route ID: {}", route.getId());
                        continue;
                    }
                    
                    if (route.getStationTo() == null) {
                        logger.warn("Found route with null stationTo, skipping route ID: {}", route.getId());
                        continue;
                    }
                    
                    Long fromId = route.getStationFrom().getId();
                    if (fromId == null) {
                        logger.warn("Found route with null stationFrom ID, skipping route ID: {}", route.getId());
                        continue;
                    }
                    
                    graph.computeIfAbsent(fromId, k -> new ArrayList<>()).add(route);
                    logger.debug("Added route ID {} ({} {}) from station {} to station {} with time {} minutes", 
                        route.getId(), route.getTrainCategory(), route.getTrainNumber(), 
                        fromId, route.getStationTo().getId(), route.getTravelTimeMinutes());
                } catch (Exception e) {
                    logger.error("Error processing route ID {}: {}", route != null ? route.getId() : "null", e.getMessage());
                }
            }
            
            logger.info("Graph built successfully with {} nodes", graph.size());
            return graph;
        } catch (Exception e) {
            logger.error("Error building graph: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private List<Long> findShortestPathDijkstraWithWeight(Map<Long, List<Route>> graph, Long startId, Long endId, Function<Route, Long> weightFunction) {
        try {
            logger.info("Starting Dijkstra's algorithm with custom weight from {} to {}", startId, endId);
            
            if (startId == null) {
                logger.error("startId is null");
                throw new IllegalArgumentException("startId cannot be null");
            }
            if (endId == null) {
                logger.error("endId is null");
                throw new IllegalArgumentException("endId cannot be null");
            }
            if (graph == null) {
                logger.error("graph is null");
                throw new IllegalArgumentException("graph cannot be null");
            }
            
            PriorityQueue<PathNode> pq = new PriorityQueue<>(Comparator.comparingLong(PathNode::getTime));
            
            Map<Long, Long> distances = new HashMap<>();
            
            Map<Long, Long> previous = new HashMap<>();
            distances.put(startId, 0L);
            pq.offer(new PathNode(0L, startId));
            logger.info("Initialized Dijkstra with start station {}", startId);
            
            int iterations = 0;
            int maxIterations = 1000;
            
            while (!pq.isEmpty() && iterations < maxIterations) {
                iterations++;
                
                PathNode current = pq.poll();
                if (current == null) {
                    logger.warn("Polled null PathNode from priority queue");
                    continue;
                }
                
                long currentTime = current.getTime();
                Long currentStationId = current.getStationId();
                
                if (currentStationId == null) {
                    logger.warn("PathNode has null stationId, skipping");
                    continue;
                }
                
                if (currentTime > distances.getOrDefault(currentStationId, Long.MAX_VALUE)) {
                    continue;
                }
                
                if (currentStationId.equals(endId)) {
                    logger.info("Reached destination station {} after {} iterations", endId, iterations);
                    break;
                }
                
                List<Route> neighbors = graph.getOrDefault(currentStationId, Collections.emptyList());
                logger.debug("Exploring {} neighbors from station {}", neighbors.size(), currentStationId);
                
                for (Route route : neighbors) {
                    try {
                        if (route == null) {
                            logger.warn("Found null route in neighbors, skipping");
                            continue;
                        }
                        
                        if (route.getStationTo() == null) {
                            logger.warn("Found route with null stationTo in neighbors, skipping");
                            continue;
                        }
                        
                        Long neighborId = route.getStationTo().getId();
                        if (neighborId == null) {
                            logger.warn("Found route with null stationTo ID in neighbors, skipping");
                            continue;
                        }
                        
                        long newTime = currentTime + weightFunction.apply(route);
                        
                        if (newTime < distances.getOrDefault(neighborId, Long.MAX_VALUE)) {
                            distances.put(neighborId, newTime);
                            previous.put(neighborId, currentStationId);
                            pq.offer(new PathNode(newTime, neighborId));
                            logger.debug("Found shorter path to station {} with weight {}", neighborId, newTime);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing neighbor route: {}", e.getMessage());
                    }
                }
            }
            
            if (iterations >= maxIterations) {
                logger.warn("Dijkstra's algorithm reached maximum iterations limit ({})", maxIterations);
            }
            
            logger.info("Reconstructing path from previous map");
            List<Long> path = new ArrayList<>();
            if (!previous.containsKey(endId) && !startId.equals(endId)) {
                logger.info("No path found - endId not in previous map");
                return path;
            }
            
            Long current = endId;
            while (current != null) {
                path.add(current);
                current = previous.get(current);
            }
            
            Collections.reverse(path);
            logger.info("Path reconstructed with {} stations: {}", path.size(), path);
            return path;
            
        } catch (Exception e) {
            logger.error("Error in findShortestPathDijkstraWithWeight: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private static class PathNode {
        private final long time;
        private final Long stationId;
        
        public PathNode(long time, Long stationId) {
            this.time = time;
            this.stationId = stationId;
        }
        
        public long getTime() {
            return time;
        }
        
        public Long getStationId() {
            return stationId;
        }
    }
    
    private StationOutputDTO mapToStationOutputDTO(Station station) {
        return new StationOutputDTO(station.getId(), station.getName(), station.getCity());
    }

    private RouteOutputDTO mapToRouteOutputDTO(Route route) {
        StationOutputDTO stationFrom = mapToStationOutputDTO(route.getStationFrom());
        StationOutputDTO stationTo = mapToStationOutputDTO(route.getStationTo());
        return new RouteOutputDTO(
            route.getId(), 
            stationFrom, 
            stationTo, 
            route.getTravelTimeMinutes(), 
            route.getPrice(),
            route.getTrainCategory(),
            route.getTrainNumber(),
            route.getCapacity(),
            route.getAvailableSeats()
        );
    }
}