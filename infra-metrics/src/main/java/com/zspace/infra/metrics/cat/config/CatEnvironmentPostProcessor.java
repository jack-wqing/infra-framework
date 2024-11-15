package com.zspace.infra.metrics.cat.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.DefaultClientConfigManager;
import com.jindi.infra.common.constant.CommonConstant;
import com.jindi.infra.common.util.InnerEnvironmentUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CatEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String PROPERTIES_FILE = "/META-INF/app.properties";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String appName = loadProjectName();
		DefaultClientConfigManager.appName = StringUtils.isEmpty(appName)
				? environment.getProperty(CommonConstant.APPLICATION_NAME)
				: appName;
		String[] activeProfiles = environment.getActiveProfiles();
		if (InnerEnvironmentUtils.isDev(activeProfiles)) {
			String home = System.getenv("HOME");
			environment.getSystemProperties().put("CAT_HOME", home + "/logs/cat");
			initDevCat();
		}
	}

	private String loadProjectName() {
		String appName = null;
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);

			if (in == null) {
				in = Cat.class.getResourceAsStream(PROPERTIES_FILE);
			}
			if (in != null) {
				Properties prop = new Properties();
				prop.load(in);
				return prop.getProperty("app.name");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return appName;
	}

	/**
	 * cat 环境初始化
	 */
	private void catInitialize() {
		if (Cat.isInitialized()) {
			return;
		}
		synchronized (Cat.class) {
			Thread thread = new Thread(() -> {
				if (Cat.isInitialized()) {
					return;
				}
				long startTime = System.currentTimeMillis();
				System.out.println("cat initialize doing");
				try {
					Cat.initialize();
				} catch (Throwable e) {
					log.error("cat initialize", e);
				}
				log.info("cat initialize finish costTime = {} ms", System.currentTimeMillis() - startTime);
			});
			thread.start();
		}
	}

	private void initDevCat() {
		try (InputStream inputStream = CatEnvironmentPostProcessor.class.getResourceAsStream("/client-dev.xml")) {
			File file = File.createTempFile("cat", ".xml");
			FileOutputStream fos = new FileOutputStream(file);
			byte[] b = new byte[1024];
			int length;
			while ((length = inputStream.read(b)) > 0) {
				fos.write(b, 0, length);
			}
			inputStream.close();
			fos.close();
			DefaultClientConfigManager.clientFile = file;
		} catch (IOException e) {
			log.error("cat init client-dev.xml error", e);
		}
	}
}
