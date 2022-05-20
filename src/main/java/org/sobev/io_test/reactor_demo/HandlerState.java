package org.sobev.io_test.reactor_demo;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author luojx
 * @date 2022/5/12 14:37
 */
public interface HandlerState {
    public void changeState(TCPHandler h);

    public void handle(TCPHandler h, SelectionKey sk, SocketChannel sc,
                       ThreadPoolExecutor pool) throws IOException;
}
