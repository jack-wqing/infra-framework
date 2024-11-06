package com.jindi.infra.space.user;

import com.jindi.infra.dataapi.spacecloud.QuerySpaceCloud;
import com.jindi.infra.dataapi.spacecloud.annotation.Endpoint;
import com.jindi.infra.dataapi.spacecloud.annotation.SpaceCloud;
import com.jindi.infra.dataapi.spacecloud.param.SpaceRequestDTO;
import com.jindi.infra.dataapi.spacecloud.wrapper.WhereWrapper;

import java.util.List;
import java.util.Map;

@SpaceCloud(project = "cb", service = "user")
public class UserRomaQuerySpaceCloud extends QuerySpaceCloud {

    private static final String TYPE = "Roma";

    @Endpoint(value = "queryCount", type = TYPE)
    public Integer queryCount() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        return queryCount(spaceRequestDTO);
    }

    @Endpoint(value = "queryAll", type = TYPE)
    public Map<String, List<Map<Object, Object>>> queryAll() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        return queryAll(spaceRequestDTO, Map.class);
    }

    @Endpoint(value = "queryList", type = TYPE)
    public List<User> queryList() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        return queryList(spaceRequestDTO, User.class);
    }

    @Endpoint(value = "queryFirst", type = TYPE)
    public User queryFirst() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        spaceRequestDTO.put("address", "1");
        return queryFirst(spaceRequestDTO, User.class);
    }

    @Endpoint(value = "whereFirst", type = TYPE)
    public User whereFirst() {
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.eq(User::getAddress, "1");
        return whereFirst(whereWrapper, User.class);
    }

    @Endpoint(value = "whereCount", type = TYPE)
    public Integer whereCount() {
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.eq(User::getAddress, "1");
        return whereCount(whereWrapper);
    }

    @Endpoint(value = "whereAll", type = TYPE)
    public Map<String, List<Map<Object, Object>>> whereAll() {
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.eq(User::getAddress, "1");
        return whereAll(whereWrapper, Map.class);
    }

    @Endpoint(value = "whereList", type = TYPE)
    public List<User> whereList() {
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.eq(User::getAddress, "1");
        return whereList(whereWrapper, User.class);
    }
}
