package com.jindi.infra.biz;

import com.jindi.common.tools.context.LocationContext;
import com.jindi.common.tools.context.TycRequestContext;
import com.jindi.infra.biz.context.RequestContextFeignCallInterceptor;
import com.jindi.infra.biz.context.RequestContextGrpcCallInterceptor;
import com.jindi.infra.biz.context.RequestContextHttpFilter;
import com.jindi.infra.biz.context.filler.LocationRequestContextFiller;
import com.jindi.infra.biz.context.filler.RequestContextFiller;
import com.jindi.infra.biz.context.filler.TycRequestContextFiller;
import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.grpc.extension.RequestFilter;
import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@Import({BizAutoConfiguration.WebBizAutoConfiguration.class,
		BizAutoConfiguration.OpenFeignBizAutoConfiguration.class,
		BizAutoConfiguration.RPCAutoConfiguration.class
})
public class BizAutoConfiguration {

	@Configuration
	@ConditionalOnClass({CallInterceptor.class, RequestFilter.class})
	public class RPCAutoConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public RequestContextGrpcCallInterceptor userRequestCallInterceptor() {
			return new RequestContextGrpcCallInterceptor();
		}

	}

	@Configuration
	@ConditionalOnClass({FilterRegistrationBean.class})
	@Import({BizAutoConfiguration.WebBizAutoConfiguration.LocationContextAutoConfiguration.class,
			BizAutoConfiguration.WebBizAutoConfiguration.TycRequestContextAutoConfiguration.class})
	public class WebBizAutoConfiguration {

		@ConditionalOnWebApplication
		@Bean
		public FilterRegistrationBean<RequestContextHttpFilter> requestContextHttpFilter(List<RequestContextFiller> fillerList) {
			FilterRegistrationBean<RequestContextHttpFilter> registration = new FilterRegistrationBean<>();
			RequestContextHttpFilter requestContextHttpFilter = new RequestContextHttpFilter(fillerList);
			registration.setFilter(requestContextHttpFilter);
			registration.addUrlPatterns("/*");
			registration.setName("requestContextHttpFilter");
			return registration;
		}

		@Configuration
		@ConditionalOnClass({LocationContext.class})
		public class LocationContextAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public LocationRequestContextFiller locationRequestContextFiller() {
				return new LocationRequestContextFiller();
			}
		}

		@Configuration
		@ConditionalOnClass({TycRequestContext.class})
		public class TycRequestContextAutoConfiguration {
			@ConditionalOnMissingBean
			@Bean
			public TycRequestContextFiller tycRequestContextFiller() {
				return new TycRequestContextFiller();
			}
		}
    }

	@Configuration
	@ConditionalOnClass({Feign.class})
	public class OpenFeignBizAutoConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public RequestContextFeignCallInterceptor userOpenFeignRequestInterceptor() {
			return new RequestContextFeignCallInterceptor();
		}
	}
}
