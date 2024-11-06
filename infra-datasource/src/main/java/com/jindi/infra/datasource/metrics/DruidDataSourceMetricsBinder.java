package com.jindi.infra.datasource.metrics;


import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceMBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
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
public class DruidDataSourceMetricsBinder  implements MeterBinder, DataSourceMetricsBinder {

    private static final Pattern instancePattern = Pattern.compile("jdbc:mysql://.*?/");

    @Autowired(required = false)
    private List<DataSource> dataSourceList;
    private static Iterable<Tag> sTags;

    public DruidDataSourceMetricsBinder() {
        this(Collections.emptyList());
    }

    public DruidDataSourceMetricsBinder(Iterable<Tag> tags) {
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
        DruidDataSource druidDataSource = DataSourceUnwrapper.unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class);
        if (druidDataSource != null && StringUtils.isNotBlank(druidDataSource.getUrl())) {
            Counter.builder("tyc.datasource.relation").tags(Tags.concat(sTags, new String[]{"datasource", getPoolName(druidDataSource), "db_instance", getInstance(druidDataSource.getUrl()), "db_database", getDataBase(druidDataSource.getUrl())})).description("").register(registry);
        }
    }

    private String getPoolName(DruidDataSource druidDataSource) {
        return StringUtils.isBlank(druidDataSource.getName()) ? "null" : druidDataSource.getName();
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
        DruidDataSource druidDataSource = DataSourceUnwrapper.unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class);
        if (druidDataSource != null) {
            return StringUtils.isBlank(druidDataSource.getName()) ? "null" : druidDataSource.getName();
        }
        return "";
    }

    @Override
    public String getInstance(DataSource dataSource) {
        DruidDataSource druidDataSource = DataSourceUnwrapper.unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class);
        if (druidDataSource != null && StringUtils.isNumeric(druidDataSource.getUrl())) {
            return getInstance(druidDataSource.getUrl());
        }
        return "";
    }

    @Override
    public String getDataBase(DataSource dataSource) {
        DruidDataSource druidDataSource = DataSourceUnwrapper.unwrap(dataSource, DruidDataSourceMBean.class, DruidDataSource.class);
        if (druidDataSource != null && StringUtils.isNumeric(druidDataSource.getUrl())) {
            return getDataBase(druidDataSource.getUrl());
        }
        return "";
    }
}
