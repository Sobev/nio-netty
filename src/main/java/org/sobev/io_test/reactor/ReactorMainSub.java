package org.sobev.io_test.reactor;

import org.sobev.io_test.reactor_demo.TCPHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author luojx
 * @date 2022/5/12 16:41
 */
public class ReactorMainSub implements Runnable {

    public static void main(String[] args) {
        try {
            ReactorMainSub reactorMainSub = new ReactorMainSub(15656);
            new Thread(reactorMainSub).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final ServerSocketChannel ssc;
    private final Selector selector;//主reactor，接收accept

    public ReactorMainSub(int port) throws IOException {
        this.ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(port));
        this.selector = Selector.open();
        //ssc向主selector注册一个OP_ACCEPT事件，返回通道的key
        SelectionKey selectionKey = ssc.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor(ssc));
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

                //依据selectionKey的attachment保存的对象不同而执行不同的润方法，
                // attachment保存了一个实现了Runnable接口的对象，
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

    /**
     * @description 接受accept事件
     */
    static class Acceptor implements Runnable {
        private final ServerSocketChannel ssc;
        private final int core = Runtime.getRuntime().availableProcessors();
        private final Selector[] selectors = new Selector[core];
        private int selIdx = 0; //当前的selector下标
        private SubReactor[] subReactors = new SubReactor[core];
        private Thread[] threads = new Thread[core];

        Acceptor(ServerSocketChannel ssc) throws IOException {
            this.ssc = ssc;
            for (int i = 0; i < core; i++) {
                selectors[i] = Selector.open();
                subReactors[i] = new SubReactor(ssc, selectors[i], i);
                threads[i] = new Thread(subReactors[i]);
                threads[i].start();
            }
        }

        @Override
        public synchronized void run() {
            try {
                SocketChannel socketChannel = ssc.accept();
                System.out.println(socketChannel.socket().getRemoteSocketAddress().toString()
                        + " is connected.");

                socketChannel.configureBlocking(false);
                subReactors[selIdx].setRestart(true);
                selectors[selIdx].wakeup();
                // socketChannel 向第i个selector注册OP_READ事件，返回sk
                SelectionKey selectionKey = socketChannel.register(selectors[selIdx], SelectionKey.OP_READ);
                selectors[selIdx].wakeup();
                subReactors[selIdx].setRestart(false);
                selectionKey.attach(new TCPHandler(selectionKey, socketChannel));

                if (++selIdx == core) {
                    selIdx = 0;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
    * @description 多个SubSelector类在Thread执行，监听不同的selectedKeys事件
    */
    static class SubReactor implements Runnable {
        private final ServerSocketChannel ssc;
        private final Selector selector;
        private int num;
        private boolean restart = false;

        SubReactor(ServerSocketChannel ssc, Selector selector, int num) {
            this.ssc = ssc;
            this.selector = selector;
            this.num = num;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
//                System.out.println("ID:" + num
//                      + " subReactor waiting for new event on port: "
//                      + ssc.socket().getLocalPort() + "...");
                System.out.println(num + " :waiting for restart");
                while (!Thread.interrupted() && !restart) {
                    try {
                        if (selector.select() == 0)
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
        }

        public void setRestart(boolean restart) {
            this.restart = restart;
        }

        private void dispatch(SelectionKey key) {
            //key.attachment() == new TCPHandler(selectionKey, socketChannel)
            Runnable r = (Runnable) (key.attachment()); // 根据事件之key绑定的对象开新线程
            if (r != null)
                r.run();
        }
    }
}
