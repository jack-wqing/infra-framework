package com.jindi.infra.cache.redis.tycredis;


import com.jindi.infra.cache.redis.client.TycRedisClient;
import com.jindi.infra.cache.redis.key.Key;
import com.jindi.infra.cache.redis.key.KeyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TycRedisClientTestsConfig.class})
@TestPropertySource("classpath:redis-test.properties")
@Slf4j
public class TycRedisClientTests {

    @Resource(name = "testClient")
    private TycRedisClient<String> tycRedisClient;

    private static final KeyBuilder VALUE_KEY_BUILDER = KeyBuilder.init("infra_sdk_value_check", "method");

    @BeforeAll
    public void setUp() {
        log.info("TycRedisClient测试用例开始");
    }

    /**
     * Value类型
     */

    @Test
    public void testSet() throws InterruptedException {
        log.info("testSet开始");

        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue.set"), "testValue.set-value", Duration.ofSeconds(10L));
        Assert.assertEquals("testValue.set-value", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue.set")));
        System.out.println(tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue.set")));
        Long expire1 = tycRedisClient.getRedisTemplate().getExpire(VALUE_KEY_BUILDER.build("testValue.set").getKey());
        Thread.sleep(2000L);
        Long expire2 = tycRedisClient.getRedisTemplate().getExpire(VALUE_KEY_BUILDER.build("testValue.set").getKey());
        Assert.assertTrue(expire1 != null && expire1 > 0L && expire2 != null && expire2 > 0L && expire1 > expire2);
    }

    @Test
    public void testSet2() {
        String method = "set2";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value");
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }

    @Test
    public void testSet3() {
        String method = "set3";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10);
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }

    @Test
    public void testSet4() {
        String method = "set4";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS);
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }


    @Test
    public void testSetIfPresent() {
        Assert.assertTrue(tycRedisClient.setIfPresent(VALUE_KEY_BUILDER.build("testValue.set"), "testValue.set-value2", Duration.ofSeconds(10L)));
        Assert.assertEquals("testValue.set-value2", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue.set")));
    }

    @Test
    public void testSetIfAbsent() {
        Assert.assertTrue(tycRedisClient.setIfAbsent(VALUE_KEY_BUILDER.build("testValue.setIfAbsent"), "testValue.set-value2", Duration.ofSeconds(10L)));
        Assert.assertEquals("testValue.set-value2", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue.setIfAbsent")));
    }

    @Test
    public void testSetIfPresent2() {
        String method = "setIfPresent2";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS);
        Assert.assertTrue(tycRedisClient.setIfPresent(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "valueNew", 10, TimeUnit.SECONDS));
        Assert.assertEquals("testValue." + method + "valueNew", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }

    @Test
    public void testSetIfPresent3() {
        String method = "setIfPresent3";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS);
        Assert.assertTrue(tycRedisClient.setIfPresent(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "valueNew"));
        Assert.assertEquals("testValue." + method + "valueNew", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }

    @Test
    public void testSetIfAbsent2() {
        String method = "setIfAbsent2";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS);
        Assert.assertFalse(tycRedisClient.setIfAbsent(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "valueNew", 10, TimeUnit.SECONDS));;
    }

    @Test
    public void testSetIfAbsent3() {
        String method = "setIfAbsent3";
        Assert.assertTrue(tycRedisClient.setIfAbsent(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS));
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }

    @Test
    public void testSetIfAbsent4() {
        String method = "setIfAbsent4";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS);
        Assert.assertFalse(tycRedisClient.setIfAbsent(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "valueNew", 10, TimeUnit.SECONDS));
    }

    @Test
    public void testSetIfAbsent5() {
        String method = "setIfAbsent5";
        Assert.assertTrue(tycRedisClient.setIfAbsent(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS));
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }

    @Test
    public void testMultiSet1() {
        String method = "multiSet1";
        Map<Key, String> map = new HashMap<>();
        map.put(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value");
        tycRedisClient.multiSet(map);
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }

    @Test
    public void testMultiSetIfAbsent() {
        String method = "multiSetIfAbsent" + System.currentTimeMillis();
        Map<Key, String> map2 = new HashMap<>();
        map2.put(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value");
        Assert.assertTrue(tycRedisClient.multiSetIfAbsent(map2));
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
        tycRedisClient.delete(VALUE_KEY_BUILDER.build("testValue." + method));
    }

    @Test
    public void testGetAndSet() {
        String method = "getAndSet";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS);
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.getAndSet(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "valueNew"));
        Assert.assertEquals("testValue." + method + "valueNew", tycRedisClient.get(VALUE_KEY_BUILDER.build("testValue." + method)));
    }

    @Test
    public void testMultiGet() {
        String method = "multiSet";
        tycRedisClient.set(VALUE_KEY_BUILDER.build("testValue." + method), "testValue." + method + "value", 10, TimeUnit.SECONDS);
        List<Key> list = new ArrayList<>();
        list.add(VALUE_KEY_BUILDER.build("testValue." + method));
        Assert.assertEquals("testValue." + method + "value", tycRedisClient.multiGet(list).get(0));
    }

    @Test
    public void testIncrement() {
        String method = "increment";
        tycRedisClient.initNumber(VALUE_KEY_BUILDER.build("testValue." + method), 110);
        Assert.assertTrue(111 == tycRedisClient.increment(VALUE_KEY_BUILDER.build("testValue." + method)));
        Assert.assertTrue(113 == tycRedisClient.increment(VALUE_KEY_BUILDER.build("testValue." + method), 2));
        tycRedisClient.delete(VALUE_KEY_BUILDER.build("testValue." + method));
    }

    @Test
    public void testMultiSetExpire() {
        String method = "MultiSetExpire";
        Map<Key, String> map = new HashMap<>();
        map.put(VALUE_KEY_BUILDER.build("abc"), "abc");
        map.put(VALUE_KEY_BUILDER.build("def"), "def");
        tycRedisClient.multiSetExpire(map, 100000);
        List<String> stringList = tycRedisClient.multiGet(map.keySet());
        Assert.assertTrue(stringList.size() == 2 && stringList.stream().anyMatch(v->v.contains("abc")));
    }

    @Test
    public void testSetOpts() {
        String method = "sadd";
        Key key = VALUE_KEY_BUILDER.build("testSet." + method);
        tycRedisClient.sadd(key, new String[]{"value1", "value2", "value3", "value4"});

        String spopResult = tycRedisClient.spop(key);
        Assert.assertTrue(spopResult.contains("value"));

        List<String> spopList = tycRedisClient.spop(key, 1);
        Assert.assertTrue(spopList.size() == 1);

        Set<String> smembers = tycRedisClient.smembers(key);
        Assert.assertTrue(smembers.size() == 2);

        List<String> srandomMembers = tycRedisClient.srandomMembers(key, 1);
        Assert.assertTrue(srandomMembers.size() == 1);
    }

    @Test
    public void testListOpts() {
        String method = "list";
        Key key = VALUE_KEY_BUILDER.build("testList." + method);
        tycRedisClient.delete(key);
        tycRedisClient.lleftPush(key, "value2");
        tycRedisClient.lleftPush(key, "value1");
        tycRedisClient.lrightPush(key, "value3");
        tycRedisClient.lrightPush(key, "value4");

        List<String> rangeList = tycRedisClient.lrange(key, 1, 2);
        Assert.assertTrue(rangeList.stream().allMatch(v->v.contains("value")) && rangeList.size() == 2);

        Assert.assertTrue(tycRedisClient.lindex(key, 2).contains("value"));

        String leftValue = tycRedisClient.lleftPop(key);
        Assert.assertEquals(leftValue, "value1");

        String rightValue = tycRedisClient.lrightPop(key);
        Assert.assertEquals(rightValue, "value4");

        tycRedisClient.lrightPushAll(key, "value5", "value6");
        Assert.assertTrue(Long.valueOf(4L).equals(tycRedisClient.lsize(key)));
        tycRedisClient.delete(key);
    }

    @Test
    public void testHashOpts() {
        String method = "hash";
        Key key = VALUE_KEY_BUILDER.build("testHash." + method);
        tycRedisClient.hput(key, "type", "value1");
        Assert.assertTrue(tycRedisClient.hhasKey(key, "type"));
        Assert.assertEquals(tycRedisClient.hget(key, "type"), "value1");

        Map<String, String> map = new HashMap<>();
        map.put("key2", "value2");
        map.put("key3", "value3");
        tycRedisClient.hputAll(key, map);
        List<String> values = tycRedisClient.hmultiGet(key, map.keySet());
        Assert.assertTrue(values.size() == 2 && values.stream().allMatch(v->v.contains("value")));

        List<String> hvalues = tycRedisClient.hvalues(key);
        Assert.assertTrue(hvalues.size() == 3 && hvalues.stream().allMatch(v->v.contains("value")));
    }

    @Test
    public void testZsetOpts() {
        String method = "zset";
        Key key = VALUE_KEY_BUILDER.build("testZset." + method);
        tycRedisClient.zadd(key, "value1", 1);
        tycRedisClient.zadd(key, "value2", 2);
        tycRedisClient.zadd(key, "value3", 3);
        Assert.assertTrue(tycRedisClient.zincrementScore(key, "value3", 1).equals(Double.valueOf("4")));
        Assert.assertTrue(tycRedisClient.zrank(key, "value3").equals(2L));
        Long zcount = tycRedisClient.zcount(key, 1, 3);
        Assert.assertTrue(zcount.equals(2L));
        Set<String> zrange = tycRedisClient.zrange(key, 0, 1);
        Assert.assertTrue(zrange.size() == 2 && zrange.containsAll(Lists.newArrayList("value2", "value1")));
    }


}
