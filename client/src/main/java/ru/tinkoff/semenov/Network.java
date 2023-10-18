package ru.tinkoff.semenov;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import ru.tinkoff.semenov.enums.Command;

public class Network {
    private static final String SEPARATOR = "|";
    private static final String HOST = "localhost";
    private static final int PORT = 8189;
    private SocketChannel channel;

    public Network(Callback onRequestReceivedCallback) {
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
                                socketChannel.pipeline().addLast(new StringDecoder(),
                                        new StringEncoder(),
                                        new ClientHandler(onRequestReceivedCallback));
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

    public void register(String login, String password) {
        channel.writeAndFlush(Command.REGISTER.name() + SEPARATOR + login.length() + login + password);
    }

    public void authorize(String login, String password) {
        channel.writeAndFlush(Command.AUTH.name() + SEPARATOR + login.length() + login + password);
    }

    // TODO: public void getFiles() {}

    public void close() {
        channel.close();
    }
}
