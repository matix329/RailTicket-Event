package com.railgraph.dto.input;

public class TransactionInputDTO {
    private Long ticketId;
    private String userId;

    public TransactionInputDTO() {}

    public TransactionInputDTO(Long ticketId, String userId) {
        this.ticketId = ticketId;
        this.userId = userId;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}