package com.jindi.infra.grpc.util;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.GeneratedMessageV3;

public interface AsyncFunction<R extends GeneratedMessageV3> {

	ListenableFuture<R> get();
}
