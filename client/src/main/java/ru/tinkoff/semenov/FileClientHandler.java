package ru.tinkoff.semenov;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Хендлер, обрабатывающий байтовые данные поступающего файла
 */
public class FileClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * Папка, куда сохраняются полученные файлы
     */
    public static final String PATH_TO_DOWNLOADS = "client\\downloads";

    /**
     * Ссылка на стандартный хендлер работы со строковыми командами, при завершении файлового обмена переключимся на него
     */
    private final DefaultClientHandler defaultHandler;

    /**
     * Ожидаемый объем получаемого файла
     */
    private final long targetFileLength;

    /**
     * Получаемый файл записывается сюда
     */
    private final File file;

    /**
     * Флаг, говорящий о прекращении скачивания
     */
    private boolean isDownloadCanceled;

    /**
     * Помимо инициализации полей конструктор создает файл, куда будет вестись запись
     *
     * @param filename         имя скачиваемого файла
     * @param targetFileLength размер скачиваемого файла
     * @param defaultHandler   хендлер для работы со строковыми командами
     */
    public FileClientHandler(String filename, long targetFileLength, DefaultClientHandler defaultHandler) {

        this.targetFileLength = targetFileLength;
        this.file = new File(PATH_TO_DOWNLOADS + "\\" + filename);
        this.defaultHandler = defaultHandler;

        try {
            Files.createFile(Path.of(PATH_TO_DOWNLOADS + "\\" + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод считывания поступающих данных.
     *
     * @param ctx текущий контекст канала
     * @param msg переданные байты
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {

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

        if (file.length() == targetFileLength) {
            switchToDefaultHandler(ctx);
        }
    }

    /**
     * При ошибке пробуем переключиться обратно в режим строковых команд, установив флаг отмены скачивания
     *
     * @param ctx   текущий контекст канала
     * @param cause - ошибка
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        isDownloadCanceled = true;
        switchToDefaultHandler(ctx);
        throw new RuntimeException("Ошибка в получении файла" + file.getName(), cause);
    }

    /**
     * Если одно из стоп-значений принято мы возвращаем в конвейер строковые кодеры/декодеры и
     * текущий хендлер меняем обратно на стандартный. Если отправка завершена некорректно - удаляем файл.
     *
     * @param ctx текущий контекст канала
     */
    private void switchToDefaultHandler(ChannelHandlerContext ctx) {
        ctx.pipeline().addFirst("stringEncoder", new StringEncoder());
        ctx.pipeline().addFirst("stringDecoder", new StringDecoder());
        ctx.pipeline().replace("fileHandler", "defaultHandler", defaultHandler);

        if (isDownloadCanceled) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setDownloadCanceled(boolean downloadCanceled) {
        isDownloadCanceled = downloadCanceled;
    }

    public long getTargetFileLength() {
        return targetFileLength;
    }

    public File getFile() {
        return file;
    }
}
