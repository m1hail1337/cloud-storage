package ru.tinkoff.semenov;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Обработчик ответов сервера. Аннотация {@link Sharable} позволяет сохранить текущий хендлер и переключится на него
 * после загрузки файла.
 */
@Sharable
public class DefaultClientHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Текущая (или последняя выполненная) задача по обработке
     */
    private Action currentAction;

    /**
     * Метод обрабатывает полученное сообщение в зависимости от текущего действия ({@link Action}) пользователя
     * @param ctx текущий контекст канала передачи
     * @param msg принятое сообщение
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        currentAction.handle(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        throw new RuntimeException("Ошибка у пользователя", cause);
    }

    public void setAction (Action action) {
        this.currentAction = action;
    }
}
