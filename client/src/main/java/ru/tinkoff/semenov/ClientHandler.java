package ru.tinkoff.semenov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

    private Action currentAction;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        currentAction.handle(msg);
    }

    public void setAction (Action action) {
        this.currentAction = action;
    }
}
