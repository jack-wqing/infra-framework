package com.jindi.infra.core.constants;

public enum MethodType {

	/**
	 * One request message followed by one response message.
	 */
	UNARY,

	/**
	 * Zero or more request messages with one response message.
	 */
	CLIENT_STREAMING,

	/**
	 * One request message followed by zero or more response messages.
	 */
	SERVER_STREAMING,

	/**
	 * Zero or more request and response messages arbitrarily interleaved in time.
	 */
	BIDI_STREAMING,

	/**
	 * Cardinality and temporal relationships are not known. Implementations should
	 * not make buffering assumptions and should largely treat the same as
	 * {@link #BIDI_STREAMING}.
	 */
	UNKNOWN;
}
