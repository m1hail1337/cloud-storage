package ru.tinkoff.semenov;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import ru.tinkoff.semenov.enums.Command;

/**
 * Основной класс сетевого взаимодействия с сервером. Здесь настраивается соединение, устанавливаются
 * обработчики ответов, выполняется отправка команд на сервер.
 * <br>*ВАЖНО*: даже в команду без аргументов необходимо добавить {@link Network#SEPARATOR}!
 */
public class Network {
    /**
     * Разделитель между командами и её аргументами
     */
    public static final String SEPARATOR = "|";
    /**
     * Хост сервера
     */
    private static final String HOST = "localhost";
    /**
     * Порт подключения к серверу
     */
    private static final int PORT = 8189;
    /**
     * Канал соединения с сервером по которому идет взаимодействие
     */
    private SocketChannel channel;
    /**
     * Обработчик сообщений клиента
     */
    private final DefaultClientHandler defaultHandler = new DefaultClientHandler();
    private FileClientHandler fileHandler;
    private boolean loadCanceled;


    public Network() {
        Thread t = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                channel = socketChannel;
                                ChannelPipeline pipeline = socketChannel.pipeline();

                                pipeline.addLast("stringDecoder", new StringDecoder());
                                pipeline.addLast("stringEncoder", new StringEncoder());
                                pipeline.addLast(new ChunkedWriteHandler());
                                pipeline.addLast("defaultHandler", defaultHandler);
                            }
                        });
                ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        t.start();
    }

    /**
     * Отправка на сервер команды для регистрации нового пользователя
     *
     * @param login    логин нового пользователя
     * @param password пароль нового пользователя
     */
    public void register(String login, String password) {
        channel.writeAndFlush(Command.REGISTER.name() + SEPARATOR + login.length() + login + password);
    }

    /**
     * Отправка на сервер команды авторизации
     *
     * @param login    логин пользователя
     * @param password пароль пользователя
     */
    public void authorize(String login, String password) {
        channel.writeAndFlush(Command.AUTH.name() + SEPARATOR + login.length() + login + password);
    }

    /**
     * Отправка на сервер команды перемещения (вырезки) файла
     *
     * @param file        название файла (который вырезаем)
     * @param destination точка назначение (куда вставляем)
     */
    public void cutFile(String file, String destination) {
        channel.writeAndFlush(Command.CUT.name() + SEPARATOR + file + SEPARATOR + destination);
    }

    /**
     * Отправка на сервер команды копирования файла
     *
     * @param file        название файла (который вырезаем)
     * @param destination точка назначение (куда вставляем)
     */
    public void copyFile(String file, String destination) {
        channel.writeAndFlush(Command.COPY.name() + SEPARATOR + file + SEPARATOR + destination);
    }

    /**
     * Отправка на сервер файла пользователя
     *
     * @param file        файл для передачи
     * @param destination путь к папке, где файл будет сохранен на сервере
     */
    public void loadFile(ChunkedFile file, String destination) {
        channel.writeAndFlush(Command.LOAD.name() + SEPARATOR + destination + SEPARATOR + file.length());

        new Thread(() -> {
            try {
                while (!file.isEndOfInput() && !loadCanceled) {
                    channel.writeAndFlush(file.readChunk(ByteBufAllocator.DEFAULT));
                }
                file.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Отправляем запрос на получение файла с сервера, конкретно размер файла.
     * @param filePath путь к файлу
     */
    public void downloadRequest(String filePath) {
        channel.writeAndFlush(Command.FILE_LENGTH.name() + SEPARATOR + filePath);
    }

    /**
     * При успешном получении размера файла проводим манипуляции с конвейером обработчиков для настройки его на получение
     * файловых байтов и отправляем команду, которая начинает отправку фрагментов файла.
     * @param filename имя скачиваемого файла
     * @param fileLength размер скачиваемого файла (в байтах)
     */
    public void downloadFile(String filename, long fileLength) {
        fileHandler = new FileClientHandler(filename, fileLength, defaultHandler);
        channel.writeAndFlush(Command.DOWNLOAD.name() + SEPARATOR);

        ChannelPipeline pipeline = channel.pipeline();
        pipeline.remove("stringEncoder");
        pipeline.remove("stringDecoder");
        pipeline.replace("defaultHandler", "fileHandler", fileHandler);
    }

    /**
     * Отправка на сервер команды создания новой директории
     *
     * @param path путь к новой директории
     */
    public void createNewDirectory(String path) {
        channel.writeAndFlush(Command.NEW_DIR.name() + SEPARATOR + path);
    }

    /**
     * Отправка на сервер команды удаления файла
     *
     * @param path путь к файлу (который хотим удалить)
     */
    public void deleteFile(String path) {
        channel.writeAndFlush(Command.DELETE.name() + SEPARATOR + path);
    }

    public void close() {
        channel.close();
    }

    public DefaultClientHandler getDefaultHandler() {
        return defaultHandler;
    }

    public FileClientHandler getFileHandler() {
        return fileHandler;
    }

    public void setLoadCanceled(boolean loadCanceled) {
        this.loadCanceled = loadCanceled;
    }
}
