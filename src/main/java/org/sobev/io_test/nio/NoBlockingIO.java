package org.sobev.io_test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @author luojx
 * @date 2022/5/11 10:53
 */
public class NoBlockingIO {
    /**
     * Buffer缓冲区
     * Channel通道
     * Selector选择器
     * <p>
     * ###
     * Selector选择器就可以比喻成麦当劳的广播。
     * 一个线程能够管理多个Channel的状态
     */

    public static void main(String[] args) throws IOException {
        Thread serverThread = new Thread(() -> {
            try {
                noBlockingIOServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread clientThread = new Thread(() -> {
            try {
                noBlockingIOClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
        clientThread.start();
        try {
            serverThread.join();
            clientThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void noBlockingIOClient() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(8080));
        //切换成非阻塞模式
        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
        FileChannel channel = FileChannel.open(Paths.get("C:\\Users\\DELL\\Downloads\\centos_install_v2ray2.sh"), StandardOpenOption.READ);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while (channel.read(byteBuffer) != -1) {
            //flip to read mode
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            //to write mode
            byteBuffer.clear();
        }
        //read response
        while (selector.select() > 0){
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            //获取已“就绪”的事件，(不同的事件做不同的事)
            while (iter.hasNext()){
                SelectionKey selectionKey = iter.next();
                //读事件就绪
                if(selectionKey.isReadable()){
                    SocketChannel sc = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = sc.read(buffer);
                    if(read > 0){
                        buffer.flip();
                        System.out.println(new String(buffer.array(), 0, read));
                    }
                }
                iter.remove();
            }
        }

        channel.close();
        socketChannel.close();
    }

    public static void noBlockingIOServer() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8080));

        Selector selector = Selector.open();
        //返回值 SelectionKey记录了通道和选择器之间的关系
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        //轮询地获取选择器上已“就绪”的事件--->只要select()>0，说明已就绪
        while (selector.select() > 0){
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            //获取已“就绪”的事件，(不同的事件做不同的事)
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                //接收事件就绪
                if(selectionKey.isAcceptable()){
                    SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                }else if(selectionKey.isReadable()){
                    //获取当前选择器读就绪状态的通道
                    SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    FileChannel fileChannel = FileChannel.open(Paths.get("niouuid.sh"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    while (clientChannel.read(byteBuffer) != -1){
                        byteBuffer.flip();
                        fileChannel.write(byteBuffer);
                        byteBuffer.clear();
                    }
                    byteBuffer.put("img received...".getBytes());
                    byteBuffer.flip();
                    clientChannel.write(byteBuffer);
                    byteBuffer.clear();
                }
                //取消选择键(已经处理过的事件，就应该取消掉了)
                iterator.remove();
            }
        }

    }
}
