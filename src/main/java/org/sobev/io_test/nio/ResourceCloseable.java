package org.sobev.io_test.nio;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author luojx
 * @date 2022/8/27 14:37
 */
public class ResourceCloseable implements Closeable {

    public static void main(String[] args) throws IOException {
        try(ResourceCloseable rc = new ResourceCloseable()){
            System.out.println("handle");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        System.out.println("invoke close");
    }
}
