package com.jindi.infra.space.user;

import com.jindi.infra.space.SpaceCloudAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SpaceCloudAutoConfiguration.class)
public class UserConfig {

    @Bean
    public UserQuerySpaceCloud userQuerySpaceCloud() {
        return new UserQuerySpaceCloud();
    }

    @Bean
    public UserRomaQuerySpaceCloud userRomaQuerySpaceCloud () {
        return new UserRomaQuerySpaceCloud();
    }
}
