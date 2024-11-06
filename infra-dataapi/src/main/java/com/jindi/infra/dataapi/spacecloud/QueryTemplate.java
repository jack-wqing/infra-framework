package com.jindi.infra.dataapi.spacecloud;

import java.util.List;

import com.jindi.infra.dataapi.spacecloud.param.SpaceRequestDTO;
import com.jindi.infra.dataapi.spacecloud.wrapper.WhereWrapper;

public interface QueryTemplate {

    /**
     *
     * @param spaceRequestDTO
     * @return
     */
    Integer queryCount(SpaceRequestDTO spaceRequestDTO);

    /**
     *
     * @param spaceRequestDTO
     * @param resultType
     * @param <T>
     * @return
     */
    <T> T queryAll(SpaceRequestDTO spaceRequestDTO, Class<T> resultType);

    /**
     *
     * @param whereWrapper
     * @param resultType
     * @param <T>
     * @return
     */
    <T> T whereAll(WhereWrapper whereWrapper, Class<T> resultType);

    /**
     *
     * @param spaceRequestDTO
     * @param resultType
     * @param <T>
     * @return
     */
    <T> List<T> queryList(SpaceRequestDTO spaceRequestDTO, Class<T> resultType);

    /**
     *
     * @param whereWrapper
     * @param resultType
     * @param <T>
     * @return
     */
    <T> List<T> whereList(WhereWrapper whereWrapper, Class<T> resultType);

    /**
     *
     * @param spaceRequestDTO
     * @param resultType
     * @param <T>
     * @return
     */
    <T> T queryFirst(SpaceRequestDTO spaceRequestDTO, Class<T> resultType);

    /**
     *
     * @param whereWrapper
     * @param resultType
     * @param <T>
     * @return
     */
    <T> T whereFirst(WhereWrapper whereWrapper, Class<T> resultType);

    /**
     *
     * @param whereWrapper
     * @return
     */
    Integer whereCount(WhereWrapper whereWrapper);

}
