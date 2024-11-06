package com.jindi.infra.datasource.metrics;


import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceUnwrapper;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HikariDataSourceMetricsBinder  implements MeterBinder, DataSourceMetricsBinder {


    private static final Pattern instancePattern = Pattern.compile("jdbc:mysql://.*?/");

    @Autowired(required = false)
    private List<DataSource> dataSourceList;
    private static Iterable<Tag> sTags;

    public HikariDataSourceMetricsBinder() {
        this(Collections.emptyList());
    }

    public HikariDataSourceMetricsBinder(Iterable<Tag> tags) {
        sTags = tags;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        if (CollectionUtils.isEmpty(dataSourceList)) {
            log.debug("数据源配置为空");
            return;
        }

        for (DataSource dataSource : dataSourceList) {
            binDataSourceMetrics(dataSource, registry);
        }
    }

    @Override
    public void binDataSourceMetrics(DataSource dataSource, MeterRegistry registry) {
        HikariDataSource hikariDataSource = DataSourceUnwrapper.unwrap(dataSource, HikariConfigMXBean.class, HikariDataSource.class);
        if (hikariDataSource != null && StringUtils.isNotBlank(hikariDataSource.getJdbcUrl())) {
            Counter.builder("tyc.datasource.relation").tags(Tags.concat(sTags, new String[]{"datasource", getPoolName(hikariDataSource), "db_instance", getInstance(hikariDataSource.getJdbcUrl()), "db_database", getDataBase(hikariDataSource.getJdbcUrl())})).description("").register(registry);
        }
    }

    private String getPoolName(HikariDataSource hikariDataSource) {
        return StringUtils.isBlank(hikariDataSource.getPoolName()) ? "null" : hikariDataSource.getPoolName();
    }

    private String getDataBase(String jdbcUrl) {
        if (StringUtils.isBlank(jdbcUrl)) {
            return "null";
        }
        if (jdbcUrl.contains("?")) {
            jdbcUrl = jdbcUrl.substring(0, jdbcUrl.indexOf("?"));
        }
        return jdbcUrl.substring(jdbcUrl.lastIndexOf("/") + 1);
    }

    private String getInstance(String jdbcUrl) {
        if (StringUtils.isBlank(jdbcUrl)) {
            return "null";
        }
        Matcher matcher = instancePattern.matcher(jdbcUrl);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return jdbcUrl;
    }

    @Override
    public String getPoolName(DataSource dataSource) {
        HikariDataSource hikariDataSource = DataSourceUnwrapper.unwrap(dataSource, HikariConfigMXBean.class, HikariDataSource.class);
        if (hikariDataSource != null) {
            return StringUtils.isBlank(hikariDataSource.getPoolName()) ? "null" : hikariDataSource.getPoolName();
        }
        return "";
    }

    @Override
    public String getInstance(DataSource dataSource) {
        HikariDataSource hikariDataSource = DataSourceUnwrapper.unwrap(dataSource, HikariConfigMXBean.class, HikariDataSource.class);
        if (hikariDataSource != null && StringUtils.isNotBlank(hikariDataSource.getJdbcUrl())) {
            return getInstance(hikariDataSource.getJdbcUrl());
        }
        return "";
    }

    @Override
    public String getDataBase(DataSource dataSource) {
        HikariDataSource hikariDataSource = DataSourceUnwrapper.unwrap(dataSource, HikariConfigMXBean.class, HikariDataSource.class);
        if (hikariDataSource != null && StringUtils.isNotBlank(hikariDataSource.getJdbcUrl())) {
            return getDataBase(hikariDataSource.getJdbcUrl());
        }
        return "";
    }
}
