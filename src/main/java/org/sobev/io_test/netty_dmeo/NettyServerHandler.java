package org.sobev.io_test.netty_dmeo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * @author luojx
 * @date 2022/5/18 15:38
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    //ChannelHandlerContext, 其代表了 ChannelHandler 和 ChannelPipeline 之间的绑定
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("remote address :" + ctx.channel().remoteAddress());
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println(byteBuf.toString(CharsetUtil.UTF_8));

        ctx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer(cause.getMessage(), CharsetUtil.UTF_8));
    }
}
