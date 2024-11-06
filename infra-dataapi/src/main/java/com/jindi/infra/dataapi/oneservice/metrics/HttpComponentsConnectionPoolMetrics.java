//package com.jindi.infra.dataapi.oneservice.metrics;
//
//import java.util.Set;
//
//import javax.annotation.Resource;
//
//import org.apache.http.conn.routing.HttpRoute;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.apache.http.pool.PoolStats;
//import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
//
//import io.micrometer.core.instrument.Gauge;
//import io.micrometer.core.instrument.MeterRegistry;
//
//public class HttpComponentsConnectionPoolMetrics implements MeterRegistryCustomizer {
//
//    @Resource
//    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
//
//    @Override
//    public void customize(MeterRegistry registry) {
//        Set<HttpRoute> routes = poolingHttpClientConnectionManager.getRoutes();
//        for(HttpRoute route : routes) {
//            PoolStats stats = poolingHttpClientConnectionManager.getStats(route);
//            Gauge.builder("http_components_per_connections", stats, this::getAvailableCount)
//                    .tag("remoteHost", route.getTargetHost().getHostName() + ":" + route.getTargetHost().getPort())
//                    .tags("type", "available")
//                    .description("one service http components per connections available stat")
//                    .register(registry);
//            Gauge.builder("http_components_per_connections", stats, this::getLeasedCount)
//                    .tag("remoteHost", route.getTargetHost().getHostName() + ":" + route.getTargetHost().getPort())
//                    .tags("type", "leased")
//                    .description("one service http components per connections leased stat")
//                    .register(registry);
//            Gauge.builder("http_components_per_connections", stats, this::getPendingCount)
//                    .tag("remoteHost", route.getTargetHost().getHostName() + ":" + route.getTargetHost().getPort())
//                    .tags("type", "pending")
//                    .description("one service http components per connections pending stat")
//                    .register(registry);
//            Gauge.builder("http_components_per_connections", stats, this::getMaxCount)
//                    .tag("remoteHost", route.getTargetHost().getHostName() + ":" + route.getTargetHost().getPort())
//                    .tags("type", "max")
//                    .description("one service http components per connections max stat")
//                    .register(registry);
//        }
//    }
//
//    public double getAvailableCount(PoolStats stats) {
//        return Double.valueOf(stats.getAvailable());
//    }
//
//    private double getLeasedCount(PoolStats stats) {
//        return Double.valueOf(stats.getLeased());
//    }
//
//    private double getPendingCount(PoolStats stats) {
//        return Double.valueOf(stats.getPending());
//    }
//
//    private double getMaxCount(PoolStats stats) {
//        return Double.valueOf(stats.getMax());
//    }
//}
