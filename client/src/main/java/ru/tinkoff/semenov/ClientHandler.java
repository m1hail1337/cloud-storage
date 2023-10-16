package ru.tinkoff.semenov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

    private final Callback onRequestReceivedCallback;

    public ClientHandler(Callback onRequestReceivedCallback) {
        this.onRequestReceivedCallback = onRequestReceivedCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (onRequestReceivedCallback != null) {
            onRequestReceivedCallback.callback(msg);
        }
    }
}
