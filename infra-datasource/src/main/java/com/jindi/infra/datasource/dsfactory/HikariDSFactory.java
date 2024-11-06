package com.jindi.infra.datasource.dsfactory;

import com.dianping.cat.Cat;
import com.google.common.collect.Sets;
import com.jindi.infra.tools.util.PropertiesUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


@Slf4j
public class HikariDSFactory extends BaseDataSourceFactory {

    private static Set<String> focusField = new HashSet<>();

    private static final Integer RETRY_TIME = 3;

    private static final String RETRY_CAT_EVENT = "DataSource.init.retry";

    @Override
    public String poolType() {
        return "hikari";
    }

    @Override
    public Properties fixProperties(Properties properties) {
        PropertiesUtils.mergeProperties(PropertiesUtils.getPropertiesByPrefix(properties, "hikari"), properties);
        return PropertiesUtils.toCamel(pickFocusProperties(properties));
    }

    @Override
    public DataSource createDataSource(String beanName, Properties fixedProperties, Properties properties) {
        HikariConfig hikariConfig = new HikariConfig(fixedProperties);
        hikariConfig.setPoolName(beanName);
        return createDataSourceWithRetry(hikariConfig, 1);
    }

    private DataSource createDataSourceWithRetry(HikariConfig hikariConfig, Integer time) {
        try {
            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            if (isConfigError(e)) {
                log.error("存在连接异常的配置, {}", hikariConfig.getJdbcUrl(), e);
                return DataSourceBuilder.create().type(HikariDataSource.class)
                        .driverClassName(hikariConfig.getDriverClassName())
                        .url(hikariConfig.getJdbcUrl())
                        .username(hikariConfig.getUsername())
                        .password(hikariConfig.getPassword()).build();
            }
            if (time <= RETRY_TIME) {
                try {
                    Thread.sleep(1000L);
                } catch (Exception threadException) {
                }
                Cat.logEvent(RETRY_CAT_EVENT, hikariConfig.getJdbcUrl());
                return createDataSourceWithRetry(hikariConfig, time + 1);
            }
            throw e;
        }
    }

    private boolean isConfigError(Exception e) {
        return isUnknowHostException(e) || isAccessDenyException(e);
    }

    private boolean isAccessDenyException(Exception e) {
        try {
            if (e.getCause() instanceof SQLException && e.getMessage().toLowerCase().contains("access denied")) {
                return true;
            }
        } catch (Exception ex) {
        }
        return false;
    }

    private boolean isUnknowHostException(Exception e) {
        try {
            if (e.getCause().getCause() instanceof UnknownHostException || e.getCause().getCause().getCause() instanceof UnknownHostException) {
                return true;
            }
        } catch (Exception ex) {
        }
        return false;
    }

    private Properties pickFocusProperties(Properties properties) {
        Properties result = new Properties();
        properties.forEach((key, value) -> {
            if (focusField.contains(String.valueOf(key))) {
                result.put(key, value);
            }
        });
        return result;
    }

    static {
        focusField = Sets.newHashSet("jdbcUrl","username","password","data-source-class-name","driver-class-name","auto-commit","connection-timeout","idle-timeout","max-lifetime","connection-test-query","minimum-idle","maximum-pool-size","metric-registry","health-check-registry","pool-name","initialization-fail-timeout","isolate-internal-queries","allow-pool-suspension","read-only","register-mbeans","catalog","connection-init-sql","transaction-isolation","validation-timeout","leak-detection-threshold","data-source","schema","thread-factory","scheduled-executor");
    }
}