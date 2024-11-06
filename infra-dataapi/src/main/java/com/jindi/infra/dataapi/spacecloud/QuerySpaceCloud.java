package com.jindi.infra.dataapi.spacecloud;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.dataapi.spacecloud.param.SpaceRequestDTO;
import com.jindi.infra.dataapi.spacecloud.param.WhereComposite;
import com.jindi.infra.dataapi.spacecloud.wrapper.WhereWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuerySpaceCloud extends BaseQuerySpaceCloud implements QueryTemplate {

	/**
	 * @param spaceRequestDTO
	 * @return
	 */
	@Override
	public Integer queryCount(SpaceRequestDTO spaceRequestDTO) {
		return queryAll(spaceRequestDTO, Integer.class);
	}

	/**
	 * @param spaceRequestDTO
	 * @param resultType
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> T queryAll(SpaceRequestDTO spaceRequestDTO, Class<T> resultType) {
		if (resultType == Boolean.class
				|| resultType == Short.class
				|| resultType == Integer.class
				|| resultType == Long.class
				|| resultType == Double.class
				|| resultType == Date.class
		) {
			return singleAttribute(spaceRequestDTO, resultType);
		}
		return graphQLHttpClient().get(resultType, getBody(spaceRequestDTO));
	}

	private <T> T singleAttribute(SpaceRequestDTO spaceRequestDTO, Class<T> resultType) {
		Map map = graphQLHttpClient().getOne(Map.class, getBody(spaceRequestDTO));
		if (map == null || map.isEmpty()) {
			return null;
		}
		String sa = String.valueOf(map.values().stream().findFirst().get());
		if (StringUtils.isBlank(sa)) {
			return null;
		}
		return InnerJSONUtils.parseObject(sa, resultType);
	}

	/**
	 * @param spaceRequestDTO
	 * @param resultType
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> List<T> queryList(SpaceRequestDTO spaceRequestDTO, Class<T> resultType) {
		return graphQLHttpClient().getList(resultType, getBody(spaceRequestDTO));
	}

	/**
	 *
	 * @param spaceRequestDTO
	 * @param resultType
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> T queryFirst(SpaceRequestDTO spaceRequestDTO, Class<T> resultType) {
		return graphQLHttpClient().getOne(resultType, getBody(spaceRequestDTO));
	}

	/**
	 *
	 * @param whereWrapper
	 * @param resultType
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> T whereFirst(WhereWrapper whereWrapper, Class<T> resultType) {
		WhereComposite whereComposite = buildWhereComposite(whereWrapper);
		return graphQLHttpClient().getOne(resultType, getBody(whereComposite));
	}

	/**
	 * @param whereWrapper
	 * @return
	 */
	@Override
	public Integer whereCount(WhereWrapper whereWrapper) {
		return whereAll(whereWrapper, Integer.class);
	}

	private <T> T singleAttribute(WhereWrapper whereWrapper, Class<T> resultType) {
		WhereComposite whereComposite = buildWhereComposite(whereWrapper);
		Map map = graphQLHttpClient().getOne(Map.class, getBody(whereComposite));
		if (map == null || map.isEmpty()) {
			return null;
		}
		String sa = String.valueOf(map.values().stream().findFirst().get());
		if (StringUtils.isBlank(sa)) {
			return null;
		}
		return InnerJSONUtils.parseObject(sa, resultType);
	}

	/**
	 * @param whereWrapper
	 * @param resultType
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> T whereAll(WhereWrapper whereWrapper, Class<T> resultType) {
		if (resultType == Boolean.class
				|| resultType == Short.class
				|| resultType == Integer.class
				|| resultType == Long.class
				|| resultType == Double.class
				|| resultType == Date.class
		) {
			return singleAttribute(whereWrapper, resultType);
		}
		WhereComposite whereComposite = buildWhereComposite(whereWrapper);
		return graphQLHttpClient().get(resultType, getBody(whereComposite));
	}

	/**
	 * 
	 * @param whereWrapper
	 * @param resultType
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> List<T> whereList(WhereWrapper whereWrapper, Class<T> resultType) {
		WhereComposite whereComposite = buildWhereComposite(whereWrapper);
		return graphQLHttpClient().getList(resultType, getBody(whereComposite));
	}
}
