package org.sobev.io_test.netty_dmeo.idle;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

/**
 * @author luojx
 * @date 2022/5/21 9:12
 */
public class IdleHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                System.out.println("reader idle");
//                ctx.close();
            }else if(event.state() == IdleState.WRITER_IDLE){
                System.out.println("writer idle");
                ctx.writeAndFlush(Unpooled.copiedBuffer("ping pong", CharsetUtil.UTF_8));
            }
        }
    }
}
