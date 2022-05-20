package org.sobev.io_test.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author luojx
 * @date 2022/5/12 13:26
 */
public class ReactorMultiThread implements Runnable {
    private final ServerSocketChannel ssc;
    private final Selector selector;

    public static void main(String[] args) throws IOException {
        ReactorMultiThread reactorMultiThread = new ReactorMultiThread(15656);
        reactorMultiThread.run();
    }

    public ReactorMultiThread(int port) throws IOException {
        this.selector = Selector.open();
        this.ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(port));
        SelectionKey sk = ssc.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor(ssc, selector));
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            System.out.println("Waiting for new event on port: " + ssc.socket().getLocalPort() + "...");
            try {
                //blocking
                int readyOpsChannel = selector.select();
                if (readyOpsChannel == 0)
                    continue;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey selectionKey = iter.next();
                dispatch(selectionKey);
                iter.remove();
            }
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        //attachment有Acceptor和TCPHandler
        Runnable r = (Runnable) selectionKey.attachment();

        if (r != null) {
            r.run();
        } else {
            System.err.println("attachment is null!!! check it");
        }
    }

    static class Acceptor implements Runnable {

        private final ServerSocketChannel ssc;

        private final Selector selector;

        public Acceptor(ServerSocketChannel ssc, Selector selector) {
            this.ssc = ssc;
            this.selector = selector;
        }

        @Override
        public void run() {
            try {
                SocketChannel clientChannel = ssc.accept();
                System.out.println(clientChannel.socket().getRemoteSocketAddress().toString() + " is connected.");

                clientChannel.configureBlocking(false);
                SelectionKey selectionKey = clientChannel.register(selector, SelectionKey.OP_READ);
                selector.wakeup();
                selectionKey.attach(new TCPHandler(clientChannel, selectionKey));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class TCPHandler implements Runnable {

        private final SocketChannel socketChannel;

        private final SelectionKey selectionKey;

        private int state;

        private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(
                7,
                7,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("tcpHandlerPool-" + new Random().nextInt());
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        public TCPHandler(SocketChannel socketChannel, SelectionKey selectionKey) {
            this.socketChannel = socketChannel;
            this.selectionKey = selectionKey;
            state = 0;
        }

        @Override
        public void run() {
            try {
                if (getState() == 0) {
                    read();
                    pool.execute(() -> {
                        try {
                            process();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    send();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                closeChannel();
            }
        }

        private synchronized void setState(int state) {
            this.state = state;
        }

        private synchronized int getState() {
            return state;
        }

        private void read() throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            StringBuilder builder = new StringBuilder();
            while (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                builder.append(new String(byteBuffer.array(), 0, byteBuffer.limit()));
                byteBuffer.clear();
            }
            System.out.println(socketChannel.socket().getRemoteSocketAddress().toString()
                    + " > data");
        }

        private void send() throws IOException {
            System.out.println(Thread.currentThread().getName() + " sending message!!!");
            String str = Thread.currentThread().getName() + " Your message has sent to "
                    + socketChannel.socket().getLocalSocketAddress().toString() + "\r\n";
            ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes());
            socketChannel.write(byteBuffer);

            setState(0);
            selectionKey.interestOps(SelectionKey.OP_READ);
//            selectionKey.selector().wakeup();
        }

        private void process() {
            setState(1);
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            System.out.println(Thread.currentThread().getName() + " processing data...");
//            selectionKey.selector().wakeup();
        }

        private void closeChannel() {
            try {
                selectionKey.cancel();
                socketChannel.close();
            } catch (IOException e1) {
                System.err.println(e1.getMessage());
            }

        }
    }
}
