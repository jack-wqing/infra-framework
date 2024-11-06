package com.jindi.infra.tools.aliyun.oss;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

/**
 * https://help.aliyun.com/document_detail/32010.html?spm=a2c4g.11186623.6.935.191546a1WVOzFX
 */
@Configuration
@EnableConfigurationProperties(OSSProperties.class)
@ConditionalOnClass(OSS.class)
public class OSSConfiguration {

	@ConditionalOnMissingBean(name = "oss")
	@Bean(name = "oss")
	public OSS oss(OSSProperties ossProperties) {
		ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
		conf.setSupportCname(true);
		conf.setConnectionRequestTimeout(ossProperties.getConnectionRequestTimeout());
		conf.setConnectionTimeout(ossProperties.getSocketTimeout());
		conf.setRequestTimeout(ossProperties.getRequestTimeout());
		return new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(),
				ossProperties.getAccessKeySecret(), conf);
	}

	@ConditionalOnMissingBean(name = "ossTemplate")
	@Bean(name = "ossTemplate")
	public OSSTemplate ossTemplate() {
		return new OSSTemplate();
	}
}
