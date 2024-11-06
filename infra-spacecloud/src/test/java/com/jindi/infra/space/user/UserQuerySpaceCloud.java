package com.jindi.infra.space.user;

import com.jindi.infra.space.QuerySpaceCloud;
import com.jindi.infra.space.annotation.Endpoint;
import com.jindi.infra.space.annotation.SpaceCloud;
import com.jindi.infra.space.param.SpaceRequestDTO;
import com.jindi.infra.space.wrapper.WhereWrapper;

import java.util.List;
import java.util.Map;

/**
 * http://sc-test.jindidata.com/mission-control/projects/cb/remote-services/user
 */
@SpaceCloud(project = "cb", service = "user")
public class UserQuerySpaceCloud extends QuerySpaceCloud {

    private static final String TYPE = "SpaceCloud";

    /**
     * <pre>
     *     {{"{"}}
     *   user @demo {{"{"}}
     *     id @aggregate(op: "count")
     *   {{"}"}}
     * {{"}"}}
     * </pre>
     *
     * @return
     */
    @Endpoint(value = "queryCount", type = TYPE)
    public Integer queryCount() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        return queryCount(spaceRequestDTO);
    }

    /**
     * <pre>
     * {
     *   user @demo {
     *     id
     *     username
     *     address
     *     create_time
     *   }
     * }
     * </pre>
     *
     * @return
     */
    @Endpoint(value = "queryAll", type = TYPE)
    public Map<String, List<Map<Object, Object>>> queryAll() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        return queryAll(spaceRequestDTO, Map.class);
    }

    /**
     * <pre>
     * {
     *   user @demo {
     *     id
     *     username
     *     address
     *     create_time
     *   }
     * }
     * </pre>
     *
     * @return
     */
    @Endpoint(value = "queryList", type = TYPE)
    public List<User> queryList() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        return queryList(spaceRequestDTO, User.class);
    }

    /**
     * <pre>
     * {
     *   user(
     *     where: {address: {_eq: {{.args.address}}}}
     *   ) @demo {
     *     id
     *     username
     *     address
     *     create_time
     *   }
     * }
     * </pre>
     *
     * @return
     */
    @Endpoint(value = "queryFirst", type = TYPE)
    public User queryFirst() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        spaceRequestDTO.put("address", "1");
        return queryFirst(spaceRequestDTO, User.class);
    }

    /**
     * <pre>
     * {
     *   user(
     *     where: {{ whereClause (index . "args" "composite") "address" "username" }}
     *   ) @demo {
     * 		id
     *     username
     *     address
     *     create_time
     *   }
     * }
     * </pre>
     *
     * @return
     */
    @Endpoint(value = "whereFirst", type = TYPE)
    public User whereFirst() {
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.eq(User::getAddress, "1");
        return whereFirst(whereWrapper, User.class);
    }

    /**
     * <pre>
     * {
     *   user(
     *     where: {{ whereClause (index . "args" "composite") "address" "username" }}
     *   ) @demo {
     *    id @aggregate(op: "count")
     *   }
     * }
     * </pre>
     *
     * @return
     */
    @Endpoint(value = "whereCount", type = TYPE)
    public Integer whereCount() {
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.eq(User::getAddress, "1");
        return whereCount(whereWrapper);
    }

    /**
     * <pre>
     * {
     *   user(
     *     where: {{ whereClause (index . "args" "composite") "address" "username" }}
     *   ) @demo {
     *     id
     *     username
     *     address
     *     create_time
     *   }
     * }
     * </pre>
     *
     * @return
     */
    @Endpoint(value = "whereAll", type = TYPE)
    public Map<String, List<Map<Object, Object>>> whereAll() {
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.eq(User::getAddress, "1");
        return whereAll(whereWrapper, Map.class);
    }

    /**
     * <pre>
     * {
     *          user(
     *            where: {{ whereClause (index . "args" "composite") "address" "username" }}
     *          ) @demo {
     *            id
     *            username
     *            address
     *            create_time
     *          }
     *        }
     * </pre>
     *
     * @return
     */
    @Endpoint(value = "whereList", type = TYPE)
    public List<User> whereList() {
        WhereWrapper whereWrapper = new WhereWrapper();
        whereWrapper.eq(User::getAddress, "1");
        return whereList(whereWrapper, User.class);
    }

    @Endpoint(value = "querySQLList", type = TYPE)
    public List<User> querySQLList() {
        SpaceRequestDTO spaceRequestDTO = new SpaceRequestDTO();
        return queryList(spaceRequestDTO, User.class);
    }

}
