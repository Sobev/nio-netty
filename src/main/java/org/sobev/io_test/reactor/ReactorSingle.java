package org.sobev.io_test.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author luojx
 * @date 2022/5/12 8:51
 */
public class ReactorSingle implements Runnable {
    private final ServerSocketChannel ssc;
    private final Selector selector;

    public ReactorSingle(int port) throws IOException {
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
                System.out.println("readyOpsChannel = " + readyOpsChannel);
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
            System.out.println("dispatching to certain attachment");
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
                System.out.println(Thread.currentThread().getName() + " attaching handler for new client socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class TCPHandler implements Runnable {
        private final SocketChannel clientChannel;

        private final SelectionKey selectionKey;

        int state;

        public TCPHandler(SocketChannel clientChannel, SelectionKey selectionKey) {
            this.clientChannel = clientChannel;
            this.selectionKey = selectionKey;
            this.state = 0;
        }


        @Override
        public void run() {
            try {
                if (state == 0) {
                    read();
                } else {
                    send();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                closeChannel();
            }
        }

        private synchronized void read() throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            StringBuilder builder = new StringBuilder();
            while (clientChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                builder.append(new String(byteBuffer.array(), 0, byteBuffer.limit()));
                byteBuffer.clear();
            }
            process(builder);
            System.out.println(clientChannel.socket().getRemoteSocketAddress().toString()
                    + " > ");
            state = 1;
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            selectionKey.selector().wakeup();
        }

        private void send() throws IOException {
            String str = Thread.currentThread().getName() + " Your message has sent to "
                    + clientChannel.socket().getLocalSocketAddress().toString() + "\r\n";
            ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes());
            clientChannel.write(byteBuffer);

            state = 0;
            selectionKey.interestOps(SelectionKey.OP_READ);
            selectionKey.selector().wakeup();
        }

        private void process(StringBuilder builder) {
            System.out.println("processing data!!!");
            System.out.println(builder);
        }

        private void closeChannel() {
            try {
                selectionKey.cancel();
                clientChannel.close();
            } catch (IOException e1) {
                System.err.println(e1.getMessage());
            }

        }

    }

    public static void main(String[] args) {
        try {
            ReactorSingle reactorFunc = new ReactorSingle(15656);
            reactorFunc.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
