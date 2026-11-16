package com.cinematography.cust.util;

public enum AuthUtilsEnum {
	DATE_TIME_FORMAT(Constants.DATE_TIME_FORMAT),
	ACTIVE(Constants.ACTIVE);

	private final String description;

	AuthUtilsEnum(String desc) {
		this.description = desc;
	}

	public String description() {
		return this.description;
	}

	public static class Constants {
		public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
		public static final String UTC_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss[.SSS]";
		public static final String DATE_FORMAT = "yyyy-MM-dd";
		public static final String ACTIVE = "ACTIVE";

		private Constants() {
		}
	}
}
