package com.jindi.infra.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ThrowableUtils {

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
