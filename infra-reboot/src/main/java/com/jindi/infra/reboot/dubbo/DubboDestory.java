package com.jindi.infra.reboot.dubbo;

import com.alibaba.dubbo.config.ProtocolConfig;

public class DubboDestory {

    public DubboDestory() {
        // 主动加载ProtocolConfig类，解决shutdown时出现该类未初始化异常
        ProtocolConfig protocolConfig = new ProtocolConfig();
    }

    public void shutdownDubbo() {
        ProtocolConfig.destroyAll();
    }
}
