package com.jindi.infra.datasource.autoconfig;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

@Configuration
@ConditionalOnClass({PageHelper.class, SqlSessionFactory.class, PageHelperProperties.class})
@EnableConfigurationProperties({PageHelperProperties.class})
@ConditionalOnMissingBean(type = "com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration")
public class PageHelperAutoConfiguration {
    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactoryList;
    @Autowired(required = false)
    private PageHelperProperties properties;

    @Bean
    @ConfigurationProperties(
            prefix = "pagehelper"
    )
    public Properties pageHelperProperties() {
        return new Properties();
    }

    @PostConstruct
    public void addPageInterceptor() {
        if (CollectionUtils.isEmpty(sqlSessionFactoryList)) {
            return;
        }
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.putAll(this.pageHelperProperties());
        if (this.properties != null) {
            properties.putAll(this.properties.getProperties());
        }
        pageInterceptor.setProperties(properties);
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            if (sqlSessionFactory.getConfiguration().getInterceptors().stream().noneMatch(interceptor->interceptor.getClass().getSimpleName().contains("PageIntercetor"))) {
                sqlSessionFactory.getConfiguration().addInterceptor(pageInterceptor);
            }
        }
    }
}
