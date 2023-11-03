package ru.tinkoff.semenov;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Хендлер, обрабатывающий байтовые данные файла
 */
public class FilesIOHandler extends ChannelInboundHandlerAdapter {

    /**
     * Ссылка на дефолтный хендлер, который будет возвращен в конвейер
     */
    private final MainHandler defaultHandler;
    /**
     * Получаемый файл
     */
    private final File file;

    private final long fileLength;
    /**
     * Путь к получаемому файлу
     */
    private final String path;
    /**
     * Флаг некорректной остановки загрузки
     */
    private boolean isLoadCanceled = false;

    public FilesIOHandler(String path, long fileLength, MainHandler defaultHandler) {
        this.path = path;
        this.file = new File(path);
        this.defaultHandler = defaultHandler;
        this.fileLength = fileLength;
    }

    /**
     * Метод считывания поступающих данных.
     * @param ctx текущий контекст канала
     * @param msg переданные байты
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        ByteBuffer nioBuffer = ((ByteBuf) msg).nioBuffer();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel channel = raf.getChannel();
        while (nioBuffer.hasRemaining()) {
            channel.position(raf.length());
            channel.write(nioBuffer);
        }
        byteBuf.release();
        channel.close();
        raf.close();

        if (file.length() == fileLength) {
            switchToDefaultHandler(ctx);
        }
    }

    /**
     * При ошибке пробуем переключиться обратно в режим строковых команд, удалив временный файл
     * @param ctx текущий контекст канала
     * @param cause - ошибка
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        isLoadCanceled = true;
        switchToDefaultHandler(ctx);
        throw new RuntimeException("Ошибка в получении файла", cause);
    }

    /**
     * Если одно из стоп-значений принято мы возвращаем в конвейер строковые кодеры/декодеры и
     * текущий хендлер меняем обратно на стандартный. Если отправка завершена некорректно - удаляем файл.
     * @param ctx текущий контекст канала
     */
    private void switchToDefaultHandler(ChannelHandlerContext ctx) {
        ctx.pipeline().addFirst("stringEncoder", new StringEncoder());
        ctx.pipeline().addFirst("stringDecoder", new StringDecoder());
        ctx.pipeline().replace("fileHandler", "defaultHandler", defaultHandler);

        if (isLoadCanceled) {
            MainHandler.getCommands().get("DELETE").execute(path);
        }
        ctx.writeAndFlush("LOADED");
    }
}
