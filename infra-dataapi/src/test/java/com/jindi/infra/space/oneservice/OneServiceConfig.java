package com.jindi.infra.space.oneservice;

import com.jindi.infra.dataapi.oneservice.config.OneServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OneServiceAutoConfiguration.class)
public class OneServiceConfig {

    @Bean
    public OneServiceQuery oneServiceQuery() {
        return new OneServiceQuery();
    }

}
