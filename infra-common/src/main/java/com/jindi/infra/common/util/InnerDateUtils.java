package com.jindi.infra.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.common.constant.DateTimeFormat;

public class InnerDateUtils {

	private static final Map<DateTimeFormat, DateTimeFormatter> cached = new ConcurrentHashMap<>();

	/**
	 * 当前时间的字符串形式
	 *
	 * @return
	 */
	public static String nowDateTimeString() {
		return get(DateTimeFormat.YYYY_MM_DD_HH_MM_SS).format(LocalDateTime.now());
	}

	/**
	 * 当前日期的字符串形式
	 */
	public static String nowDateString() {
		return get(DateTimeFormat.YYYY_MM_DD).format(LocalDate.now());
	}

	/**
	 * 当前日期+小时的字符串形式
	 */
	public static String nowDateHHString() {
		return get(DateTimeFormat.YYYY_MM_DD_HH).format(LocalDateTime.now());
	}

	/**
	 * 日期转字符串
	 *
	 * @param date
	 * @param dateTimeFormat
	 * @return
	 */
	public static String dateTimeString(Date date, DateTimeFormat dateTimeFormat) {
		if (date == null) {
			return null;
		}
		Instant instant = date.toInstant();
		ZoneId zone = ZoneId.systemDefault();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		if (Objects.equals(DateTimeFormat.DATE, dateTimeFormat.getType())) {
			LocalDate localDate = localDateTime.toLocalDate();
			return get(dateTimeFormat).format(localDate);
		}
		return get(dateTimeFormat).format(localDateTime);
	}

	/**
	 * 字符串转日期对象
	 *
	 * @param str
	 * @param dateTimeFormat
	 * @return
	 */
	public static Date parse(String str, DateTimeFormat dateTimeFormat) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		ZoneId zone = ZoneId.systemDefault();
		if (Objects.equals(DateTimeFormat.DATE, dateTimeFormat.getType())) {
			return Date.from(LocalDate.parse(str, get(dateTimeFormat)).atStartOfDay(zone).toInstant());
		}
		return Date.from(LocalDateTime.parse(str, get(dateTimeFormat)).atZone(zone).toInstant());
	}

	/**
	 * @param dateTimeFormat
	 * @return
	 */
	private static DateTimeFormatter get(DateTimeFormat dateTimeFormat) {
		if (cached.containsKey(dateTimeFormat)) {
			return cached.get(dateTimeFormat);
		}
		cached.put(dateTimeFormat, DateTimeFormatter.ofPattern(dateTimeFormat.getFormat()));
		return cached.get(dateTimeFormat);
	}

	public static Date addYears(Date date, int amount) {
		return add(date, 1, amount);
	}

	public static Date addMonths(Date date, int amount) {
		return add(date, 2, amount);
	}

	public static Date addWeeks(Date date, int amount) {
		return add(date, 3, amount);
	}

	public static Date addDays(Date date, int amount) {
		return add(date, 5, amount);
	}

	public static Date addHours(Date date, int amount) {
		return add(date, 11, amount);
	}

	public static Date addMinutes(Date date, int amount) {
		return add(date, 12, amount);
	}

	public static Date addSeconds(Date date, int amount) {
		return add(date, 13, amount);
	}

	public static Date addMilliseconds(Date date, int amount) {
		return add(date, 14, amount);
	}

	public static Date setYears(Date date, int amount) {
		return set(date, 1, amount);
	}

	public static Date setMonths(Date date, int amount) {
		return set(date, 2, amount);
	}

	public static Date setDays(Date date, int amount) {
		return set(date, 5, amount);
	}

	public static Date setHours(Date date, int amount) {
		return set(date, 11, amount);
	}

	public static Date setMinutes(Date date, int amount) {
		return set(date, 12, amount);
	}

	public static Date setSeconds(Date date, int amount) {
		return set(date, 13, amount);
	}

	public static Date setMilliseconds(Date date, int amount) {
		return set(date, 14, amount);
	}

	private static Date add(Date date, int calendarField, int amount) {
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(calendarField, amount);
		return c.getTime();
	}

	private static Date set(Date date, int calendarField, int amount) {
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setLenient(false);
		c.setTime(date);
		c.set(calendarField, amount);
		return c.getTime();
	}
}
