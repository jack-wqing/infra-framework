package com.jindi.infra.grpc.util;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.jindi.infra.core.constants.GrpcConsts;
import com.jindi.infra.core.exception.GrpcCodeException;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcUtils {

	public static Boolean isGrpcTimeoutException(Throwable e) {
		if (e instanceof StatusRuntimeException) {
			StatusRuntimeException statusRuntimeException = (StatusRuntimeException) e;
			if (Status.DEADLINE_EXCEEDED.getCode().equals(statusRuntimeException.getStatus().getCode()) ||
					Status.CANCELLED.getCode().equals(statusRuntimeException.getStatus().getCode())) {
				return true;
			}
		}
		return false;
	}

	public static Throwable parseCause(Throwable e) {
		if (e instanceof ExecutionException) {
			ExecutionException executionException = (ExecutionException) e;
			if (executionException.getCause() == null) {
				return e;
			}
			e = executionException.getCause();
		}
		if (e instanceof StatusRuntimeException) {
			return parseStatusRuntimeException(e);
		}
		return e;
	}

	public static Throwable parseStatusRuntimeException(Throwable e) {
		StatusRuntimeException statusRuntimeException = (StatusRuntimeException) e;
		if (!Objects.equals(Status.Code.UNKNOWN, statusRuntimeException.getStatus().getCode())) {
			return e;
		}
		Metadata metadata = statusRuntimeException.getTrailers();
		if (metadata == null) {
			return e;
		}
		if (!metadata.containsKey(GrpcConsts.EXCEPTION_STACK_TRACE_KEY)) {
			return e;
		}
		String stackTrace = metadata.get(GrpcConsts.EXCEPTION_STACK_TRACE_KEY);
		String code = metadata.get(GrpcConsts.EXCEPTION_CODE_KEY);
		if (StringUtils.isBlank(stackTrace)) {
			return e;
		}
		String[] rows = StringUtils.split(stackTrace, "\t");
		if (ArrayUtils.isEmpty(rows)) {
			return e;
		}
		String firstRow = rows[0];
		if (StringUtils.isBlank(firstRow)) {
			return e;
		}
		int index = StringUtils.indexOf(firstRow, ":");
		Throwable throwable;
		if (index < 0) {
			throwable = createCause(firstRow.trim(), StringUtils.substring(stackTrace, firstRow.trim().length()), code);
		} else {
			throwable = createCause(StringUtils.substring(stackTrace, 0, index).trim(),
					StringUtils.substring(stackTrace, index + 1), code);
		}
		if (throwable != null) {
			return throwable;
		}
		return e;
	}

	public static Throwable createCause(String typeName, String message, String code) {
		Class clazz = null;
		try {
			clazz = ClassUtils.forName(typeName);
		} catch (Throwable e) {
			log.error("没有找到{}异常类", typeName, e);
			return null;
		}
		if (GrpcCodeException.class.isAssignableFrom(clazz) && StringUtils.isNotBlank(code)) {
			try {
				Constructor constructor = clazz.getConstructor(Integer.class, String.class);
				return (Throwable) constructor.newInstance(Integer.parseInt(code), message);
			} catch (Throwable e) {
			}
		}
		try {
			Constructor constructor = clazz.getConstructor(String.class);
			return (Throwable) constructor.newInstance(message);
		} catch (Throwable e) {
		}
		try {
			Constructor constructor = clazz.getConstructor();
			return (Throwable) constructor.newInstance();
		} catch (Throwable e) {
		}
		log.error("类: {} 没有匹配的异常类构造器; 目前仅支持 存在 new 异常类() 或者 new 异常类('字符串') 构造器的异常类", typeName);
		return null;
	}
}
