package org.sobev.io_test.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @author luojx
 * @date 2022/5/12 10:06
 */
public class NioClient {

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                NioClient client = null;
                try {
                    client = new NioClient(15656);
                    client.communicateWithServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
//        NioClient client = new NioClient(15656);
//        client.communicateWithServer();
    }

    SocketChannel socketChannel;

    int port;

    public NioClient(int port) throws IOException {
        this.port = port;
        this.socketChannel = SocketChannel.open(new InetSocketAddress(port));
        //切换成非阻塞模式
        socketChannel.configureBlocking(false);
    }

    public void communicateWithServer() throws IOException {
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
        FileChannel channel = FileChannel.open(Paths.get("C:\\Users\\DELL\\Downloads\\centos_install_v2ray2.sh"), StandardOpenOption.READ);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while (channel.read(byteBuffer) > 0) {
            //flip to read mode
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            //to write mode
            byteBuffer.clear();
        }

        byteBuffer.put("EOF data sent finish".getBytes());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        //read response
        while (selector.select() > 0) {
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            //获取已“就绪”的事件，(不同的事件做不同的事)
            while (iter.hasNext()) {
                SelectionKey selectionKey = iter.next();
                //读事件就绪
                if (selectionKey.isReadable()) {
                    SocketChannel sc = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = sc.read(buffer);
                    if (read > 0) {
                        buffer.flip();
                        System.out.println(new String(buffer.array(), 0, read));
                        buffer.clear();
//                        buffer.put("Recved ur data".getBytes()); //造成无限重发
//                        buffer.flip();
//                        socketChannel.write(buffer);
                    }
                }
                iter.remove();
            }
        }

        channel.close();
        socketChannel.close();
    }
}
