package com.railgraph.event;

import java.time.LocalDateTime;

public class TransactionEvent {
    private Long ticketId;
    private String userId;
    private LocalDateTime timestamp;

    public TransactionEvent() {}

    public TransactionEvent(Long ticketId, String userId, LocalDateTime timestamp) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}