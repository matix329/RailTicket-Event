package com.railgraph.repository;

import com.railgraph.model.RouteAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteAnalyticsRepository extends JpaRepository<RouteAnalytics, Long> {

    List<RouteAnalytics> findByRouteId(Long routeId);

    List<RouteAnalytics> findByRouteIdAndWindowStartAfter(Long routeId, LocalDateTime after);

    List<RouteAnalytics> findByWindowStartAfter(LocalDateTime after);

    @Query("SELECT ra FROM RouteAnalytics ra " +
           "WHERE ra.windowStart > :since " +
           "ORDER BY ra.ticketsSold DESC")
    List<RouteAnalytics> findTopRoutesBySales(@Param("since") LocalDateTime since);

    @Query("SELECT ra FROM RouteAnalytics ra " +
           "WHERE ra.windowStart > :since " +
           "ORDER BY ra.totalRevenue DESC")
    List<RouteAnalytics> findTopRoutesByRevenue(@Param("since") LocalDateTime since);

    @Query("SELECT SUM(ra.ticketsSold) FROM RouteAnalytics ra " +
           "WHERE ra.routeId = :routeId AND ra.windowStart > :since")
    Integer getTotalTicketsSoldForRoute(@Param("routeId") Long routeId, @Param("since") LocalDateTime since);

    @Query("SELECT SUM(ra.totalRevenue) FROM RouteAnalytics ra " +
           "WHERE ra.routeId = :routeId AND ra.windowStart > :since")
    Double getTotalRevenueForRoute(@Param("routeId") Long routeId, @Param("since") LocalDateTime since);
}