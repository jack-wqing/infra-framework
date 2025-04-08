package com.jindi.infra.grpc.server;

import com.jindi.infra.grpc.util.ExecutorsUtils;
import lombok.Data;


@Data
public class ServerProperties {


	private Integer port = 9999;


	private Integer awaitTerminationSecond = 5;


	private Integer workerThreads = 200;

	private String threadPool = ExecutorsUtils.FIXED;
}
