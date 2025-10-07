#!/bin/bash

echo "Building RailTicket-Event with Kafka + Flink integration..."

mvn clean package -DskipTests

cd flink-jobs
mvn clean package -DskipTests
cd ..

echo "Starting Docker Compose stack..."
docker-compose up --build -d

echo "Waiting for services to start..."
sleep 30

echo "Deploying Flink job..."
docker exec -it railgraph-flink-jobmanager flink run /opt/flink/usrlib/flink-jobs.jar

echo "System is ready!"
echo "Backend API: http://localhost:8085"
echo "Flink Dashboard: http://localhost:8081"
echo "Kafka: localhost:29092"


