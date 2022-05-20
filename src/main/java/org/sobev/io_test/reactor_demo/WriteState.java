package org.sobev.io_test.reactor_demo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author luojx
 * @date 2022/5/12 14:39
 */
public class WriteState implements HandlerState {
    @Override
    public void changeState(TCPHandler h) {
        h.setState(new ReadState());
    }

    @Override
    public void handle(TCPHandler h, SelectionKey sk, SocketChannel sc, ThreadPoolExecutor pool) throws IOException {
        // get message from message queue

        String str = "Your message has sent to "
                + sc.socket().getLocalSocketAddress().toString() + "\r\n";
        ByteBuffer buf = ByteBuffer.wrap(str.getBytes()); // wrap自动把buf的位置设为0，所以不需要再flip()

        while (buf.hasRemaining()) {
            sc.write(buf); // 回传给client回应字符串，发送buf的position位置 到limit位置为止之间的内容
        }

        h.setState(new ReadState()); // 改变状态(SENDING->READING)
        sk.interestOps(SelectionKey.OP_READ); // 通过key改变通道注册的事件
        sk.selector().wakeup(); // 使一个阻止的选择器立即返回
    }
}
