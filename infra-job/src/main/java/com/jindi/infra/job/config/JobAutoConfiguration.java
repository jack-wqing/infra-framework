package com.jindi.infra.job.config;

import javax.annotation.Resource;

import com.jindi.infra.job.constant.JobConstant;
import com.jindi.infra.job.trace.JobMDCAspect;
import com.jindi.infra.job.trace.JobTraceAspect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.jindi.infra.common.constant.CommonConstant;
import com.jindi.infra.common.util.InnerIpUtils;
import com.jindi.infra.job.properties.JobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(JobProperties.class)
@Slf4j
public class JobAutoConfiguration {

	@Resource
	private Environment environment;

	@Bean
	public XxlJobSpringExecutor xxlJobExecutor(@Autowired JobProperties jobProperties) {
		String appname = getAppname();
		XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
		xxlJobSpringExecutor.setAdminAddresses(jobProperties.getUrl());
		xxlJobSpringExecutor.setAppname(appname);
		xxlJobSpringExecutor.setAddress("");
		xxlJobSpringExecutor.setIp(InnerIpUtils.getCachedIP());
		xxlJobSpringExecutor.setPort(jobProperties.getJobPort());
		xxlJobSpringExecutor.setAccessToken("");
		xxlJobSpringExecutor.setLogPath(jobProperties.getLogPath());
		xxlJobSpringExecutor.setLogRetentionDays(jobProperties.getLogRetentionDays());
		log.info(">>>>>>>>>>> xxl-job config init: {}", appname);
		return xxlJobSpringExecutor;
	}

	@Bean
	@ConditionalOnClass(name = "com.jindi.infra.trace.http.context.HttpTraceContext")
	public JobTraceAspect jobTraceAspect() {
		return new JobTraceAspect();
	}

	@Bean
	@ConditionalOnMissingClass("com.jindi.infra.trace.http.context.HttpTraceContext")
	public JobMDCAspect jobMDCAspect() {
		return new JobMDCAspect();
	}

	private String getAppname() {
		String appname = environment.getProperty(JobConstant.APPNAME);
		if (StringUtils.isBlank(appname)) {
			appname = environment.getProperty(CommonConstant.APPLICATION_NAME);
		}
		return appname;
	}
}
