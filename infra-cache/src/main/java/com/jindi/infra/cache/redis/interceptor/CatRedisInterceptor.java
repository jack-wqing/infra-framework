package com.jindi.infra.cache.redis.interceptor;


import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.jindi.infra.cache.redis.key.Key;
import com.jindi.infra.common.util.InnerJSONUtils;

public class CatRedisInterceptor extends RedisInterceptor {

    private static ThreadLocal<Transaction> transactionThreadLocal = new ThreadLocal<>();

    public static final String CAT_CACHE = "Cache.Redis";
    public static final String CAT_CACHE_RESULT = "Cache.Redis.Result";
    public static final String CAT_CACHE_CONNECTION_EVENT = "Cache.Redis.Connection";

    @Override
    public void doBefore(String opt, Key key, String connection) {
        if (transactionThreadLocal.get() != null) {
            return;
        }
        Transaction transaction = null;
        if (key != null) {
            transaction = Cat.newTransaction(CAT_CACHE, key.getTemplate() + ":(" + opt + ")");
            Cat.logEvent(CAT_CACHE_CONNECTION_EVENT, connection, Message.SUCCESS, key.getKey());
        } else {
            transaction = Cat.newTransaction(CAT_CACHE, opt);
            Cat.logEvent(CAT_CACHE_CONNECTION_EVENT, connection);
        }

        transactionThreadLocal.set(transaction);
    }

    @Override
    public void doAfter(String opt, Key key, String connection) {
        Transaction transaction = transactionThreadLocal.get();
        if (transaction != null) {
            transaction.setSuccessStatus();
        }
    }

    @Override
    public void doError(String opt, Key key, String connection, Throwable e) {
        Transaction transaction = transactionThreadLocal.get();
        if (transaction != null) {
            transaction.setStatus(e);
        }
    }

    @Override
    public void doFinally(String opt, Key key, String connection, Object result) {
        Transaction transaction = transactionThreadLocal.get();
        if (transaction != null) {
            logResult(opt, result);
            transaction.complete();
        }
        transactionThreadLocal.remove();
    }

    private void logResult(String opt, Object result) {
        if (!Cat.getManager().isTraceMode()) {
            return;
        }
        String resStr;
        try {
            resStr = InnerJSONUtils.toJSONString(result);
        } catch (Exception e) {
            try {
                resStr = result.toString();
            } catch (Exception ex) {
                resStr = "结果序列化异常";
            }
        }
        Cat.logTrace(CAT_CACHE_RESULT, opt, Message.SUCCESS, resStr);
    }


}
