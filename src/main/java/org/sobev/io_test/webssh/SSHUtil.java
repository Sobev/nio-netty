package org.sobev.io_test.webssh;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.StreamGobbler;

import java.io.*;
import java.util.Calendar;

/**
 * @author luojx
 * @date 2022/7/11 13:47
 * https://www.cnblogs.com/miracle-luna/p/12050367.html
 * https://blog.csdn.net/qq_36551991/article/details/106747832
 * https://blog.ops-coffee.cn/s/a3ejjvttuujzwyk21ntbqq
 * https://blog.ops-coffee.cn/s/gxhkc8rbhhjjf_whxd7j3w
 */
public class SSHUtil {

    public static void main(String[] args) throws Exception {
        String host = "120.77.18.179";
        String username = "root";
        String password = "";
        Connection connection = login(host, username, password);
        String execute = execute(connection, "ls -l");
        System.out.println("execute = " + execute);
    }


    public static Connection login(String host, String username, String password) throws Exception {
        Connection conn = null;
        long startTime = Calendar.getInstance().getTimeInMillis();
        try {
            conn = new Connection(host);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated) {
                System.out.println("Authenticated Successfully");
                return conn;
            } else {
                System.out.println("Authenticated Failed");
                return null;
            }
        } catch (Exception e) {

        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        System.out.println("登录用时: " + (endTime - startTime) / 1000.0 + "s\n");
        return conn;
    }

    public static String execute(Connection connection, String cmd) {
        String result = "";
        ch.ethz.ssh2.Session session = null;
        try {
            if (connection != null) {
                session = connection.openSession();
                session.execCommand(cmd);
                result = processStdout(session.getStdout(), "UTF-8");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
            if(connection != null){
                connection.close();
            }
        }
            return result;
    }
        private static String processStdout (InputStream in, String charset){
            InputStream stdout = new StreamGobbler(in);
            StringBuffer buffer = new StringBuffer();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout, charset));
                String line = null;
                while ((line = br.readLine()) != null) {
                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                System.err.println("解析脚本出错：" + e.getMessage());
                e.printStackTrace();
            }
            return buffer.toString();
        }

    }