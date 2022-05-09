package com.anmi.grasscutter.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        String commandStr = msg.toString();
        if (commandStr.startsWith("SERVER_COMMAND_MESSAGE::")) {
            String input = commandStr.replace("SERVER_COMMAND_MESSAGE::", "");
            System.out.println("有消息了，" + input);
            MessageSend.setFlag(1, input);
        }
    }
}
