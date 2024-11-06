package com.jindi.infra.space.user;

import com.jindi.infra.common.util.InnerJSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * http://sc-test.jindidata.com/mission-control/projects/cb/remote-services/user
 */
@SpringBootTest(classes = UserConfig.class)
@Slf4j
class UserQuerySpaceCloudTests {

    @Autowired
    private UserQuerySpaceCloud userQuerySpaceCloud;

    @Test
    void testQueryCount() {
        int count = userQuerySpaceCloud.queryCount();
        System.out.println("testQueryCount count = " + count);
    }

    @Test
    void testQueryList() {
        List<User> userList = userQuerySpaceCloud.queryList();
        System.out.println("testQueryList userList = " + InnerJSONUtils.toJSONString(userList));
    }

    @Test
    void testQueryFirst() {
        User user = userQuerySpaceCloud.queryFirst();
        System.out.println("testQueryFirst user = " + InnerJSONUtils.toJSONString(user));
    }

    /**
     * <pre>
     *     {
     *   "user": [
     *     {
     *       "address": "卫通大厦",
     *       "create_time": "2021-07-19T03:00:27Z",
     *       "id": 1,
     *       "username": "zhangsan"
     *     },
     *     {
     *       "address": "卫通大厦",
     *       "create_time": "2021-07-19T03:00:38Z",
     *       "id": 2,
     *       "username": "lisi"
     *     }
     *   ]
     * }
     * </pre>
     */
    @Test
    void testQueryAll() {
        Map<String, List<Map<Object, Object>>> map = userQuerySpaceCloud.queryAll();
        System.out.println("testQueryAll map = " + InnerJSONUtils.toJSONString(map));
    }

    @Test
    void testWhereFirst() {
        User user = userQuerySpaceCloud.whereFirst();
        System.out.println("testWhereFirst user = " + InnerJSONUtils.toJSONString(user));
    }

    @Test
    void testWhereAll() {
        Map<String, List<Map<Object, Object>>> map = userQuerySpaceCloud.whereAll();
        System.out.println("testWhereAll map = " + InnerJSONUtils.toJSONString(map));
    }

    @Test
    void testWhereList() {
        List<User> userList = userQuerySpaceCloud.whereList();
        System.out.println("testWhereList userList = " + InnerJSONUtils.toJSONString(userList));
    }

    @Test
    void testWhereCount() {
        int count = userQuerySpaceCloud.whereCount();
        System.out.println("testWhereCount count = " + count);
    }

    @Test
    void testQuerySQLList() {
        List<User> userList = userQuerySpaceCloud.querySQLList();
        System.out.println("testQuerySQLList userList = " + InnerJSONUtils.toJSONString(userList));
    }
}
