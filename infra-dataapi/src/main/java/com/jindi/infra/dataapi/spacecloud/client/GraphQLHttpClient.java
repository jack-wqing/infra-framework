package com.jindi.infra.dataapi.spacecloud.client;

import java.util.List;

import com.jindi.infra.dataapi.spacecloud.constant.GraphQLTypeEnums;

public interface GraphQLHttpClient {

    /**
     * 获取实体列表
     *
     * @param responseType
     * @param body
     * @param <T>
     * @return
     */
    <T> List<T> getList(Class<T> responseType, Object body);

    /**
     * 获取一个实体
     *
     * @param responseType
     * @param body
     * @param <T>
     * @return
     */
    <T> T getOne(Class<T> responseType, Object body);

    /**
     * 获取完整的实体
     *
     * @param responseType
     * @param body
     * @param <T>
     * @return
     */
    <T> T get(Class<T> responseType, Object body);

    /**
     * 获取实体的字符串内容
     *
     * @param body
     * @return
     */
    String getRaw(Object body);

    /**
     * GraphQL中间件 类型
     *
     * @return
     */
    GraphQLTypeEnums getGraphQLTypeEnums();

    String matchUrlPrefix(String project, String service, String endpoint);
}
