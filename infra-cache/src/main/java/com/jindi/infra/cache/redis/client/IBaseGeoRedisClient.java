package com.jindi.infra.cache.redis.client;


import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.util.List;
import java.util.Map;

public interface IBaseGeoRedisClient<K, V>  {
    Long ggeoAdd(K key, Point point, V member);
    Long ggeoAdd(K key, RedisGeoCommands.GeoLocation<V> location);
    Long ggeoAdd(K key, Map<V, Point> memberCoordinateMap);
    Long ggeoAdd(K key, Iterable<RedisGeoCommands.GeoLocation<V>> geoLocations);
    Distance ggeoDist(K key, V member1, V member2);
    Distance ggeoDist(K key, V member1, V member2, Metric metric);
    List<String> ggeoHash(K key, V... members);
    List<Point> ggeoPos(K key, V... members);
    GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadius(K key, Circle within);
    GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadius(K key, Circle within, RedisGeoCommands.GeoRadiusCommandArgs args);
    GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadiusByMember(K key, V member, double radius);
    GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadiusByMember(K key, V member, Distance distance);
    GeoResults<RedisGeoCommands.GeoLocation<V>> ggeoRadiusByMember(
            K key, V member, Distance distance, RedisGeoCommands.GeoRadiusCommandArgs args);
    Long ggeoRemove(K key, V... members);
    Long gadd(K key, Point point, V member);
    Long gadd(K key, RedisGeoCommands.GeoLocation<V> location);
    Long gadd(K key, Map<V, Point> memberCoordinateMap);
    Long gadd(K key, Iterable<RedisGeoCommands.GeoLocation<V>> geoLocations);
    Distance gdistance(K key, V member1, V member2);
    Distance gdistance(K key, V member1, V member2, Metric metric);
    List<String> ghash(K key, V... members);
    List<Point> gposition(K key, V... members);
    GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(K key, Circle within);
    GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(K key, Circle within, RedisGeoCommands.GeoRadiusCommandArgs args);
    GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(K key, V member, double radius);
    GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(K key, V member, Distance distance);
    GeoResults<RedisGeoCommands.GeoLocation<V>> gradius(K key, V member, Distance distance, RedisGeoCommands.GeoRadiusCommandArgs args);
    Long gremove(K key, V... members);
}
