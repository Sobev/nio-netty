package org.sobev.io_test.netty_dmeo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author luojx
 * @date 2022/5/18 16:06
 */
public class NettyClient {
    public static void main(String[] args) {
        NioEventLoopGroup loopGroup = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            //监听器的回调方法operationComplete()，将会在对应的操作完成时被调用
            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("连接成功");
                }else {
                    future.cause().printStackTrace();
                }
            });
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {

        } finally {
            loopGroup.shutdownGracefully();
        }
    }
}
