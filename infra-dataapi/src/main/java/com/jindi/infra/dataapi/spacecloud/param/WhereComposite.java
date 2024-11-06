package com.jindi.infra.dataapi.spacecloud.param;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.jindi.infra.dataapi.spacecloud.constant.OperatorConsts;
import com.jindi.infra.dataapi.spacecloud.wrapper.CompositeItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhereComposite implements Serializable {

    private Map<String, Object> params;

    // roma参数转化，兼容composite参数
    public Map<String, Object> getRomaParams(Boolean isComplexSql) {
        HashMap<String, Object> romaParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if ("composite".equals(entry.getKey())) {
                Boolean isComplexParams = false;
                if (JSONObject.toJSONString(entry.getValue()).contains(OperatorConsts.OR)){
                    isComplexParams = true;
                }

                if (isComplexParams || isComplexSql) {
                    StringBuffer whereValue = getSql(((List<CompositeItem>) entry.getValue()));
                    romaParams.put("where", whereValue.toString());
                } else {
                    for (CompositeItem compositeItem : (List<CompositeItem>) entry.getValue()) {
                        if (OperatorConsts.IS_NULL.equals(compositeItem.getOp())) {
                            if ((boolean) compositeItem.getValue()) {
                                romaParams.put(compositeItem.getField(), OperatorConsts.SQL_IS_NULL);
                            } else {
                                romaParams.put(compositeItem.getField(), OperatorConsts.SQL_IS_NOT_NULL);
                            }
                        } else {
                            romaParams.put(compositeItem.getField(), getValue(compositeItem.getValue()));
                        }
                    }
                }
            } else {
                romaParams.put(entry.getKey(), entry.getValue());
            }
        }
        return romaParams;
    }

    private Object getValue(Object value) {
        if (value instanceof String && "".equals(value)) {
            return "''";
        }
        return value;
    }

    private StringBuffer getWhereSql(List<CompositeItem> compositeItems) {
        StringBuffer where = new StringBuffer();
        if (compositeItems.size() == 0) {
            return new StringBuffer();
        }
        for (CompositeItem composite : compositeItems) {
            where = getOpSql(composite, where);
        }
        return where;
    }

    private StringBuffer getSql(List<CompositeItem> compositeItems) {
        StringBuffer whereValue = new StringBuffer();
        StringBuffer whereSql = getWhereSql(compositeItems);
        if (whereSql.length() != 0) {
            whereValue.append(whereSql.substring(4));
        }
        return whereValue;
    }

    private StringBuffer getOpSql(CompositeItem composite, StringBuffer where) {
        String sqlOp = OperatorConsts.OP_MAP.get(composite.getOp());
        String sqlValue = composite.getValue().toString();
        String sqlField = composite.getField();
        if (composite.getValue() instanceof String) {
            String compositeValue = (String) composite.getValue();
            if (compositeValue.startsWith("\"") && compositeValue.endsWith("\"")) {
                sqlValue = sqlValue.substring(1, sqlValue.length() - 1);
            }
            sqlValue = String.format("'%s'", sqlValue);
        }
        if (OperatorConsts.OR.equals(composite.getOp()) || OperatorConsts.AND.equals(composite.getOp())) {
            StringBuffer orOpBuffer = new StringBuffer();
            List<CompositeItem> compositeItems = (List<CompositeItem>) composite.getValue();
            for (CompositeItem c : compositeItems) {
                orOpBuffer = getOpSql(c, orOpBuffer);
            }
            if (orOpBuffer.length() != 0) {
                where.append(String.format(" %s (", sqlOp));
                if (orOpBuffer.toString().startsWith(" and")) {
                    where.append(orOpBuffer.substring(5));
                } else {
                    where.append(orOpBuffer.substring(4));
                }
                where.append(")");
            }
            return where;
        }
        where.append(" and");
        switch (composite.getOp()) {
            case OperatorConsts.IS_NULL:
                if ((boolean) composite.getValue()) {
                    where.append(String.format(OperatorConsts.SQL_NO_VALUE_TEMPLATE, sqlField, OperatorConsts.SQL_IS_NULL));
                } else {
                    where.append(String.format(OperatorConsts.SQL_NO_VALUE_TEMPLATE, sqlField, OperatorConsts.SQL_IS_NOT_NULL));
                }
                break;
            case OperatorConsts.L_LIKE_TEMPLATE:
                where.append(String.format(OperatorConsts.SQL_VALUE_TEMPLATE, sqlField,
                        OperatorConsts.SQL_LIKE, String.format(OperatorConsts.SQL_L_LIKE_TEMPLATE, sqlValue)));
                break;
            case OperatorConsts.R_LIKE_TEMPLATE:
                where.append(String.format(OperatorConsts.SQL_VALUE_TEMPLATE, sqlField,
                        OperatorConsts.SQL_LIKE, String.format(OperatorConsts.SQL_R_LIKE_TEMPLATE, sqlValue)));
                break;
            case OperatorConsts.LIKE_TEMPLATE:
                where.append(String.format(OperatorConsts.SQL_VALUE_TEMPLATE, sqlField,
                        OperatorConsts.SQL_LIKE, String.format(OperatorConsts.SQL_LIKE_TEMPLATE, sqlValue)));
                break;
            case OperatorConsts.IN:
            case OperatorConsts.NIN:
                where.append(String.format(OperatorConsts.SQL_VALUE_TEMPLATE, sqlField,
                        sqlOp, getInSql(sqlValue)));
                break;
            default:
                where.append(String.format(OperatorConsts.SQL_VALUE_TEMPLATE, sqlField,
                        sqlOp, sqlValue));
                break;
        }
        return where;
    }

    private String getInSql(Object sqlValue) {
        String inSql = sqlValue.toString();
        inSql = inSql.replace(", ", "', '");
        inSql = inSql.replace("[", "('");
        inSql = inSql.replace("]", "')");
        return inSql;
    }
}
