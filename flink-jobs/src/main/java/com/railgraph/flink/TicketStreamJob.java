package com.railgraph.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.Properties;

public class TicketStreamJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("ticket-events")
                .setGroupId("ticket-analytics")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> kafkaStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        DataStream<TicketEvent> ticketEvents = kafkaStream
                .map(json -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    return mapper.readValue(json, TicketEvent.class);
                })
                .name("Parse Ticket Events");

        ticketEvents
                .keyBy(TicketEvent::getRouteId)
                .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
                .reduce(new TicketQuantityReducer())
                .map(event -> String.format(
                        "Route %d sold %d tickets in last 5 minutes",
                        event.getRouteId(), event.getQuantity()))
                .print()
                .name("Print Analytics");

        env.execute("Ticket Analytics Job");
    }

    public static class TicketQuantityReducer implements ReduceFunction<TicketEvent> {
        @Override
        public TicketEvent reduce(TicketEvent a, TicketEvent b) {
            a.setQuantity(a.getQuantity() + b.getQuantity());
            return a;
        }
    }
}