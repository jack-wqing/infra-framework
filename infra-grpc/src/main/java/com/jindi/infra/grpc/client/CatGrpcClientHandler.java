package com.jindi.infra.grpc.client;


import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.jindi.infra.core.constants.CatType;
import com.jindi.infra.grpc.constant.RpcConsts;

public class CatGrpcClientHandler {

    public Transaction newTransaction(String method) {
        return Cat.newTransaction(CatType.RPC_CLIENT, method);
    }

    public void success(Transaction transaction) {
        transaction.setStatus(Transaction.SUCCESS);
    }

    public void fail(Transaction transaction, Throwable throwable) {
        transaction.setStatus(throwable);
    }

    public void complete(Transaction transaction) {
        transaction.complete();
    }

}
