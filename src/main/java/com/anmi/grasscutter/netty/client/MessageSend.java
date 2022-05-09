package com.anmi.grasscutter.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Slf4j
@Component
public class MessageSend {

    private static final EventLoopGroup group = new NioEventLoopGroup(2);

    //    private static final String HOST = "43.138.56.77";
    private static final String HOST = "localhost";
    private static final int PORT = 8824;

    private static Channel channel;

    public MessageSend() {
        this.connect(HOST, PORT);
    }

    /**
     * 连接服务端
     *
     * @param host 地址
     * @param port 端口
     */
    private void connect(String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int flag = 0;
    private static String msg = "";

    public static void setFlag(int newVal, String msg) {
        MessageSend.flag = newVal;
        MessageSend.msg = msg;
    }

    public static String sendCommand(String command) {
        String commandFormat = MessageFormat.format("SERVER_COMMAND::{0}", command);
        channel.writeAndFlush(Unpooled.wrappedBuffer(commandFormat.getBytes()));
        int flagCopy = MessageSend.flag;
        int times = 0, maxTimes = 10;
        try {
            while (times <= maxTimes) {
                Thread.sleep(500);
                times ++;
                if (flagCopy == (MessageSend.flag + 1)) {
                    MessageSend.flag = 0;
                    return MessageSend.msg;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public static void disconnect() throws InterruptedException {
        channel.closeFuture().sync();
        group.shutdownGracefully();
    }
}
