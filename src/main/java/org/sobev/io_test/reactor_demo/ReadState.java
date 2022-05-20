package org.sobev.io_test.reactor_demo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author luojx
 * @date 2022/5/12 14:37
 */
public class ReadState implements HandlerState {

    private SelectionKey sk;

    @Override
    public void changeState(TCPHandler h) {
        h.setState(new WorkState());
    }

    @Override
    public void handle(TCPHandler h, SelectionKey sk, SocketChannel sc, ThreadPoolExecutor pool) throws IOException {
        this.sk = sk;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        StringBuilder builder = new StringBuilder();
        while (sc.read(byteBuffer) > 0) {
            byteBuffer.flip();
            builder.append(new String(byteBuffer.array(), 0, byteBuffer.limit()));
            byteBuffer.clear();
        }
        String str = builder.toString();
        if (!str.equals(" ")) {
            h.setState(new WorkState()); // 改变状态(READING->WORKING)
            pool.execute(new WorkerThread(h, str)); // do process in worker thread
            System.out.println(sc.socket().getRemoteSocketAddress().toString()
                    + " > " + str);
        }
    }

    synchronized void process(TCPHandler h, String str) {
        // do process(decode, logically process, encode)..
        // ..
        h.setState(new WriteState()); // 改变状态(WORKING->SENDING)
        this.sk.interestOps(SelectionKey.OP_WRITE); // 通过key改变通道注册的事件
        this.sk.selector().wakeup(); // 使一个阻止的选择器立即返回
    }

    class WorkerThread implements Runnable {

        TCPHandler h;
        String str;

        public WorkerThread(TCPHandler h, String str) {
            this.h = h;
            this.str=str;
        }

        @Override
        public void run() {
            process(h, str);
        }

    }
}
