package com.jindi.infra.datasource.metrics;


import io.micrometer.core.instrument.MeterRegistry;

import javax.sql.DataSource;

public interface DataSourceMetricsBinder {

    void binDataSourceMetrics(DataSource dataSource, MeterRegistry registry);
    String getPoolName(DataSource dataSource);
    String getInstance(DataSource dataSource);
    String getDataBase(DataSource dataSource);
}
