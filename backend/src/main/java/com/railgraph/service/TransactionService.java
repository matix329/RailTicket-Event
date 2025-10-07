package com.railgraph.service;

import com.railgraph.dto.input.TransactionInputDTO;
import com.railgraph.dto.output.TransactionOutputDTO;
import com.railgraph.dto.output.TicketOutputDTO;
import com.railgraph.event.TransactionEvent;
import com.railgraph.event.TransactionEventProducer;
import com.railgraph.model.Transaction;
import com.railgraph.model.Ticket;
import com.railgraph.repository.TransactionRepository;
import com.railgraph.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private TransactionEventProducer transactionEventProducer;
    
    public TransactionOutputDTO createTransaction(TransactionInputDTO dto) {
        Optional<Ticket> ticket = ticketRepository.findById(dto.getTicketId());
        if (ticket.isEmpty()) {
            throw new IllegalArgumentException("Ticket with ID " + dto.getTicketId() + " does not exist");
        }
        
        Transaction transaction = new Transaction(ticket.get(), LocalDateTime.now(), dto.getUserId());
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        transactionEventProducer.sendTransactionEvent(
            new TransactionEvent(ticket.get().getId(), dto.getUserId(), LocalDateTime.now())
        );
        
        return mapToTransactionOutputDTO(savedTransaction);
    }
    
    public List<TransactionOutputDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToTransactionOutputDTO)
                .collect(Collectors.toList());
    }
    
    public TransactionOutputDTO getTransactionById(Long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if (transaction.isEmpty()) {
            throw new IllegalArgumentException("Transaction with ID " + id + " does not exist");
        }
        return mapToTransactionOutputDTO(transaction.get());
    }
    
    public List<TransactionOutputDTO> getTransactionsByUser(String userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::mapToTransactionOutputDTO)
                .collect(Collectors.toList());
    }
    
    private TransactionOutputDTO mapToTransactionOutputDTO(Transaction transaction) {
        TicketOutputDTO ticketDTO = mapToTicketOutputDTO(transaction.getTicket());
        return new TransactionOutputDTO(
            transaction.getId(),
            ticketDTO,
            transaction.getTimestamp(),
            transaction.getUserId()
        );
    }
    
    private TicketOutputDTO mapToTicketOutputDTO(Ticket ticket) {
        return new TicketOutputDTO(
            ticket.getId(),
            mapToRouteOutputDTO(ticket.getRoute()),
            ticket.getBasePrice(),
            ticket.getFinalPrice(),
            ticket.getDiscountType(),
            ticket.getQuantity()
        );
    }
    
    private com.railgraph.dto.output.RouteOutputDTO mapToRouteOutputDTO(com.railgraph.model.Route route) {
        com.railgraph.dto.output.StationOutputDTO stationFrom = new com.railgraph.dto.output.StationOutputDTO(
            route.getStationFrom().getId(),
            route.getStationFrom().getName(),
            route.getStationFrom().getCity()
        );
        com.railgraph.dto.output.StationOutputDTO stationTo = new com.railgraph.dto.output.StationOutputDTO(
            route.getStationTo().getId(),
            route.getStationTo().getName(),
            route.getStationTo().getCity()
        );
        return new com.railgraph.dto.output.RouteOutputDTO(
            route.getId(),
            stationFrom,
            stationTo,
            route.getTravelTimeMinutes(),
            route.getPrice()
        );
    }
}