package com.railgraph.service;

import com.railgraph.dto.input.TicketInputDTO;
import com.railgraph.dto.output.RouteOutputDTO;
import com.railgraph.dto.output.StationOutputDTO;
import com.railgraph.dto.output.TicketOutputDTO;
import com.railgraph.event.TicketEvent;
import com.railgraph.event.TicketEventProducer;
import com.railgraph.model.*;
import com.railgraph.repository.RouteRepository;
import com.railgraph.repository.TicketRepository;
import com.railgraph.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TicketEventProducer ticketEventProducer;

    @Transactional
    public TicketOutputDTO buyTicket(TicketInputDTO ticketInput, String userId) {
        Optional<Route> route = routeRepository.findById(ticketInput.getRouteId());
        if (route.isEmpty()) {
            throw new IllegalArgumentException("Route with ID " + ticketInput.getRouteId() + " does not exist");
        }

        if (ticketInput.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Route routeEntity = route.get();
        if (!routeEntity.hasAvailableSeats(ticketInput.getQuantity())) {
            throw new IllegalArgumentException("Not enough seats available. Available: " + routeEntity.getAvailableSeats() + ", Requested: " + ticketInput.getQuantity());
        }

        BigDecimal routePrice = routeEntity.getPrice();
        BigDecimal totalBasePrice = routePrice.multiply(new BigDecimal(ticketInput.getQuantity()));
        BigDecimal finalPrice = calculateFinalPrice(totalBasePrice, ticketInput.getDiscountType());
        
        Ticket ticket = new Ticket(routeEntity, totalBasePrice, ticketInput.getDiscountType(), finalPrice, ticketInput.getQuantity());
        Ticket savedTicket = ticketRepository.save(ticket);
        
        routeEntity.reserveSeats(ticketInput.getQuantity());
        routeRepository.save(routeEntity);

        Transaction transaction = new Transaction(savedTicket, LocalDateTime.now(), userId);
        transactionRepository.save(transaction);

        ticketEventProducer.sendTicketEvent(
            new TicketEvent(
                routeEntity.getId(),
                ticketInput.getQuantity(),
                ticketInput.getDiscountType().name(),
                userId,
                LocalDateTime.now()
            )
        );

        return mapToTicketOutputDTO(savedTicket);
    }

    public List<TicketOutputDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToTicketOutputDTO)
                .collect(Collectors.toList());
    }
    
    public boolean validateTicket(Long ticketId) {
        Optional<Ticket> ticket = ticketRepository.findById(ticketId);
        return ticket.isPresent();
    }
    

    private BigDecimal calculateFinalPrice(BigDecimal basePrice, DiscountType discountType) {
        switch (discountType) {
            case STUDENT:
                return basePrice.multiply(new BigDecimal("0.5"));
            case SENIOR:
                return basePrice.multiply(new BigDecimal("0.7"));
            case GROUP:
                return basePrice.multiply(new BigDecimal("0.8"));
            case NONE:
            default:
                return basePrice;
        }
    }
    
    private TicketOutputDTO mapToTicketOutputDTO(Ticket ticket) {
        RouteOutputDTO routeDTO = mapToRouteOutputDTO(ticket.getRoute());
        return new TicketOutputDTO(ticket.getId(), routeDTO, ticket.getBasePrice(), ticket.getFinalPrice(), ticket.getDiscountType(), ticket.getQuantity());
    }

    private RouteOutputDTO mapToRouteOutputDTO(Route route) {
        StationOutputDTO stationFrom = new StationOutputDTO(route.getStationFrom().getId(), route.getStationFrom().getName(), route.getStationFrom().getCity());
        StationOutputDTO stationTo = new StationOutputDTO(route.getStationTo().getId(), route.getStationTo().getName(), route.getStationTo().getCity());
        return new RouteOutputDTO(route.getId(), stationFrom, stationTo, route.getTravelTimeMinutes(), route.getPrice());
    }
}