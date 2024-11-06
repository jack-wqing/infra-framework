package com.jindi.infra.grpc.channel;

import com.jindi.infra.grpc.extension.DiscoveryProvider;
import com.jindi.infra.grpc.extension.Node;

import java.util.List;

public class NullDiscoveryProvider implements DiscoveryProvider {

    @Override
    public void register() throws Exception {

    }

    @Override
    public void unregister() throws Exception {

    }

    @Override
    public Node chooseServer(String serverName) {
        return null;
    }

    @Override
    public List<Node> getAllNodes(String serverName) {
        return null;
    }

    @Override
    public String getRegion() {
        return null;
    }
}
