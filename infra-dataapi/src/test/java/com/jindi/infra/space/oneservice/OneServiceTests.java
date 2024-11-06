package com.jindi.infra.space.oneservice;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jindi.infra.common.util.InnerJSONUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * http://sc-test.jindidata.com/mission-control/projects/cb/remote-services/user
 */
@SpringBootTest(classes = OneServiceConfig.class)
@Slf4j
class OneServiceTests {

    @Autowired
    private OneServiceQuery oneServiceQuery;

    @Test
    void testQueryList() {
        List<String> test = oneServiceQuery.queryList();
        System.out.println("testQueryList: " + InnerJSONUtils.toJSONString(test));
    }

}
