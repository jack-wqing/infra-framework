package com.jindi.infra.governance.constant;

import org.apache.commons.lang3.StringUtils;

/**
 * @author changbo
 * @date 2021/8/27
 */
public enum GroupEnum {
    UNKNOWN("", "unknown", "unknown"),
    C("产品技术部-C端", "product-tech", "c"),
    ADS("产品技术部-商业化", "product-tech", "ads"),
    INFRA("产品技术部-基础架构", "product-tech", "infra"),
    EE("效率工程", "efficiency-engineering", "ee"),

    SEARCH("产品技术部-搜索", "product-tech", "search"),
    TRADE("后端组", "backend", "trade"),
    OPERATING("运营", "product-tech", "operating"),
    ENTERPRISE_INFORMATION("数据", "product-tech", "enterprise-information"),
    USER("用户", "product-tech", "user"),
    TYC_C("C端", "product-tech", "tyc-c"),
    DMP("DMP", "product-tech", "dmp"),
    INCUBATION("商机", "product-tech", "incubation"),
    ALGORITHM("算法", "product-tech", "algorithm"),
    KNOWLEDGE_GRAPH("知识图谱", "product-tech", "knowledge-graph"),
    ;

    private String belong;
    private String department;
    private String group;

    GroupEnum(String belong, String department, String group) {
        this.belong = belong;
        this.department = department;
        this.group = group;
    }

    public static GroupEnum getEnum(String name) {
        if (StringUtils.isBlank(name)) {
            return UNKNOWN;
        }
        try {
            return valueOf(name.toUpperCase().trim());
        } catch (Throwable e) {
            return UNKNOWN;
        }
    }

    public String getBelong() {
        return belong;
    }

    public String getDepartment() {
        return department;
    }

    public String getGroup() {
        return group;
    }
}
