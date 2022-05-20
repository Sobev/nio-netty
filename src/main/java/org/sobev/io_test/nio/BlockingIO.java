package org.sobev.io_test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author luojx
 * @date 2022/5/11 10:21
 * @description NIO被叫为 no-blocking io，其实是在网络这个层次中理解的，
 * 对于FileChannel来说一样是阻塞。
 */
public class BlockingIO {
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
                blockingServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread clientThread = new Thread(() -> {
            try {
                blockingClient();
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

    public static void blockingClient() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080));
        FileChannel channel = FileChannel.open(Paths.get("C:\\Users\\DELL\\Downloads\\centos_install_v2ray2.sh"), StandardOpenOption.READ);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while (channel.read(byteBuffer) != -1) {
            //flip to read mode
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            //to write mode
            byteBuffer.clear();
        }
        socketChannel.shutdownOutput();

        //accept response
        int len;
        while ((len = socketChannel.read(byteBuffer)) != -1) {
            byteBuffer.flip();
            System.out.println(new String(byteBuffer.array(), 0, len));
            byteBuffer.clear();
        }

        //close
        channel.close();
        socketChannel.close();

    }

    public static void blockingServer() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        FileChannel fileChannel = FileChannel.open(Paths.get("uuid.sh"), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.READ);
        serverChannel.bind(new InetSocketAddress(8080));
        System.out.println("Server started");
        SocketChannel clientChannel = serverChannel.accept();
        System.out.println("Got client");
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while (clientChannel.read(byteBuffer) != -1) {
            byteBuffer.flip();
            fileChannel.write(byteBuffer);
            byteBuffer.clear();
        }
        //response client
        byteBuffer.put("image upload success".getBytes());
        byteBuffer.flip();
        clientChannel.write(byteBuffer);
        byteBuffer.clear();

        //close
        fileChannel.close();
        clientChannel.close();
        serverChannel.close();

    }

}
