package com.railgraph.dto.output;

import java.time.LocalDateTime;

public class TransactionOutputDTO {
    private Long id;
    private TicketOutputDTO ticket;
    private LocalDateTime timestamp;
    private String userId;

    public TransactionOutputDTO() {}

    public TransactionOutputDTO(Long id, TicketOutputDTO ticket, LocalDateTime timestamp, String userId) {
        this.id = id;
        this.ticket = ticket;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TicketOutputDTO getTicket() { return ticket; }
    public void setTicket(TicketOutputDTO ticket) { this.ticket = ticket; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}