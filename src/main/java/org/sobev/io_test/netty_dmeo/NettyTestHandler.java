package org.sobev.io_test.netty_dmeo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * @author luojx
 * @date 2022/5/19 10:10
 */
@ChannelHandler.Sharable
public class NettyTestHandler extends SimpleChannelInboundHandler<ByteBuf> {
    //当 channelRead0()方法完成时，你已经有了传入消息，并且已经处理完它了。当该方
    //法返回时，SimpleChannelInboundHandler 负责释放指向保存该消息的 ByteBuf 的内存引用
    //channelRead(){channelRead0(); releaseReference();}
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello client", CharsetUtil.UTF_8));
    }
}
