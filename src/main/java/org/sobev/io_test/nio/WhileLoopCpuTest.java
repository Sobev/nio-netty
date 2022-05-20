package org.sobev.io_test.nio;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

/**
 * @author luojx
 * @date 2022/5/11 13:46
 */
public class WhileLoopCpuTest {
    private static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static void main(String[] args) throws InterruptedException {
        while (true){
            System.out.println(osmxb.getSystemCpuLoad());
            //cpu时间片切换
            Thread.sleep(1);
        }
    }
}
