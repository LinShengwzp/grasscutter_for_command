package com.anmi.grasscutter.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author wzp12
 */
@Slf4j
@Component("MessageSend")
public class MessageSend {

    private static final EventLoopGroup GROUP = new NioEventLoopGroup(2);
    private static String HOST = "localhost";
    private static int PORT = 8824;
    private static final int readerIdleTime = 5;

    private static Channel channel;
    public static final Object WAITING_OBJECT = new Object();
    public static final Object RECONNECT_OBJECT = new Object();
    private static int resCode = 0;
    private static int flag = 0;
    private static boolean connSuccess = false;
    private static String msg = "";

    @Value("${server.listen-host}")
    public void setHost(String host) {
        MessageSend.HOST = host;
        init();
    }

    @Value("${server.listen-port}")
    public void setPort(int port) {
        MessageSend.PORT = port;
        init();
    }

    private void init() {
        flag++;
        if (flag == 2) {
            conn();
            flag = 0;
        }
    }

    public void conn() {
        new Thread(() -> {
            synchronized (MessageSend.RECONNECT_OBJECT) {
                try {
                    while (true) {
                        if (!MessageSend.connSuccess) {
                            new Thread(() -> this.connect(HOST, PORT)).start();
                            MessageSend.RECONNECT_OBJECT.wait();
                        } else {
                            MessageSend.RECONNECT_OBJECT.wait();
                        }
                        log.warn("try to reconnect the server");
                        Thread.sleep(readerIdleTime * 1000);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * 连接服务端
     *
     * @param host 地址
     * @param port 端口
     */
    private void connect(String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(GROUP)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        //读超时时间设置为10s，0表示不监控
                        socketChannel.pipeline().addLast(new IdleStateHandler(readerIdleTime, 0, 0, TimeUnit.SECONDS));
                        //处理字符串
                        socketChannel.pipeline().addLast(new StringDecoder());
                        //自定义的处理逻辑
                        socketChannel.pipeline().addLast(new ChannelHandler());
                    }
                });
        try {
            // 发起异步连接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            MessageSend.connSuccess = true;
            log.info("connect the server success on");
        } catch (Exception e) {
            e.printStackTrace();
            disconnect(true);
        }
    }

    public static void setFlag(int newVal, String msg) {
        synchronized (MessageSend.WAITING_OBJECT) {
            MessageSend.resCode = newVal;
            MessageSend.msg = msg;
            MessageSend.WAITING_OBJECT.notify();
        }
    }

    public String sendCommand(String command) {
        synchronized (MessageSend.WAITING_OBJECT) {
            String commandFormat = MessageFormat.format("SERVER_COMMAND::{0}", command);
            channel.writeAndFlush(Unpooled.wrappedBuffer(commandFormat.getBytes()));
            try {
                MessageSend.WAITING_OBJECT.wait();
                return MessageSend.msg;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void disconnect(boolean needReConnect) {
        synchronized (MessageSend.RECONNECT_OBJECT) {
            try {
                log.error("disconnect the server");
                if (Objects.nonNull(channel)) {
                    channel.closeFuture().sync();
                }
                // GROUP.shutdownGracefully();
                MessageSend.connSuccess = false;
                if (needReConnect) {
                    MessageSend.RECONNECT_OBJECT.notify();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
