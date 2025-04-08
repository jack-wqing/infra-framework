package com.jindi.infra.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * 天眼查扩展
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.42.1)",
    comments = "Source: infra.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class TycExtendGrpc {

  private TycExtendGrpc() {}

  public static final String SERVICE_NAME = "infra.TycExtend";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.jindi.infra.grpc.Infra.Empty,
      com.jindi.infra.grpc.Infra.Empty> getPingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ping",
      requestType = com.jindi.infra.grpc.Infra.Empty.class,
      responseType = com.jindi.infra.grpc.Infra.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.jindi.infra.grpc.Infra.Empty,
      com.jindi.infra.grpc.Infra.Empty> getPingMethod() {
    io.grpc.MethodDescriptor<com.jindi.infra.grpc.Infra.Empty, com.jindi.infra.grpc.Infra.Empty> getPingMethod;
    if ((getPingMethod = TycExtendGrpc.getPingMethod) == null) {
      synchronized (TycExtendGrpc.class) {
        if ((getPingMethod = TycExtendGrpc.getPingMethod) == null) {
          TycExtendGrpc.getPingMethod = getPingMethod =
              io.grpc.MethodDescriptor.<com.jindi.infra.grpc.Infra.Empty, com.jindi.infra.grpc.Infra.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ping"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.jindi.infra.grpc.Infra.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.jindi.infra.grpc.Infra.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new TycExtendMethodDescriptorSupplier("ping"))
              .build();
        }
      }
    }
    return getPingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TycExtendStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TycExtendStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TycExtendStub>() {
        @java.lang.Override
        public TycExtendStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TycExtendStub(channel, callOptions);
        }
      };
    return TycExtendStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TycExtendBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TycExtendBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TycExtendBlockingStub>() {
        @java.lang.Override
        public TycExtendBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TycExtendBlockingStub(channel, callOptions);
        }
      };
    return TycExtendBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TycExtendFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TycExtendFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TycExtendFutureStub>() {
        @java.lang.Override
        public TycExtendFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TycExtendFutureStub(channel, callOptions);
        }
      };
    return TycExtendFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * 天眼查扩展
   * </pre>
   */
  public static abstract class TycExtendImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * 探活
     * </pre>
     */
    public void ping(com.jindi.infra.grpc.Infra.Empty request,
        io.grpc.stub.StreamObserver<com.jindi.infra.grpc.Infra.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPingMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.jindi.infra.grpc.Infra.Empty,
                com.jindi.infra.grpc.Infra.Empty>(
                  this, METHODID_PING)))
          .build();
    }
  }

  /**
   * <pre>
   * 天眼查扩展
   * </pre>
   */
  public static final class TycExtendStub extends io.grpc.stub.AbstractAsyncStub<TycExtendStub> {
    private TycExtendStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TycExtendStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TycExtendStub(channel, callOptions);
    }

    /**
     * <pre>
     * 探活
     * </pre>
     */
    public void ping(com.jindi.infra.grpc.Infra.Empty request,
        io.grpc.stub.StreamObserver<com.jindi.infra.grpc.Infra.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPingMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * 天眼查扩展
   * </pre>
   */
  public static final class TycExtendBlockingStub extends io.grpc.stub.AbstractBlockingStub<TycExtendBlockingStub> {
    private TycExtendBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TycExtendBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TycExtendBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 探活
     * </pre>
     */
    public com.jindi.infra.grpc.Infra.Empty ping(com.jindi.infra.grpc.Infra.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPingMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * 天眼查扩展
   * </pre>
   */
  public static final class TycExtendFutureStub extends io.grpc.stub.AbstractFutureStub<TycExtendFutureStub> {
    private TycExtendFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TycExtendFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TycExtendFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * 探活
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.jindi.infra.grpc.Infra.Empty> ping(
        com.jindi.infra.grpc.Infra.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPingMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PING = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final TycExtendImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(TycExtendImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PING:
          serviceImpl.ping((com.jindi.infra.grpc.Infra.Empty) request,
              (io.grpc.stub.StreamObserver<com.jindi.infra.grpc.Infra.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class TycExtendBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TycExtendBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.jindi.infra.grpc.Infra.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TycExtend");
    }
  }

  private static final class TycExtendFileDescriptorSupplier
      extends TycExtendBaseDescriptorSupplier {
    TycExtendFileDescriptorSupplier() {}
  }

  private static final class TycExtendMethodDescriptorSupplier
      extends TycExtendBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    TycExtendMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (TycExtendGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TycExtendFileDescriptorSupplier())
              .addMethod(getPingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
