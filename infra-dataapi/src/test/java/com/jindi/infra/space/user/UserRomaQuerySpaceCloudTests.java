package com.jindi.infra.space.user;

import com.jindi.infra.common.util.InnerJSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest(classes = UserConfig.class)
@Slf4j
class UserRomaQuerySpaceCloudTests {

    @Autowired
    private UserRomaQuerySpaceCloud userRomaQuerySpaceCloud;

    @Test
    void testQueryCount() {
        int count = userRomaQuerySpaceCloud.queryCount();
        System.out.println("testQueryCount count = " + count);
    }

    @Test
    void testQueryList() {
        List<User> userList = userRomaQuerySpaceCloud.queryList();
        System.out.println("testQueryList userList = " + InnerJSONUtils.toJSONString(userList));
    }

    @Test
    void testQueryFirst() {
        User user = userRomaQuerySpaceCloud.queryFirst();
        System.out.println("testQueryFirst user = " + InnerJSONUtils.toJSONString(user));
    }

    /**
     * <pre>
     *     {
     *   "default": [
     *     {
     *       "sex": "2021-07-19 11:00:27",
     *       "name": "zhangsan",
     *       "id": 1,
     *       "age": "卫通大厦"
     *     },
     *     {
     *       "sex": "2021-07-19 11:00:38",
     *       "name": "lisi",
     *       "id": 2,
     *       "age": "卫通大厦"
     *     }
     *   ]
     * }
     * </pre>
     */
    @Test
    void testQueryAll() {
        Map<String, List<Map<Object, Object>>> map = userRomaQuerySpaceCloud.queryAll();
        System.out.println("testQueryAll map = " + InnerJSONUtils.toJSONString(map));
    }

    @Test
    void testWhereFirst() {
        User user = userRomaQuerySpaceCloud.whereFirst();
        System.out.println("testWhereFirst user = " + InnerJSONUtils.toJSONString(user));
    }

    @Test
    void testWhereAll() {
        Map<String, List<Map<Object, Object>>> map = userRomaQuerySpaceCloud.whereAll();
        System.out.println("testWhereAll map = " + InnerJSONUtils.toJSONString(map));
    }

    @Test
    void testWhereList() {
        List<User> userList = userRomaQuerySpaceCloud.whereList();
        System.out.println("testWhereList userList = " + InnerJSONUtils.toJSONString(userList));
    }

    @Test
    void testWhereCount() {
        int count = userRomaQuerySpaceCloud.whereCount();
        System.out.println("testWhereCount count = " + count);
    }
}
