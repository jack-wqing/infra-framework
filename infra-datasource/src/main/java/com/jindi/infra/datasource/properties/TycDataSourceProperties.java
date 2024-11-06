package com.jindi.infra.datasource.properties;


import lombok.Data;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;

import java.util.Hashtable;
import java.util.Properties;

@Data
public class TycDataSourceProperties extends Hashtable<String, TycDataSourceHolder> {

    //mybatis的通用配置
    private Properties mybatis;

    private Boolean needGenEmptyDataSource = false;

}
