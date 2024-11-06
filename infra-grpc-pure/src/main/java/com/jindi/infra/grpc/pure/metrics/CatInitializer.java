package com.jindi.infra.grpc.pure.metrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.DefaultClientConfigManager;
import com.jindi.infra.grpc.pure.constant.CloudPlatformEnum;
import com.jindi.infra.grpc.pure.constant.EnvEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CatInitializer {

	public static void initCat(CloudPlatformEnum cloudPlatform, String appName, EnvEnum env) {
		String home = System.getenv("HOME");
		System.setProperty("CAT_HOME", home + "/logs/cat");
		DefaultClientConfigManager.appName = appName;
		if (env == null) {
			env = EnvEnum.DEFAULT_ENV;
		}
		try (InputStream inputStream = CatInitializer.class
				.getResourceAsStream(String.format("/client-%s-%s.xml", cloudPlatform.getName(), env.getName()))) {
			File file = File.createTempFile("cat", ".xml");
			try (FileOutputStream fos = new FileOutputStream(file)) {
				byte[] b = new byte[1024];
				int length;
				while ((length = inputStream.read(b)) > 0) {
					fos.write(b, 0, length);
				}
			}
			DefaultClientConfigManager.clientFile = file;
			Cat.initialize();
		} catch (IOException e) {
			log.error("cat init client-{}.xml error", env.getName(), e);
		}
	}
}
