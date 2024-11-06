package com.jindi.infra.tools.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum RpcLatencyPeriodEnum {

    CLIENT_BEFORE(0, "clientBefore", "客户端发起请求到实际发送请求的耗时(包括客户端排队 + 客户端序列化"),
    SEND_NET(1, "sendNet", "客户端发起网络请求后,到服务端接收到请求的耗时"),
    SERVER_BEFORE_INVOKE(2,"serverBeforeInvoke", "服务端收到请求到实际开始执行的耗时(包括反序列化等)"),
    SERVER_INVOKE(3,"serverInvoke", "服务端逻辑处理时间"),
    SERVER_AFTER_INVOKE(4, "serverAfterInvoke", "服务端处理完到请求开始返回的耗时(包括序列化等)"),
    RECV_NET(5, "recvNet", "处理结果从服务端返回到客户端的耗时"),
    CLIENT_AFTER(6, "clientAfter", "客户端收到请求以后得耗时(包括客户端反序列化等)"),
    ;

    private Integer order;
    private String name;
    private String desc;

    public static List<RpcLatencyPeriodEnum> LATENCY_PERIOD_LIST = Arrays.stream(RpcLatencyPeriodEnum.values())
            .sorted(Comparator.comparing(RpcLatencyPeriodEnum::getOrder).reversed()).collect(Collectors.toList());

    public static Map<String, RpcLatencyPeriodEnum> LATENCY_PERIOD_MAP = Arrays.stream(RpcLatencyPeriodEnum.values())
            .collect(Collectors.toMap(RpcLatencyPeriodEnum::getName, Function.identity()));

    public static RpcLatencyPeriodEnum getEnumByName(String name) {
        return LATENCY_PERIOD_MAP.get(name);
    }
}
