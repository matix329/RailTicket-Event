package com.railgraph.controller;

import com.railgraph.dto.input.TicketInputDTO;
import com.railgraph.dto.output.TicketOutputDTO;
import com.railgraph.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    @PostMapping
    public TicketOutputDTO createTicket(@RequestBody TicketInputDTO ticketInput, @RequestParam(value = "userId", defaultValue = "user123") String userId) {
        return ticketService.buyTicket(ticketInput, userId);
    }

    @GetMapping
    public List<TicketOutputDTO> getAllTickets() {
        return ticketService.getAllTickets();
    }
    
    @GetMapping("/{id}/validate")
    public boolean validateTicket(@PathVariable("id") Long id) {
        return ticketService.validateTicket(id);
    }
}