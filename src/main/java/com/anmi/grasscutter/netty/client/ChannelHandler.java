package com.anmi.grasscutter.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
public class ChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        String commandStr = msg.toString();
        log.info("receive message: [{}]", commandStr);
        if (commandStr.startsWith("SERVER_COMMAND_MESSAGE::")) {
            String input = commandStr.replace("SERVER_COMMAND_MESSAGE::", "");
            MessageSend.setFlag(1, input);
        } else {
            MessageSend.setFlag(1, "");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("Inactive, try to conn");
        ctx.channel().close();
        MessageSend.disconnect(true);
    }
}
