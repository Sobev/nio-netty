package org.sobev.io_test.rpc.consumer;

import org.sobev.io_test.rpc.api.IRpcHelloService;
import org.sobev.io_test.rpc.consumer.proxy.RpcProxy;

/**
 * @author luojx
 * @date 2022/5/20 10:17
 */
public class RpcConsumer {
    public static void main(String[] args) {
        IRpcHelloService iRpcHelloService = RpcProxy.create(IRpcHelloService.class);
        System.out.println(iRpcHelloService.hello("hello world"));

//        IRpcService iRpcService  = RpcProxy.create(IRpcService.class);
//        System.out.println(iRpcService.add(1,2));
//        System.out.println(iRpcService.sub(3,4));
//        System.out.println(iRpcService.mul(5,6));
//        System.out.println(iRpcService.div(12,2));
    }
}
