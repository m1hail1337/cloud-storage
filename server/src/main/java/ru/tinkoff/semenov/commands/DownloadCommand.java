package ru.tinkoff.semenov.commands;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedFile;
import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.File;

/**
 * Команда для скачивания файлов с сервера. Имеет ссылку на стандартный хендлер для того, чтобы отправлять файл в
 * текущий канал и знать какой файл отправлять.
 */
public class DownloadCommand implements Command {

    /**
     * Хендлер строковых команд
     */
    private final MainHandler handler;

    public DownloadCommand(MainHandler handler) {
        this.handler = handler;
    }

    /**
     * Отправляем чанк файла, пока не достигли его конца. Команда выполняется в отдельном потоке
     * @param args у этой команды нет аргументов
     * @return пустую строку ({@link Response#EMPTY})
     */
    @Override
    public String execute(String args) {
        Channel channel = handler.getContext().channel();
        File file = handler.getCurrentDownloadFile();
        new Thread(() -> {
            try {
                ChunkedFile chunkedFile = new ChunkedFile(file);

                while (!chunkedFile.isEndOfInput()) {
                    channel.writeAndFlush(chunkedFile.readChunk(ByteBufAllocator.DEFAULT));
                }

                chunkedFile.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        return Response.EMPTY.name();
    }
}
