# infra-governance
**服务治理模块**  
## 泳道记录
### 使用方式
1. 启动时补充启动配置langTag,例如-DlangTag=tyc-1234  
2. 请求时,在header中补充langTag=tyc-1234即可
```
1. dubbo的provider会根据这个标识完成打标,参考DubboGovernancePostProcessor
2. 注册nacos时,会根据这个标识完成打标,参考BaseServiceRegister
```
### feign
```参考LaneOpenFeignRequestInterceptor\LaneRule```
1. 选取服务端机器时,从Nacos获取全量服务端实例,再使用langTag进行filter(如果没有可用机器,会兜底随机获取一个实例)
2. 客户端发起请求时从ThreadLocal中取出langTag,放入header
3. 服务端通过LaneFilter(Http的Filter,feign实际也是一个http请求)获取泳道标识,再放入ThreadLocal中,在执行以后进行删除
### grpc  
```grpc的相对复杂一些,因为客户端服务端都是各种跨线程,参考LaneTagThreadLocal\LaneCallInterceptor\LaneGrpcCoreServerInterceptor```
1. [HuaweiNacosDiscoveryProvider] :chooseServer方法从nacos获取全部实例后利用langTag进行过滤(如果没有可用机器,会兜底随机获取一个实例)
2. [LaneCallInterceptor] :客户端发起请求时,从ThreadLocal中获取langTag,放入grpc的header中(这块利用了SimpleClientInterceptor实现的grpc简版拦截器)
3. [HeaderContextServerInterceptor] :服务端收到请求以后,将header中的所有内容放到Context中(Context是grpc原生的一个上下文工具)
4. [LaneGrpcCoreServerInterceptor] :实际开始执行grpc逻辑时,从Context获取langTag,放入ThreadLocal中,在执行以后进行删除
### dubbo
```参考LaneTagRouter\DubboLaneTagFilter```
1. [DubboLaneTagFilter]   
   1. 客户端从ThreadLocal获取langTag放入dubbo的上下文RpcContext.getContext()中
   2. 服务端从RpcContext.getContext()中获取langTag放入ThreadLocal中,执行结束删除
2. [LaneTagRouter] 自定义根据langTag进行路由的能力,(如果没有可用机器,会从无标签的机器中获取一个实例)
   ```
   Directory用于获取所有的实例
   Router用于做实例的过滤
   LoadBalance用于在过滤后剩余的实例中做负载均衡
   ```
