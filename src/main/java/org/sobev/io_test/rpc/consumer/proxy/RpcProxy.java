package org.sobev.io_test.rpc.consumer.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.sobev.io_test.rpc.protocol.InvokerProtocol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author luojx
 * @date 2022/5/20 9:18
 */
public class RpcProxy {

    public static <T> T create(Class<?> clazz) {
        Class<?>[] interfaceClass = clazz.isInterface() ?
                new Class[]{clazz} :
                clazz.getInterfaces();
        RpcInvocationHandler handler = new RpcInvocationHandler(clazz);
        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaceClass, handler);
        return result;
    }

    static class RpcInvocationHandler implements InvocationHandler {

        Class<?> clazz;

        public RpcInvocationHandler(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            return rpcInvoke(proxy, method, args);
        }

        private Object rpcInvoke(Object proxy, Method method, Object[] args) throws Throwable {
            InvokerProtocol protocol = new InvokerProtocol();
            protocol.setClassName(clazz.getName());
            protocol.setMethodName(method.getName());
            protocol.setParamTypes(method.getParameterTypes());
            protocol.setParams(args);

            System.out.println("execute rpc invoke " + protocol.toString());

            RpcProxyHandler rpcProxyHandler = new RpcProxyHandler();
            NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
            try {
                Bootstrap bootstrap = new Bootstrap()
                        .group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                //协议包长度解码器
                                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                //协议包编码器
                                ch.pipeline().addLast(new LengthFieldPrepender(4));
                                //
                                ch.pipeline().addLast(new ObjectEncoder());
                                //
                                ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                //
                                ch.pipeline().addLast(rpcProxyHandler);
                            }
                        });
                ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync();
                channelFuture.channel().writeAndFlush(protocol).sync();
                channelFuture.channel().closeFuture().sync();

            } finally {
                eventLoopGroup.shutdownGracefully();
            }
            return rpcProxyHandler.getMsg();
        }
    }
}
