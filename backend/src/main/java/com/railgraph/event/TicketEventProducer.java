package com.railgraph.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TicketEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TicketEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTicketEvent(TicketEvent event) {
        kafkaTemplate.send("ticket-events", event.getRouteId().toString(), event);
    }
}