package com.zspace.infra.metrics.cat.interceptor;

import com.dianping.cat.Cat;
import com.jindi.infra.common.util.InnerIpUtils;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.core.constants.CatType;
import com.jindi.infra.grpc.constant.RpcConsts;
import com.jindi.infra.grpc.extension.CallInterceptor;
import com.jindi.infra.grpc.model.CallContext;
import com.jindi.infra.grpc.util.GrpcUtils;
import com.jindi.infra.grpc.util.NameUtils;
import com.jindi.infra.logger.elasticsearch.ElasticSearchWriter;
import com.zspace.infra.metrics.cat.CatContext;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class CatGrpcCrossClientInterceptor implements CallInterceptor {

	private static final String RPC_CLIENT_INFRA_TYC_EXTEND_PING = "Rpc.Client:infra.TycExtend/ping()";
	@Autowired
	private ObjectProvider<ElasticSearchWriter> elasticSearchWriterObjectProvider;


	@Value("${spring.application.name}")
	private String application;

	@Override
	public void before(Long id, MethodDescriptor method, Map<String, String> extHeaders) {
		String name = NameUtils.getMethodName(RpcConsts.RPC_CLIENT_TITLE, method);
		if (needIgnore(name)) {
			return;
		}
		CatContext catContext = new CatContext();
		addRpcClientEvents();
		Cat.logRemoteCallClient(catContext);
		fillExtHeaders(extHeaders, catContext);
	}

	private void fillExtHeaders(Map<String, String> extHeaders, CatContext catContext) {
		extHeaders.put(CatType.CAT_CONTEXT, InnerJSONUtils.toJSONString(catContext));
		extHeaders.put(CatType.CLIENT, application);
		extHeaders.put(CatType.CLIENT_IP, InnerIpUtils.getCachedIP());

	}

	@Override
	public void after(Long id, MethodDescriptor method, Throwable cause) {
		String name = NameUtils.getMethodName(RpcConsts.RPC_CLIENT_TITLE, method);
		if (needIgnore(name)) {
			return;
		}
		logUnknownException2ES(cause, name);
	}

	private Boolean needIgnore(String name) {
		if (Objects.equals(RPC_CLIENT_INFRA_TYC_EXTEND_PING, name)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private void logUnknownException2ES(Throwable cause, String name) {
		ElasticSearchWriter elasticSearchWriter = elasticSearchWriterObjectProvider.getIfAvailable();
		try {
			if (cause != null) {
				Throwable throwable = GrpcUtils.parseCause(cause);
				log.warn("{}:{}", CatType.RPC_CLIENT, name, throwable);
				if (elasticSearchWriter != null && !isBizException(cause)) {
					elasticSearchWriter.write(CatType.RPC_CLIENT, name, com.jindi.infra.core.util.GrpcUtils.getStackTrace(throwable));
				}
			}
		} catch (Throwable e) {
			log.warn("cat", e);
			if (elasticSearchWriter != null) {
				elasticSearchWriter.write(CatType.RPC_CLIENT, name, com.jindi.infra.core.util.GrpcUtils.getStackTrace(e));
			}
		}
	}

	private boolean isBizException(Throwable cause) {
		if (cause instanceof StatusRuntimeException) {
			StatusRuntimeException statusRuntimeException = (StatusRuntimeException) cause;
			if (statusRuntimeException.getStatus().getCode() == Status.UNKNOWN.getCode()) {
				return true;
			}
		}
		return false;
	}

	private void addRpcClientEvents() {
		CallContext callContext = CallContext.currentCallContext();
		if (callContext == null || callContext.getNode() == null) {
			return;
		}
		Cat.logEvent(CatType.RPC_CLIENT_APP, callContext.getServerName());
		Cat.logEvent(CatType.RPC_CLIENT_SERVER, callContext.getNode().getHost());
		Cat.logEvent(CatType.RPC_CLIENT_PORT, String.valueOf(callContext.getNode().getPort()));
	}

}
