package com.jindi.infra.dataapi.spacecloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jindi.infra.dataapi.spacecloud.client.GraphQLHttpClient;
import com.jindi.infra.dataapi.spacecloud.client.GraphQLHttpClients;
import com.jindi.infra.dataapi.spacecloud.constant.GraphQLTypeEnums;
import com.jindi.infra.dataapi.spacecloud.constant.SpaceCloudConsts;
import com.jindi.infra.dataapi.spacecloud.param.SpaceCloudParam;
import com.jindi.infra.dataapi.spacecloud.param.SpaceRequestDTO;
import com.jindi.infra.dataapi.spacecloud.param.WhereComposite;
import com.jindi.infra.dataapi.spacecloud.wrapper.CompositeItem;
import com.jindi.infra.dataapi.spacecloud.wrapper.WhereWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseQuerySpaceCloud {

	private static final String COMPOSITE = "composite";

	@Autowired
	private GraphQLHttpClients graphQLHttpClients;

	/**
	 * 构建where查询请求体
	 *
	 * @param whereWrapper
	 * @return
	 */
	protected WhereComposite buildWhereComposite(WhereWrapper whereWrapper) {
		if (whereWrapper == null) {
			return new WhereComposite(new HashMap<>(1));
		}
		List<CompositeItem> compositeItemList = whereWrapper.getCompositeItemList();
		Map<String, Object> options = whereWrapper.getOptions();
		return new WhereComposite(buildWhereComposite(compositeItemList, options));
	}

	private Map<String, Object> buildWhereComposite(List<CompositeItem> compositeItemList,
													Map<String, Object> options) {
		if (options == null) {
			options = new HashMap<>();
		}
		Map<String, Object> params = new HashMap<>();
		if (!CollectionUtils.isEmpty(compositeItemList)) {
			params.put(COMPOSITE, compositeItemList);
			SpaceCloudParam spaceCloudParam = SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.get();
			if (spaceCloudParam == null) {
				throw new SpaceCloudException("SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL 上下文变量不存在");
			}
			/**
			 * 兼容space-cloud mybatis模版 不支持 QuerySpaceCloud.where* 方法
			 */
			if (Objects.equals(spaceCloudParam.getType(), GraphQLTypeEnums.SPACE_CLOUD.getName())) {
				for (CompositeItem compositeItem : compositeItemList) {
					if (options.containsKey(compositeItem.getField())) {
						continue;
					}
					if (compositeItem.getField() == null) {
						continue;
					}
					options.put(compositeItem.getField(), compositeItem.getValue());
				}
			}
		}
		params.putAll(options);
		return params;
	}


	protected GraphQLHttpClient graphQLHttpClient() {
		return graphQLHttpClients.graphQLHttpClient();
	}

	protected Object getBody(SpaceRequestDTO spaceRequestDTO) {
		if (spaceRequestDTO == null) {
			spaceRequestDTO = new SpaceRequestDTO();
		}
		SpaceCloudParam spaceCloudParam = SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.get();
		if (spaceCloudParam == null) {
			throw new SpaceCloudException("SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL 上下文变量不存在");
		}
		if (Objects.equals(spaceCloudParam.getType(), GraphQLTypeEnums.ROMA.getName())) {
			return spaceRequestDTO.getRomaParams();
		}
		return spaceRequestDTO;
	}

	protected Object getBody(WhereComposite whereComposite) {
		SpaceCloudParam spaceCloudParam = SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.get();
		if (spaceCloudParam == null) {
			throw new SpaceCloudException("SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL 上下文变量不存在");
		}
		if (Objects.equals(spaceCloudParam.getType(), GraphQLTypeEnums.ROMA.getName())) {
			return whereComposite.getRomaParams(false);
		}
		return whereComposite;
	}

}
