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

    public static final String SEPARATOR = "|";

    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    private final DefaultClientHandler defaultHandler = new DefaultClientHandler();

    private SocketChannel channel;
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
        t.setDaemon(true);
        t.start();
    }

    public void register(String login, String password) {
        channel.writeAndFlush(Command.REGISTER.name() + SEPARATOR + login.length() + login + password);
    }

    public void authorize(String login, String password) {
        channel.writeAndFlush(Command.AUTH.name() + SEPARATOR + login.length() + login + password);
    }

    public void cutFile(String file, String destination) {
        channel.writeAndFlush(Command.CUT.name() + SEPARATOR + file + SEPARATOR + destination);
    }

    public void copyFile(String file, String destination) {
        channel.writeAndFlush(Command.COPY.name() + SEPARATOR + file + SEPARATOR + destination);
    }

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

    public void downloadRequest(String filePath) {
        channel.writeAndFlush(Command.FILE_LENGTH.name() + SEPARATOR + filePath);
    }

    public void downloadRequestedFile(String filename, long fileLength) {
        this.fileHandler = new FileClientHandler(filename, fileLength, defaultHandler);
        channel.writeAndFlush(Command.DOWNLOAD.name() + SEPARATOR);

        ChannelPipeline pipeline = channel.pipeline();
        pipeline.remove("stringEncoder");
        pipeline.remove("stringDecoder");
        pipeline.replace("defaultHandler", "fileHandler", fileHandler);
    }

    public void createNewDirectory(String path) {
        channel.writeAndFlush(Command.NEW_DIR.name() + SEPARATOR + path);
    }

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
