package com.jindi.infra.feign.client;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.jindi.infra.feign.constant.CatType;
import feign.Client;
import feign.Request;
import feign.Response;

import java.io.IOException;


public class CatFeignClientDecorator implements Client {

    private final Client delegate;

    public CatFeignClientDecorator(Client delegate) {
        this.delegate = delegate;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Transaction transaction = Cat.newTransaction(CatType.FEIGN_CALL, request.requestTemplate().url());
        try {
            Response response = delegate.execute(request, options);
            transaction.setStatus(Message.SUCCESS);
            return response;
        } catch (Throwable e) {
            // catch 到异常，设置状态，代表此请求失败
            transaction.setStatus("ERROR");
            // 将异常上报到cat上
            Cat.logError(e);
            throw e;
        } finally {
            transaction.complete();
        }
    }
}
