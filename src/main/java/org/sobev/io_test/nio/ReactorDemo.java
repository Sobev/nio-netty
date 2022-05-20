package org.sobev.io_test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author luojx
 * @date 2022/5/11 16:55
 */
public class ReactorDemo {
    private final Selector[] selectors = new Selector[2];
    private final ServerSocketChannel serverSocketChannel;
    Reactor[] reactors = new Reactor[2];
    SelectionKey sk;

    public ReactorDemo() throws IOException {
        //创建两个选择器
        selectors[0] = Selector.open();
        selectors[1] = Selector.open();
        //创建nio服务端
        serverSocketChannel = ServerSocketChannel.open();
        //绑定端口
        serverSocketChannel.bind(new InetSocketAddress(18010));
        //将接收事件注册到选择器上,OP_ACCEPT表示注册接收事件,一个选择器处理请求事件，另一个选择器处理IO读写事件
        sk = serverSocketChannel.register(selectors[0], SelectionKey.OP_ACCEPT);
        //设置回调函数
        sk.attach(new AcceptorDemo());
        //初始化反应器
        reactors[0] = new Reactor(selectors[0]);
        reactors[1] = new Reactor(selectors[1]);

    }

    class Reactor implements Runnable {

        //每个线程负责一个selector
        private final Selector selector;

        public Reactor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> selected = selector.selectedKeys();
                    Iterator<SelectionKey> it = selected.iterator();
                    while (it.hasNext()) {
                        //事件准备就绪，分发到对应的handler进行处理
                        dispatch((SelectionKey) (it.next()));
                    }
                    selected.clear();
                }
            } catch (IOException ex) {
            }
        }

    }

    public void dispatch(SelectionKey sk) {
        //调用之间注册得到对象，域之前的attach对应
        Runnable r = (Runnable) (sk.attachment());
        //调用之前注册的callback对象
        if (r != null) {
            r.run();
        }
    }

    class AcceptorDemo implements Runnable {

        @Override
        public void run() {
            //创建连接
            SocketChannel channel;
            try {
                channel = serverSocketChannel.accept();
                if (channel != null)
                    new Handler(selectors[0], channel);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    class Handler implements Runnable {
        ExecutorService pool = new ThreadPoolExecutor(
                5,
                10,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy()
        );

        public Handler(Selector selector, SocketChannel channel) throws IOException {
            channel.configureBlocking(false);
            sk = channel.register(selectors[1], 0);
            //将Handler作为callback对象
            sk.attach(this);
            //第二步,注册Read就绪事件
            sk.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }

        public void handler() {
            read();
            write();
        }

        public void read() {
        }

        ;

        public void write() {
        }

        @Override
        public void run() {
            pool.execute(new Task());
        }

        class Task implements Runnable {

            @Override
            public void run() {
                Handler.this.handler();
            }
        }
    }
}
