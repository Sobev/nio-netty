package org.sobev.io_test.netty_dmeo;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @author luojx
 * @date 2022/5/19 14:41
 */
public class NettyProxyServer {
    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler((new SimpleChannelInboundHandler<ByteBuf>(){
                    ChannelFuture connectFuture;
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        System.out.println("Channel active");
                        Bootstrap bootstrap = new Bootstrap();
                        bootstrap.channel(NioServerSocketChannel.class)
                                //为入站 I/O 设置 ChannelInboundHandler  这里的入站就是target的inboundHandler
                                .handler(new SimpleChannelInboundHandler<ByteBuf>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext targetCtx, ByteBuf msg) throws Exception {
                                        System.out.println("received data from baidu");
                                    }
                                });
                        bootstrap.group(ctx.channel().eventLoop());
                        connectFuture = bootstrap.connect(new InetSocketAddress("www.baidu.com", 443));
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        if(connectFuture.isDone()){
//                            ctx.writeAndFlush(msg);
                            System.out.println(msg.toString(CharsetUtil.UTF_8));
                        }
                    }
                }));
        ChannelFuture channelFuture = serverBootstrap.bind(15656).sync().addListeners(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("服务器启动成功");
                } else {
                    System.out.println("服务器启动失败");
                }
            }
        });
        channelFuture.channel().closeFuture().sync();
    }
}
