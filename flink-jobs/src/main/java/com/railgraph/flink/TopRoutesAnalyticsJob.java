package com.railgraph.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.railgraph.flink.aggregator.RouteWindowAggregate;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TopRoutesAnalyticsJob {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String KAFKA_TOPIC = "ticket-events";
    private static final String KAFKA_GROUP_ID = "top-routes-analytics";
    private static final int TOP_N = 5;

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(60000);
        env.setParallelism(1);

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
                "Kafka Source"
        );

        DataStream<TicketEvent> ticketEvents = kafkaStream
                .map(json -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    return mapper.readValue(json, TicketEvent.class);
                })
                .name("Parse Ticket Events");

        DataStream<Tuple3<Long, Integer, Long>> routeSales = ticketEvents
                .keyBy(TicketEvent::getRouteId)
                .window(TumblingProcessingTimeWindows.of(Time.minutes(10)))
                .aggregate(new TicketCountAggregator())
                .name("Aggregate Ticket Sales by Route");

        DataStream<String> topRoutes = routeSales
                .keyBy(tuple -> 0L)
                .process(new TopNRoutesProcessor(TOP_N))
                .name("Calculate Top N Routes");

        topRoutes.print().name("Print Top Routes");

        env.execute("Top-N Routes Analytics Job");
    }

    public static class TicketCountAggregator
            implements AggregateFunction<TicketEvent, RouteWindowAggregate, Tuple3<Long, Integer, Long>> {

        @Override
        public RouteWindowAggregate createAccumulator() {
            return new RouteWindowAggregate();
        }

        @Override
        public RouteWindowAggregate add(TicketEvent event, RouteWindowAggregate acc) {
            acc.setRouteId(event.getRouteId());
            acc.addTicket(event.getQuantity(), event.getFinalPrice());
            return acc;
        }

        @Override
        public Tuple3<Long, Integer, Long> getResult(RouteWindowAggregate acc) {
            return Tuple3.of(acc.getRouteId(), acc.getTotalTicketsSold(), System.currentTimeMillis());
        }

        @Override
        public RouteWindowAggregate merge(RouteWindowAggregate acc1, RouteWindowAggregate acc2) {
            acc1.merge(acc2);
            return acc1;
        }
    }

    public static class TopNRoutesProcessor
            extends KeyedProcessFunction<Long, Tuple3<Long, Integer, Long>, String> {

        private final int topN;
        private transient ListState<Tuple3<Long, Integer, Long>> routeSalesState;

        public TopNRoutesProcessor(int topN) {
            this.topN = topN;
        }

        @Override
        public void open(Configuration parameters) {
            ListStateDescriptor<Tuple3<Long, Integer, Long>> descriptor =
                    new ListStateDescriptor<>(
                            "route-sales-state",
                            Tuple3.class
                    );
            routeSalesState = getRuntimeContext().getListState(descriptor);
        }

        @Override
        public void processElement(
                Tuple3<Long, Integer, Long> value,
                Context ctx,
                Collector<String> out) throws Exception {

            routeSalesState.add(value);

            List<Tuple3<Long, Integer, Long>> allSales = new ArrayList<>();
            for (Tuple3<Long, Integer, Long> sale : routeSalesState.get()) {
                allSales.add(sale);
            }

            allSales.sort(Comparator.comparingInt((Tuple3<Long, Integer, Long> t) -> t.f1).reversed());

            if (allSales.size() > topN * 2) {
                List<Tuple3<Long, Integer, Long>> topSales = allSales.subList(0, Math.min(topN * 2, allSales.size()));
                routeSalesState.clear();
                for (Tuple3<Long, Integer, Long> sale : topSales) {
                    routeSalesState.add(sale);
                }
                allSales = topSales;
            }

            StringBuilder result = new StringBuilder("\n========== TOP " + topN + " ROUTES ==========\n");
            for (int i = 0; i < Math.min(topN, allSales.size()); i++) {
                Tuple3<Long, Integer, Long> sale = allSales.get(i);
                result.append(String.format("  %d. Route %d: %d tickets sold\n",
                        i + 1, sale.f0, sale.f1));
            }
            result.append("====================================\n");

            out.collect(result.toString());
        }
    }
}
