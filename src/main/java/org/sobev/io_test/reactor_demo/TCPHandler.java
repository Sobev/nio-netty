package org.sobev.io_test.reactor_demo;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author luojx
 * @date 2022/5/12 14:36
 */
public class TCPHandler implements Runnable {
    private final SelectionKey sk;
    private final SocketChannel sc;
    private static final int THREAD_COUNTING = 10;
    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNTING, THREAD_COUNTING, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()); // 线程池

    HandlerState state; // 以状态模式实现Handler

    public TCPHandler(SelectionKey sk, SocketChannel sc) {
        this.sk = sk;
        this.sc = sc;
        state = new ReadState(); // 初始状态设定为READING
        pool.setMaximumPoolSize(32); // 解决线程池数
    }

    @Override
    public void run() {
        try {
            state.handle(this, sk, sc, pool);

        } catch (IOException e) {
            System.out.println("[Warning!] A client has been closed.");
            closeChannel();
        }
    }

    public void closeChannel() {
        try {
            sk.cancel();
            sc.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void setState(HandlerState state) {
        this.state = state;
    }
}
