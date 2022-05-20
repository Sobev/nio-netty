package org.sobev.io_test.netty_dmeo;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.TimeUnit;

/**
 * @author luojx
 * @date 2022/5/20 14:08
 */
public class NettyPromise {
    public static void main(String[] args) {
        NettyPromise nettyPromise = new NettyPromise();
        Promise<String> promise = nettyPromise.jobPromise("hello world");
        promise.addListener(future -> {
            if (future.isSuccess()){
                System.out.println(future.get());
            }else {
                System.out.println(future.cause());
            }
        });
    }

    private <T> Promise<T> jobPromise(T param) {
        NioEventLoopGroup loopGroup = new NioEventLoopGroup();
        DefaultPromise<T> promise = new DefaultPromise<T>(loopGroup.next());
        loopGroup.schedule(() -> {
            try {
                Thread.sleep(3000);
                //Marks this future as a success and notifies all listeners.
                // If it is success or failed already it will throw an IllegalStateException.
                promise.setSuccess(param);
                return promise;
            }catch (Exception e){
                promise.setFailure(e);
            }
            return promise;
        },0, TimeUnit.SECONDS);
        return promise;
    }
}
