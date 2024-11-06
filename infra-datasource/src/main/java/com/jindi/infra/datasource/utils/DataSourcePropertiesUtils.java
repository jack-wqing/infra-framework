package com.jindi.infra.datasource.utils;


import com.jindi.infra.tools.util.PropertiesUtils;

import java.util.List;
import java.util.Properties;

public class DataSourcePropertiesUtils extends PropertiesUtils {

    private static String DEFAULT_DRIVER_CLASS_NAME;
    private static final String[] JDBC_URL_KEY = new String[]{"url", "jdbcUrl", "jdbc-url"};
    private static final String[] JDBC_USER_NAME_KEY = new String[]{"username", "user", "userName", "user-name"};
    private static final String[] JDBC_PASSWORD_KEY = new String[]{"password"};
    private static final String[] JDBC_DRIVER_KEY = new String[]{"driver-class-name", "driverClassName"};
    private static final String[] BASE_PACKAGES = new String[]{"basePackages", "base-packages"};
    private static final String[] BASE_PACKAGE_CLASSES = new String[]{"basePackageClasses", "base-package-classes"};
    private static final String[] MAPPER_LOCATION = new String[]{"mapperLocation", "mapper-location"};
    private static final String[] TRANSACTION_MANAGER = new String[]{"transactionManager", "transaction-manager"};

    static {
        initDefaultDriverClassName();
    }

    private static void initDefaultDriverClassName() {
        if (loadClass("com.mysql.cj.jdbc.Driver") != null) {
            DEFAULT_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
        } else {
            DEFAULT_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
        }
    }

    public static String getJdbcUrl(Properties properties) {
        return getProperty(properties, JDBC_URL_KEY);
    }

    public static String getJdbcUserName(Properties properties) {
        return getProperty(properties, JDBC_USER_NAME_KEY);
    }

    public static String getMapperLocation(Properties properties) {
        return getProperty(properties, MAPPER_LOCATION);
    }

    public static String getBasePackages(Properties properties) {
        return getProperty(properties, BASE_PACKAGES);
    }

    public static String getTransactionManager(Properties properties) {
        return getProperty(properties, TRANSACTION_MANAGER);
    }

    public static String getJdbcDriverClass(Properties properties) {
        return getProperty(properties, JDBC_DRIVER_KEY);
    }

    public static String getJdbcPassword(Properties properties) {
        return getProperty(properties, JDBC_PASSWORD_KEY);
    }

    public static List<Class<?>> getBasePackageClasses(Properties properties) throws ClassNotFoundException {
        String str = getProperty(properties, BASE_PACKAGE_CLASSES);
        return getClasses(str);
    }

    public static Class<?> loadClass(String className) {
        Class<?> clazz = null;

        if (className == null) {
            return null;
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // skip
        }

        ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
        if (ctxClassLoader != null) {
            try {
                clazz = ctxClassLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                // skip
            }
        }

        return clazz;
    }
}
