package com.jindi.infra.grpc.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.grpc.client.GrpcClientProxy;
import com.jindi.infra.grpc.constant.ResponseCode;
import com.jindi.infra.grpc.model.ParamType;
import com.jindi.infra.grpc.model.Request;
import com.jindi.infra.grpc.model.Response;
import com.jindi.infra.grpc.server.GrpcServiceProxy;
import com.jindi.infra.grpc.util.ClassUtils;
import com.jindi.infra.grpc.util.MethodUtils;
import com.jindi.infra.grpc.util.SdkConfigUtils;

import io.grpc.BindableService;
import lombok.extern.slf4j.Slf4j;

// 可能业务实现
@Slf4j
@RequestMapping("grpc")
@RestController
public class GrpcProvider {

	@Autowired
	private GrpcServiceProxy grpcServiceProxy;
	@Autowired
	private GrpcClientProxy grpcClientProxy;

	/**
	 * http调用grpc方法的接口
	 *
	 * <p>
	 * curl -X POST -H 'content-type:application/json'
	 * 'http://localhost:8080/grpc/invoke?interface=&method=&paramTypes=' -d '[]'
	 *
	 * @param serviceClassString
	 * @param methodName
	 * @param paramTypeNames
	 * @param values
	 * @return
	 * @throws Throwable
	 */
	@PostMapping("invoke")
	public Response invoke(@RequestParam("interface") String serviceClassString,
			@RequestParam("method") String methodName, @RequestParam("paramTypes") String paramTypeNames,
			@RequestBody List<Object> values) throws Throwable {
		if (StringUtils.isAnyBlank(serviceClassString, methodName)) {
			return Response.builder().code(ResponseCode.NOT_FOUND.getCode())
					.message(ResponseCode.NOT_FOUND.getMessage()).build();
		}
		Class<?> serviceClass = ClassUtils.forName(serviceClassString);
		BindableService bindableService = grpcServiceProxy.getBindableServiceByServiceClass(serviceClass);
		if (bindableService == null) {
			return Response.builder().code(ResponseCode.NOT_FOUND.getCode())
					.message(ResponseCode.NOT_FOUND.getMessage()).build();
		}
		Object[] params = null;
		Method method;
		if (StringUtils.isBlank(paramTypeNames)) {
			try {
				method = serviceClass.getMethod(methodName);
			} catch (NoSuchMethodException e) {
				return Response.builder().code(ResponseCode.NOT_FOUND.getCode())
						.message(ResponseCode.NOT_FOUND.getMessage()).build();
			}
		} else {
			String[] ss = StringUtils.split(paramTypeNames, ",");
			Class<?>[] paramTypes = new Class<?>[ss.length];
			params = new Object[ss.length];
			if (values != null) {
				for (int i = 0; i < ss.length; i++) {
					paramTypes[i] = ClassUtils.forName(ss[i]);
					if (values.get(i) != null) {
						Message.Builder builder = newBuilder(paramTypes[i]);
						JsonFormat.merge(InnerJSONUtils.toJSONString(values.get(i)), builder);
						params[i] = builder.build();
					}
				}
			}
			try {
				method = serviceClass.getMethod(methodName, paramTypes);
			} catch (NoSuchMethodException e) {
				return Response.builder().code(ResponseCode.NOT_FOUND.getCode())
						.message(ResponseCode.NOT_FOUND.getMessage()).build();
			}
		}
		return invoke(bindableService, params, method);
	}

	private Response<Object> invoke(BindableService bindableService, Object[] params, Method method) {
		try {
			Message message = (Message) method.invoke(bindableService, params);
			return Response.builder()
					.data(message == null ? null : JSON.parseObject(JsonFormat.printToString(message)).getInnerMap())
					.code(ResponseCode.SUCCESS.getCode()).message(ResponseCode.SUCCESS.getMessage()).build();
		} catch (Throwable e) {
			log.error("", e);
			return Response.builder().code(ResponseCode.INVOKE_EXCEPTION.getCode())
					.message(String.format(ResponseCode.INVOKE_EXCEPTION.getMessage(), e.getMessage())).build();
		}
	}

	private Message.Builder newBuilder(Class<?> messageClass)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return (Message.Builder) ((messageClass.getMethod("newBuilder")).invoke(null));
	}

	/**
	 * 查看应用所有的服务方法
	 *
	 * <p>
	 * curl -X GET 'http://localhost:8080/grpc/services'
	 *
	 * @return
	 * @throws Throwable
	 */
	@GetMapping("services")
	public Response services() throws Throwable {
		List<Class<BindableService>> serviceClasses = grpcServiceProxy.getAllServiceClass();
		if (CollectionUtils.isEmpty(serviceClasses)) {
			return Response.builder().code(ResponseCode.NOT_FOUND.getCode())
					.message(ResponseCode.NOT_FOUND.getMessage()).build();
		}
		Map<String, List<Request>> serviceClassRequestMap = parseServiceClass(serviceClasses);
		if (CollectionUtils.isEmpty(serviceClassRequestMap)) {
			return Response.builder().code(ResponseCode.NOT_FOUND.getCode())
					.message(ResponseCode.NOT_FOUND.getMessage()).build();
		}
		return Response.builder().data(serviceClassRequestMap).code(ResponseCode.SUCCESS.getCode())
				.message(ResponseCode.SUCCESS.getMessage()).build();
	}

	/**
	 * 查看应用调用的其他服务
	 *
	 * <p>
	 * curl -X GET 'http://localhost:8080/grpc/calls'
	 *
	 * @return
	 * @throws Throwable
	 */
	@GetMapping("calls")
	public Response calls() throws Throwable {
		List<Class<BindableService>> serviceClasses = grpcClientProxy.getAllServiceClass();
		if (CollectionUtils.isEmpty(serviceClasses)) {
			return Response.builder().code(ResponseCode.NOT_FOUND.getCode())
					.message(ResponseCode.NOT_FOUND.getMessage()).build();
		}
		Map<String, List<Request>> serviceClassRequestMap = parseServiceClass(serviceClasses);
		if (CollectionUtils.isEmpty(serviceClassRequestMap)) {
			return Response.builder().code(ResponseCode.NOT_FOUND.getCode())
					.message(ResponseCode.NOT_FOUND.getMessage()).build();
		}
		return Response.builder().data(serviceClassRequestMap).code(ResponseCode.SUCCESS.getCode())
				.message(ResponseCode.SUCCESS.getMessage()).build();
	}

	private Map<String, List<Request>> parseServiceClass(List<Class<BindableService>> serviceClasses) throws Exception {
		Map<String, List<Request>> serviceClassRequestMap = new HashMap<>();
		for (Class<BindableService> serviceClass : serviceClasses) {
			List<Request> requests = parseServiceClass(serviceClass);
			if (CollectionUtils.isEmpty(requests)) {
				continue;
			}
			serviceClassRequestMap.put(serviceClass.getTypeName(), parseServiceClass(serviceClass));
		}
		return serviceClassRequestMap;
	}

	private List<Request> parseServiceClass(Class<BindableService> serviceClass) throws Exception {
		Method[] methods = serviceClass.getDeclaredMethods();
		if (ArrayUtils.isEmpty(methods)) {
			return Collections.emptyList();
		}
		List<Request> requests = new ArrayList<>();
		for (Method method : methods) {
			if (!MethodUtils.isAsyncMethod(method) && MethodUtils.isRemoteMethod(method)) {
				requests.add(createRequest(serviceClass, method));
			}
		}
		return requests;
	}

	private Request createRequest(Class<BindableService> serviceClass, Method method) throws Exception {
		Request request = new Request();
		request.setApplication(SdkConfigUtils.parseServerName(serviceClass));
		request.setParamTypes(getParamTypes(method));
		request.setInterfaceName(serviceClass.getTypeName());
		request.setMethodName(method.getName());
		Type returnType = method.getReturnType();
		if ("void".equalsIgnoreCase(returnType.getTypeName())) {
			request.setRequestType(Boolean.FALSE);
		} else {
			request.setRequestType(Boolean.TRUE);
		}
		return request;
	}

	private ParamType[] getParamTypes(Method method)
			throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Parameter[] parameters = method.getParameters();
		if (ArrayUtils.isEmpty(parameterTypes)) {
			return null;
		}
		ParamType[] paramTypes = new ParamType[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			ParamType paramType = new ParamType();
			paramType.setType(parameterTypes[i].getTypeName());
			if (!ArrayUtils.isEmpty(parameters) && i < parameters.length) {
				paramType.setName(parameters[i].getName());
			} else {
				paramType.setName(String.format("param%d", i));
			}
			paramType.setSample(sample(parameterTypes[i]));
			paramTypes[i] = paramType;
		}
		return paramTypes;
	}

	private Object sample(Class clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		if (Double.class == clazz || Float.class == clazz || double.class == clazz || float.class == clazz) {
			return 0.0;
		}
		if (Short.class == clazz || Integer.class == clazz || Long.class == clazz || short.class == clazz
				|| int.class == clazz || long.class == clazz) {
			return 0;
		}
		if (CharSequence.class.isAssignableFrom(clazz)) {
			return "";
		}
		if (Date.class == clazz) {
			return "2021-10-24 00:00:00";
		}
		if (Message.class.isAssignableFrom(clazz)) {
			Message.Builder builder = newBuilder(clazz);
			Descriptors.Descriptor descriptor = builder.getDescriptorForType();
			List<Descriptors.FieldDescriptor> fieldDescriptors = descriptor.getFields();
			Map<String, Object> keyValue = new HashMap<>();
			for (Descriptors.FieldDescriptor fieldDescriptor : fieldDescriptors) {
				if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
					keyValue.put(fieldDescriptor.getName(), 0);
				} else if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
					keyValue.put(fieldDescriptor.getName(), Collections.emptyMap());
				} else {
					keyValue.put(fieldDescriptor.getName(), fieldDescriptor.getDefaultValue());
				}
			}
			return keyValue;
		}
		try {
			return clazz.newInstance();
		} catch (Throwable e) {
		}
		return null;
	}
}
