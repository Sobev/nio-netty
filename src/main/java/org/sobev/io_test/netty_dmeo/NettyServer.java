package org.sobev.io_test.netty_dmeo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.sobev.io_test.netty_dmeo.idle.IdleHandler;

/**
 * @author luojx
 * @date 2022/5/18 15:50
 */
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        //创建两个线程组 用于接收客户端的连接 和 处理客户端的请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //backlog 用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyServerHandler());
                            ch.pipeline().addLast(new IdleStateHandler(10, 5, 0));
                            ch.pipeline().addLast(new IdleHandler());
                        }
                    });
            System.out.println("服务器启动成功");
            //异步绑定端口 同步阻塞等待成功
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            //获取 Channel 的CloseFuture，并且阻塞当前线程直到它完成
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
