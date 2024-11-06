package com.jindi.infra.common.constant;

public enum DateTimeFormat {
	YYYY_MM_DD(1, "yyyy-MM-dd"), YYYY_MM_DD_HH_MM_SS(2, "yyyy-MM-dd HH:mm:ss"), YYYY_MM_DD_HH(3, "yyyy-MM-dd-HH"), YYYY(
			1, "yyyy"), YYYYMM(1, "yyyyMM"), YYYYMMDD(1, "yyyyMMdd"), YYYYMMDDHH(2, "yyyyMMddHH"), YYYYMMDDHHMM(2,
					"yyyyMMddHHmm"), YYYYMMDDHHMMSS(2, "yyyyMMddHHmmss"), YYYY_UD_MM_UD_DD(1, "yyyy/MM/dd");

	public static final Integer DATE = 1;
	public static final Integer DATETIME = 2;
	private Integer type;
	private String format;

	DateTimeFormat(Integer type, String format) {
		this.type = type;
		this.format = format;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
