package com.jindi.infra.core.constants;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

public class LaneTagThreadLocal {

	public static final String LANE_TAG_KEY = "laneTag";
	public static final String LANE_TAG_KEY_LOWER_CASE = "lanetag";

	public static String getLaneTag() {
		String laneTag = getLaneTagByMdc();
		if (StringUtils.isBlank(laneTag)) {
			return null;
		}
		return laneTag;
	}

	private static String getLaneTagByMdc() {
		return MDC.get(LANE_TAG_KEY);
	}

	public static void saveLaneTag(String lanetag) {
		if (StringUtils.isNotBlank(lanetag)) {
			MDC.put(LANE_TAG_KEY, lanetag);
		}
	}

	public static void clearLaneTag() {
		MDC.remove(LANE_TAG_KEY);
	}
}
