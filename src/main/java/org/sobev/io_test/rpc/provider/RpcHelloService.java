package org.sobev.io_test.rpc.provider;

import org.sobev.io_test.rpc.api.IRpcHelloService;

/**
 * @author luojx
 * @date 2022/5/19 17:15
 */
public class RpcHelloService implements IRpcHelloService {
    @Override
    public String hello(String name) {
        return "Hola " + name + " :)";
    }
}
