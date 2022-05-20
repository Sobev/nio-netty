package org.sobev.io_test.rpc.registry;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.sobev.io_test.rpc.protocol.InvokerProtocol;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luojx
 * @date 2022/5/19 17:27
 */
@ChannelHandler.Sharable
public class RegistryHandler extends ChannelInboundHandlerAdapter {

    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<>();

    private static List<String> classNames = new ArrayList<>();

    public RegistryHandler() {
        scanClass("org.sobev.io_test.rpc.provider");
        doRegister();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InvokerProtocol req = (InvokerProtocol) msg;
        Object result = new Object();
        if (registryMap.containsKey(req.getClassName())) {
            Object clazz = registryMap.get(req.getClassName());
            Method method = clazz.getClass().getMethod(req.getMethodName(), req.getParamTypes());
            result = method.invoke(clazz, req.getParams());
            System.out.println("result:" + result);
        }
        ctx.writeAndFlush(result);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(cause.getMessage());
    }

    private void scanClass(String packageName) {
        URL resource = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        if (resource != null) {
            File file = new File(resource.getFile());
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                if (listFile.isDirectory()) {
                    scanClass(packageName + "." + listFile.getName());
                } else {
                    classNames.add(packageName + "." + listFile.getName().replace(".class", "").trim());
                }
            }
        }
    }

    private void doRegister() {
        if (classNames.size() == 0)
            return;
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> anInterface = clazz.getInterfaces()[0];
                System.out.println("putting interface:" + anInterface.getName());
                registryMap.put(anInterface.getName(), clazz.newInstance());

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
