package com.anmi.grasscutter.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

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
        } else {
            MessageSend.setFlag(1, "");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {//超时事件
            IdleStateEvent idleEvent = (IdleStateEvent) evt;
            if (idleEvent.state() == IdleState.READER_IDLE) {//读
                ctx.channel().close();
            } else if (idleEvent.state() == IdleState.WRITER_IDLE) {//写

            } else if (idleEvent.state() == IdleState.ALL_IDLE) {//全部

            }
        }
        super.userEventTriggered(ctx, evt);
    }

}
