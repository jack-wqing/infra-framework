package com.jindi.infra.space.config;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.jindi.infra.common.util.InnerJSONUtils;
import com.jindi.infra.space.annotation.Endpoint;
import com.jindi.infra.space.annotation.SpaceCloud;
import com.jindi.infra.space.client.GraphQLHttpClients;
import com.jindi.infra.space.constant.SpaceCloudConsts;
import com.jindi.infra.space.param.SpaceCloudParam;
import com.jindi.infra.space.properties.SpaceCloudConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Aspect
public class EndpointAspect {

    @Autowired
    private GraphQLHttpClients graphQLHttpClients;
    @Autowired
    private SpaceCloudConfigProperties spaceCloudConfigProperties;

    @Around("@annotation(endpoint)")
    public Object doAround(ProceedingJoinPoint pjp, Endpoint endpoint) throws Throwable {
        Signature sig = pjp.getSignature();
        if (!(sig instanceof MethodSignature)) {
            return pjp.proceed();
        }
        MethodSignature methodSignature = (MethodSignature) sig;
        Class<?> declaringClass = methodSignature.getMethod().getDeclaringClass();
        SpaceCloud spaceCloud = declaringClass.getAnnotation(SpaceCloud.class);
        if (spaceCloud == null) {
            return pjp.proceed();
        }
        String method = String.format("%s.%s",
                declaringClass.getSimpleName(),
                methodSignature.getMethod().getName());
        Transaction transaction = null;
        SpaceCloudParam spaceCloudParam = null;
        try {
            spaceCloudParam = createSpaceCloudParam(spaceCloud, endpoint);
            SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.set(spaceCloudParam);
            transaction = Cat.newTransaction(spaceCloudParam.getType(), method);
            Object returnValue = pjp.proceed();
            transaction.setStatus(Message.SUCCESS);
            return returnValue;
        } catch (Throwable e) {
            Cat.logError(e);
            if (transaction != null) {
                transaction.setStatus("ERROR");
            }
            log.error("spaceCloudParam = {}, args = {} 失败", InnerJSONUtils.toJSONString(spaceCloudParam), InnerJSONUtils.toJSONString(pjp.getArgs()), e);
            throw e;
        } finally {
            SpaceCloudConsts.SPACE_CLOUD_PARAM_THREAD_LOCAL.remove();
            if (transaction != null) {
                transaction.complete();
            }
        }
    }

    private SpaceCloudParam createSpaceCloudParam(SpaceCloud spaceCloud, Endpoint endpoint) {
        String type = endpoint.type();
        if (StringUtils.isBlank(type)) {
            type = spaceCloudConfigProperties.getDefaultGraphQLType();
        }
        return new SpaceCloudParam(spaceCloud.project(), spaceCloud.service(), endpoint.value(), type);
    }
}
