package com.jindi.infra.dataapi.oneservice.query;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.dataapi.oneservice.client.OneServiceHttpClient;
import com.jindi.infra.dataapi.oneservice.param.OneServiceDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryOneService {

	@Autowired
	private OneServiceHttpClient client;

	public Integer queryCount(OneServiceDTO oneServiceDTO) {
		return queryAll(oneServiceDTO, Integer.class);
	}

	public <T> T queryAll(OneServiceDTO oneServiceDTO, Class<T> resultType) {
		if (resultType == Boolean.class
				|| resultType == Short.class
				|| resultType == Integer.class
				|| resultType == Long.class
				|| resultType == Double.class
				|| resultType == Date.class) {
			return singleAttribute(oneServiceDTO, resultType);
		}
		return client.get(resultType, oneServiceDTO);
	}

	public <T> List<T> queryList(OneServiceDTO oneServiceDTO, Class<T> resultType) {
		return client.getList(resultType, oneServiceDTO);
	}

	public <T> T queryFirst(OneServiceDTO oneServiceDTO, Class<T> resultType) {
		return client.getOne(resultType, oneServiceDTO);
	}

	private <T> T singleAttribute(OneServiceDTO oneServiceDTO, Class<T> resultType) {
		Map map = client.getOne(Map.class, oneServiceDTO);
		if (map == null || map.isEmpty()) {
			return null;
		}
		String sa = String.valueOf(map.values().stream().findFirst().get());
		if (StringUtils.isBlank(sa)) {
			return null;
		}
		return InnerJSONUtils.parseObject(sa, resultType);
	}
}
