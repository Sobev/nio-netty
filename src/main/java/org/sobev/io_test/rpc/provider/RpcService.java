package org.sobev.io_test.rpc.provider;

import org.sobev.io_test.rpc.api.IRpcService;

/**
 * @author luojx
 * @date 2022/5/19 17:17
 */
public class RpcService implements IRpcService {
    @Override
    public int add(int a, int b) {
        return a+b;
    }

    @Override
    public int sub(int a, int b) {
        return a-b;
    }

    @Override
    public int mul(int a, int b) {
        return a*b;
    }

    @Override
    public int div(int a, int b) {
        return a/b;
    }
}
