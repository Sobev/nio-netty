package org.sobev.io_test.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author luojx
 * @date 2022/5/11 9:03
 */
public class FileTransferTest {

    public static void main(String[] args) throws IOException {
        File src = new File("C:\\Users\\DELL\\Downloads\\vultr.com.100MB.bin");
        File ioDst = new File("C:\\Users\\DELL\\Downloads\\ioCopy.bin");
        File nioDst = new File("C:\\Users\\DELL\\Downloads\\NioCopy.bin");
        ioFileTransfer(src, ioDst);
        nioFileTransfer(src, nioDst);
    }

    /**
    * @description 普通io
    */
    public static void ioFileTransfer(File src, File dst) throws IOException {
        long start = System.currentTimeMillis();
        if(!dst.exists()){
            dst.createNewFile();
        }
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dst));
        byte[] buffer = new byte[1024*1024];
        int len;
        while ((len = bis.read(buffer)) != -1){
            bos.write(buffer, 0, len);
        }
        long end = System.currentTimeMillis();
        System.out.println("io Time Consume: " + (end - start));

    }

    /**
    * @description nio
    */
    public static void nioFileTransfer(File src, File dst) throws IOException {
        long start = System.currentTimeMillis();
        if(!dst.exists()){
            dst.createNewFile();
        }
        RandomAccessFile read = new RandomAccessFile(src, "r");
        RandomAccessFile write = new RandomAccessFile(dst, "rw");
        FileChannel readChannel = read.getChannel();
        FileChannel writeChannel = write.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
        while (readChannel.read(byteBuffer) > 0){
            byteBuffer.flip();
            writeChannel.write(byteBuffer);
            byteBuffer.clear();
        }
        writeChannel.close();
        readChannel.close();
        long end = System.currentTimeMillis();
        System.out.println("Nio Time Consume: " + (end - start));
    }

    /**
    * @description 直接缓冲区（内存映射文件）
    */
    public static void memoryMappedFile() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("C:\\Users\\DELL\\Downloads\\vultr.com.100MB.bin"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(
                Paths.get("C:\\Users\\DELL\\Downloads\\vultr.com.100MB.bin"),
                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
        MappedByteBuffer inMappedByteBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer outMappedByteBuffer = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());
        byte[] buf = new byte[inMappedByteBuffer.limit()];
        inMappedByteBuffer.get(buf);
        outMappedByteBuffer.put(buf);
    }

    /**
    * @description 通道之间数据传输（直接缓冲区）
    */
    public static void channelTransfer() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("C:\\Users\\DELL\\Downloads\\vultr.com.100MB.bin"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(
                Paths.get("C:\\Users\\DELL\\Downloads\\vultr.com.100MB.bin"),
                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
        inChannel.transferTo(0, inChannel.size(), outChannel);

        outChannel.close();
        inChannel.close();
    }
}
