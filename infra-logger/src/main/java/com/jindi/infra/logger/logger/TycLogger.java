package com.jindi.infra.logger.logger;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import com.dianping.cat.Cat;
import com.jindi.infra.common.util.InnerJSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TycLogger {

    private static List<TycLogDataInterface> interfaces = new LinkedList<>();

    public static final String LOG_DATA_TAG = "logdata";
    public static final String LOGGER_ERROR = "TycLoggerError";
    public static final String EMPTY_JSON = "{}";
    private Logger logger;

    public TycLogger(Logger log) {
        this.logger = log;
    }

    private void doLog(Object obj, Runnable runnable) {
        String value = StringUtils.EMPTY;
        try {
            value = toValue(obj);
        } catch (Exception e) {
            Cat.logEvent(LOGGER_ERROR, e.getMessage());
        }
        doLog(value, runnable);
    }

    private void doLog(String obj, Runnable runnable) {
        MDC.put(LOG_DATA_TAG, obj);
        runnable.run();
        MDC.remove(LOG_DATA_TAG);
    }

    public void trace(Object obj, String msg) {
        if (obj instanceof String) {
            logger.trace((String)obj, msg);
        } else {
            doLog(obj, () -> logger.trace(msg));
        }
    }

    public void trace(Object obj, String format, Object arg) {
        doLog(obj, () -> logger.trace(format, arg));
    }

    public void trace(Object obj, String format, Object arg1, Object arg2) {
        doLog(obj, () -> logger.trace(format, arg1, arg2));
    }

    public void trace(Object obj, String format, Object... arguments) {
        doLog(obj, () -> logger.trace(format, arguments));
    }

    public void trace(Object obj, String msg, Throwable t) {
        doLog(obj, () -> logger.trace(msg, t));
    }

    public void debug(Object obj, String msg) {
        if (obj instanceof String) {
            logger.debug((String)obj, msg);
        } else {
            doLog(obj, () -> logger.debug(msg));
        }
    }
    public void debug(Object obj, String format, Object arg) {
        doLog(obj, () -> logger.debug(format, arg));
    }
    public void debug(Object obj, String format, Object arg1, Object arg2) {
        doLog(obj, () -> logger.debug(format, arg1, arg2));
    }
    public void debug(Object obj, String format, Object... arguments) {
        doLog(obj, () -> logger.debug(format, arguments));
    }
    public void debug(Object obj, String msg, Throwable t) {
        doLog(obj, () -> logger.debug(msg, t));
    }
    public void info(Object obj, String msg) {
        if (obj instanceof String) {
            logger.info((String)obj, msg);
        } else {
            doLog(obj, () -> logger.info(msg));
        }
    }
    public void info(Object obj, String format, Object arg) {
        doLog(obj, () -> logger.info(format, arg));
    }
    public void info(Object obj, String format, Object arg1, Object arg2) {
        doLog(obj, () -> logger.info(format, arg1, arg2));
    }
    public void info(Object obj, String format, Object... arguments) {
        doLog(obj, () -> logger.info(format, arguments));
    }
    public void info(Object obj, String msg, Throwable t) {
        doLog(obj, () -> logger.info(msg, t));
    }
    public void warn(Object obj, String msg) {
        if (obj instanceof String) {
            logger.warn((String)obj, msg);
        } else {
            doLog(obj, () -> logger.warn(msg));
        }
    }
    public void warn(Object obj, String format, Object arg) {
        doLog(obj, () -> logger.warn(format, arg));
    }
    public void warn(Object obj, String format, Object... arguments) {
        doLog(obj, () -> logger.warn(format, arguments));
    }
    public void warn(Object obj, String format, Object arg1, Object arg2) {
        doLog(obj, () -> logger.warn(format, arg1, arg2));
    }
    public void warn(Object obj, String msg, Throwable t) {
        doLog(obj, () -> logger.warn(msg, t));
    }
    public void error(Object obj, String msg) {
        if (obj instanceof String) {
            logger.error((String)obj, msg);
        } else {
            doLog(obj, () -> logger.error(msg));
        }
    }
    public void error(Object obj, String format, Object arg) {
        doLog(obj, () -> logger.error(format, arg));
    }
    public void error(Object obj, String format, Object arg1, Object arg2) {
        doLog(obj, () -> logger.error(format, arg1, arg2));
    }
    public void error(Object obj, String format, Object... arguments) {
        doLog(obj, () -> logger.error(format, arguments));
    }
    public void error(Object obj, String msg, Throwable t) {
        doLog(obj, () -> logger.error(msg, t));
    }


    public void info(Pair<?,?>...pairs) {
        info(toMap(pairs));
    }
    public void trace(Pair<?,?>...pairs) {
        trace(toMap(pairs));
    }
    public void debug(Pair<?,?>...pairs) {
        debug(toMap(pairs));
    }
    public void warn(Pair<?,?>...pairs) {
        warn(toMap(pairs));
    }
    public void error(Pair<?,?>...pairs) {
        error(toMap(pairs));
    }

    public void info(Object obj) {
        String value = toValue(obj);
        doLog(value, () -> logger.trace(value));
    }
    public void trace(Object obj) {
        String value = toValue(obj);
        doLog(value, () -> logger.trace(value));
    }
    public void debug(Object obj) {
        String value = toValue(obj);
        doLog(value, () -> logger.debug(value));
    }
    public void warn(Object obj) {
        String value = toValue(obj);
        doLog(value, () -> logger.warn(value));
    }
    public void error(Object obj) {
        String value = toValue(obj);
        doLog(value, () -> logger.error(value));
    }


    public void info(String message) {
        logger.info(message);
    }
    public void trace(String message) {
        logger.trace(message);
    }
    public void debug(String message) {
        logger.debug(message);
    }
    public void warn(String message) {
        logger.warn(message);
    }
    public void error(String message) {
        logger.error(message);
    }

    public void info(String format, Object...args) {
        logger.info(format, args);
    }
    public void trace(String format, Object...args) {
        logger.trace(format, args);
    }
    public void debug(String format, Object...args) {
        logger.debug(format, args);
    }
    public void warn(String format, Object...args) {
        logger.warn(format, args);
    }
    public void error(String format, Object...args) {
        logger.error(format, args);
    }

    public void info(String message, Throwable t) {
        logger.info(message, t);
    }
    public void trace(String message, Throwable t) {
        logger.trace(message, t);
    }
    public void debug(String message, Throwable t) {
        logger.debug(message, t);
    }
    public void warn(String message, Throwable t) {
        logger.warn(message, t);
    }
    public void error(String message, Throwable t) {
        logger.error(message, t);
    }


    public static Map<String, String> toMap(Object... args) {
        int length = args.length;
        Map<String, String> map = new HashMap<>(length / 2);
        for (int i = 0; i + 1 < length; i += 2) {
            map.put(String.valueOf(args[i]), String.valueOf(args[i + 1]));
        }
        return map;
    }

    public static Map<String, String> toMap(Pair<?, ?>... args) {
        int length = args.length;
        Map<String, String> map = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            map.put(String.valueOf(args[i].getKey()), String.valueOf(args[i].getValue()));
        }
        return map;
    }

    private static String toValue(Object obj) {
        Map map;
        try {
            if (obj == null) {
                map = new HashMap();
            } else {
                map = BeanUtil.beanToMap(obj);
            }
        } catch (Exception e) {
            Cat.logEvent(LOGGER_ERROR, e.getMessage());
            return EMPTY_JSON;
        }
        for (TycLogDataInterface tycLogDataInterface :interfaces){
            try {
                map.putAll(tycLogDataInterface.get());
            } catch (Exception e) {
                Cat.logEvent(LOGGER_ERROR, tycLogDataInterface.getClass().getSimpleName() + ":" + e.getMessage());
            }
        }
        return InnerJSONUtils.toJSONString(map);

    }

    public static void addInterface(TycLogDataInterface loader) {
        interfaces.add(loader);
    }
}
