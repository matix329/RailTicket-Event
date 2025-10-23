package com.railgraph.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.railgraph.flink.aggregator.RouteWindowAggregate;
import com.railgraph.flink.model.RouteAnalytics;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

public class AdvancedTicketAnalyticsJob {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String KAFKA_TOPIC = "ticket-events";
    private static final String KAFKA_GROUP_ID = "advanced-ticket-analytics";

    private static final String JDBC_URL = "jdbc:postgresql://postgres:5432/railgraph";
    private static final String JDBC_USER = "railgraph";
    private static final String JDBC_PASSWORD = "railgraph123";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(60000);

        env.setParallelism(4);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setTopics(KAFKA_TOPIC)
                .setGroupId(KAFKA_GROUP_ID)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> kafkaStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka Ticket Events Source"
        );

        DataStream<TicketEvent> ticketEvents = kafkaStream
                .map(json -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    return mapper.readValue(json, TicketEvent.class);
                })
                .name("Parse Ticket Events");

        DataStream<RouteAnalytics> routeAnalytics = ticketEvents
                .keyBy(TicketEvent::getRouteId)
                .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
                .aggregate(
                    new RouteAnalyticsAggregator(),
                    new RouteAnalyticsWindowProcessor()
                )
                .name("Aggregate Route Analytics");

        routeAnalytics
                .map(analytics -> String.format(
                        "[ANALYTICS] Route %d: Sold %d tickets, Revenue: %.2f, Avg Price: %.2f (Window: %s to %s)",
                        analytics.getRouteId(),
                        analytics.getTicketsSold(),
                        analytics.getTotalRevenue(),
                        analytics.getAveragePrice(),
                        analytics.getWindowStart(),
                        analytics.getWindowEnd()
                ))
                .print()
                .name("Print Analytics");

        routeAnalytics.addSink(
                JdbcSink.sink(
                    "INSERT INTO route_analytics (route_id, window_start, window_end, tickets_sold, total_revenue, average_price) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (route_id, window_start, window_end) " +
                    "DO UPDATE SET tickets_sold = EXCLUDED.tickets_sold, " +
                    "total_revenue = EXCLUDED.total_revenue, " +
                    "average_price = EXCLUDED.average_price",
                    (JdbcStatementBuilder<RouteAnalytics>) (statement, analytics) -> {
                        statement.setLong(1, analytics.getRouteId());
                        statement.setTimestamp(2, analytics.getWindowStart());
                        statement.setTimestamp(3, analytics.getWindowEnd());
                        statement.setInt(4, analytics.getTicketsSold());
                        statement.setBigDecimal(5, analytics.getTotalRevenue());
                        statement.setBigDecimal(6, analytics.getAveragePrice());
                    },
                    JdbcExecutionOptions.builder()
                            .withBatchSize(100)
                            .withBatchIntervalMs(200)
                            .withMaxRetries(3)
                            .build(),
                    new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                            .withUrl(JDBC_URL)
                            .withDriverName("org.postgresql.Driver")
                            .withUsername(JDBC_USER)
                            .withPassword(JDBC_PASSWORD)
                            .build()
                )
        ).name("PostgreSQL Sink - Route Analytics");

        env.execute("Advanced Ticket Analytics Job");
    }

    public static class RouteAnalyticsAggregator
            implements AggregateFunction<TicketEvent, RouteWindowAggregate, RouteWindowAggregate> {

        @Override
        public RouteWindowAggregate createAccumulator() {
            return new RouteWindowAggregate();
        }

        @Override
        public RouteWindowAggregate add(TicketEvent event, RouteWindowAggregate accumulator) {
            accumulator.setRouteId(event.getRouteId());
            accumulator.addTicket(event.getQuantity(), event.getFinalPrice());
            return accumulator;
        }

        @Override
        public RouteWindowAggregate getResult(RouteWindowAggregate accumulator) {
            return accumulator;
        }

        @Override
        public RouteWindowAggregate merge(RouteWindowAggregate acc1, RouteWindowAggregate acc2) {
            acc1.merge(acc2);
            return acc1;
        }
    }

    public static class RouteAnalyticsWindowProcessor
            extends ProcessWindowFunction<RouteWindowAggregate, RouteAnalytics, Long, TimeWindow> {

        @Override
        public void process(Long routeId,
                          Context context,
                          Iterable<RouteWindowAggregate> elements,
                          Collector<RouteAnalytics> out) {

            RouteWindowAggregate aggregate = elements.iterator().next();

            TimeWindow window = context.window();
            Timestamp windowStart = new Timestamp(window.getStart());
            Timestamp windowEnd = new Timestamp(window.getEnd());

            RouteAnalytics analytics = new RouteAnalytics(
                    routeId,
                    windowStart,
                    windowEnd,
                    aggregate.getTotalTicketsSold(),
                    aggregate.getTotalRevenue(),
                    aggregate.getAveragePrice()
            );

            out.collect(analytics);
        }
    }
}
