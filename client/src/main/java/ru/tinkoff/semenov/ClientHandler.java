package ru.tinkoff.semenov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Обработчик ответов сервера.
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Текущая (или последняя выполненная) задача по обработке
     */
    private Action currentAction;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        currentAction.handle(msg);
    }

    public void setAction (Action action) {
        this.currentAction = action;
    }
}
