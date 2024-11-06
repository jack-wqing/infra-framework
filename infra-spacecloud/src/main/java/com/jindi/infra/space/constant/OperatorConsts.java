package com.jindi.infra.space.constant;

import java.util.HashMap;
import java.util.Map;

public class OperatorConsts {

    public static final String EQ = "_eq";

    public static final String GT = "_gt";

    public static final String GTE = "_gte";

    public static final String LT = "_lt";

    public static final String LTE = "_lte";

    public static final String IN = "_in";

    public static final String NIN = "_nin";

    public static final String LIKE = "_like";

    public static final String NE = "_ne";

    public static final String OR = "_or";

    public static final String AND = "_and";

    public static final String IS_NULL = "_isnull";

    public static final String LIKE_TEMPLATE = "%%%s%%";

    public static final String L_LIKE_TEMPLATE = "%s%%";

    public static final String R_LIKE_TEMPLATE = "%%%s";

    public static final String SQL_EQ = "=";

    public static final String SQL_GT = ">";

    public static final String SQL_GTE = ">=";

    public static final String SQL_LT = "<";

    public static final String SQL_LTE = "<=";

    public static final String SQL_IN = "in";

    public static final String SQL_NIN = "not in";

    public static final String SQL_LIKE = "like";

    public static final String SQL_NE = "<>";

    public static final String SQL_OR = "or";

    public static final String SQL_AND = "and";

    public static final String SQL_IS_NULL = "IS NULL";

    public static final String SQL_IS_NOT_NULL = "IS NOT NULL";

    public static final String SQL_LIKE_TEMPLATE = "%%%s%%";

    public static final String SQL_L_LIKE_TEMPLATE = "%s%%";

    public static final String SQL_R_LIKE_TEMPLATE = "%%%s";

    public static final Map<String, String> OP_MAP = new HashMap<String, String>() {
        {
            put(EQ, SQL_EQ);
            put(GT, SQL_GT);
            put(GTE, SQL_GTE);
            put(LT, SQL_LT);
            put(LTE, SQL_LTE);
            put(IN, SQL_IN);
            put(NIN, SQL_NIN);
            put(LIKE, SQL_LIKE);
            put(NE, SQL_NE);
            put(OR, SQL_OR);
            put(AND, SQL_AND);
            put(IS_NULL, SQL_IS_NULL);
            put(LIKE_TEMPLATE, SQL_LIKE_TEMPLATE);
            put(L_LIKE_TEMPLATE, SQL_L_LIKE_TEMPLATE);
            put(R_LIKE_TEMPLATE, SQL_R_LIKE_TEMPLATE);
        }
    };

    public static final String SQL_NO_VALUE_TEMPLATE = " %s %s";

    public static final String SQL_VALUE_TEMPLATE = " %s %s %s";
}
