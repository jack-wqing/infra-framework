package com.jindi.infra.space.client;

import com.jindi.infra.space.SpaceCloudException;
import com.jindi.infra.space.constant.SpaceCloudConsts;
import com.jindi.infra.space.param.SpaceCloudParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

public class GraphQLHttpClients {

    @Autowired
    private ObjectProvider<List<GraphQLHttpClient>> graphQLHttpClientObjectProvider;

    public GraphQLHttpClient graphQLHttpClient() {
        return graphQLHttpClient(null);
    }

    public GraphQLHttpClient graphQLHttpClient(SpaceCloudParam spaceCloudParam) {
        if (spaceCloudParam == null) {
            spaceCloudParam = SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.get();
        }
        if (spaceCloudParam == null) {
            throw new SpaceCloudException("SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL 上下文变量不存在");
        }
        List<GraphQLHttpClient> graphQLHttpClients = graphQLHttpClientObjectProvider.getIfAvailable();
        for (GraphQLHttpClient graphQLHttpClient : graphQLHttpClients) {
            if (Objects.equals(graphQLHttpClient.getGraphQLTypeEnums().getName(), spaceCloudParam.getType())) {
                return graphQLHttpClient;
            }
        }
        throw new SpaceCloudException(String.format("type = %s 不存在对应的 GraphQLHttpClient 客户端", spaceCloudParam.getType()));
    }
}
