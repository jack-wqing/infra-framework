package com.external.jindi.infra;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jindi.infra.governance.lane.LaneRule;
import com.netflix.loadbalancer.IRule;

import feign.Feign;

@Configuration
@ConditionalOnClass({Feign.class})
@ConditionalOnProperty(name = "rpc.register", havingValue = "true", matchIfMissing = true)
public class OpenFeignGovernanceAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public IRule ribbonRule() {
		return new LaneRule();
	}

}
