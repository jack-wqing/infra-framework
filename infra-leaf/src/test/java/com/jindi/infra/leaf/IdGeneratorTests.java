package com.jindi.infra.leaf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jindi.infra.leaf.config.LeafAutoConfiguration;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = LeafAutoConfiguration.class)
@Slf4j
public class IdGeneratorTests {

	@Autowired
	private IdGenerator idGenerator;

	@Test
	public void testGetId() {
		System.out.println("getSegmentId");
		Assertions.assertTrue(idGenerator.getSegmentId("leaf-segment-test") > 0, "Leaf ID should be greater than 0");
		System.out.println("getSnowflakeId");
		Assertions.assertTrue(idGenerator.getSnowflakeId("leaf-segment-test") > 0,
				"Snowflake ID should be greater than 0");
	}
}
