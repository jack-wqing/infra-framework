package com.jindi.infra.datasource.utils;


import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public class DynamicObjectProxy<T> implements InvocationHandler {

    public final AtomicReference<T> delegate;

    private DynamicObjectProxy(T delegate) {
        this.delegate = new AtomicReference<>(delegate);
    }

    public static <U> U wrap(U obj) {
        return (U)Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(), new DynamicObjectProxy(obj));
    }

    public T getAndSetNewDelegate(T newObj) {
        return delegate.getAndSet(newObj);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return ReflectionUtils.invokeMethod(method, delegate.get(), args);
    }
}
