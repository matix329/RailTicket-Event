package com.railgraph.repository;

import com.railgraph.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    
    @Modifying
    @Query(value = "ALTER SEQUENCE routes_id_seq RESTART WITH 1", nativeQuery = true)
    void resetSequence();
}