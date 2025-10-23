-- Analytics tables for Flink streaming results
-- These tables store aggregated metrics from real-time stream processing

-- Table for route ticket sales analytics (5-minute windows)
CREATE TABLE IF NOT EXISTS route_analytics (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    tickets_sold INT NOT NULL,
    total_revenue DECIMAL(10, 2),
    average_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_route_window UNIQUE (route_id, window_start, window_end)
);

-- Table for top routes analytics
CREATE TABLE IF NOT EXISTS top_routes_analytics (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    route_name VARCHAR(255),
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    tickets_sold INT NOT NULL,
    rank INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for capacity alerts (when route availability drops below threshold)
CREATE TABLE IF NOT EXISTS capacity_alerts (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    alert_timestamp TIMESTAMP NOT NULL,
    available_seats INT NOT NULL,
    total_capacity INT NOT NULL,
    availability_percent DECIMAL(5, 2) NOT NULL,
    alert_message VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for hourly revenue analytics
CREATE TABLE IF NOT EXISTS revenue_analytics (
    id BIGSERIAL PRIMARY KEY,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    total_tickets_sold INT NOT NULL,
    total_revenue DECIMAL(12, 2) NOT NULL,
    average_ticket_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_revenue_window UNIQUE (window_start, window_end)
);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_route_analytics_route_id ON route_analytics(route_id);
CREATE INDEX IF NOT EXISTS idx_route_analytics_window_start ON route_analytics(window_start DESC);
CREATE INDEX IF NOT EXISTS idx_top_routes_window_start ON top_routes_analytics(window_start DESC);
CREATE INDEX IF NOT EXISTS idx_capacity_alerts_timestamp ON capacity_alerts(alert_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_revenue_analytics_window_start ON revenue_analytics(window_start DESC);

-- View for latest route analytics (last 24 hours)
CREATE OR REPLACE VIEW latest_route_analytics AS
SELECT
    ra.route_id,
    SUM(ra.tickets_sold) as total_tickets_24h,
    SUM(ra.total_revenue) as total_revenue_24h,
    AVG(ra.average_price) as avg_price_24h,
    MAX(ra.window_end) as last_updated
FROM route_analytics ra
WHERE ra.window_start > CURRENT_TIMESTAMP - INTERVAL '24 hours'
GROUP BY ra.route_id
ORDER BY total_revenue_24h DESC;

-- View for active capacity alerts (last hour)
CREATE OR REPLACE VIEW active_capacity_alerts AS
SELECT
    ca.route_id,
    ca.alert_timestamp,
    ca.available_seats,
    ca.total_capacity,
    ca.availability_percent,
    ca.alert_message
FROM capacity_alerts ca
WHERE ca.alert_timestamp > CURRENT_TIMESTAMP - INTERVAL '1 hour'
ORDER BY ca.alert_timestamp DESC;
