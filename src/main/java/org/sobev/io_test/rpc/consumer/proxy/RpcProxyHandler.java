package org.sobev.io_test.rpc.consumer.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author luojx
 * @date 2022/5/20 10:09
 */
public class RpcProxyHandler extends ChannelInboundHandlerAdapter {

    private Object msg;

    public Object getMsg() {
        return msg;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到服务端返回的数据：" + msg);
        this.msg = msg;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

}
