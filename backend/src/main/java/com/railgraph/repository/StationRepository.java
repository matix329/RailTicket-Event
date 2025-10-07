package com.railgraph.repository;

import com.railgraph.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    
    @Modifying
    @Query(value = "ALTER SEQUENCE stations_id_seq RESTART WITH 1", nativeQuery = true)
    void resetSequence();
}