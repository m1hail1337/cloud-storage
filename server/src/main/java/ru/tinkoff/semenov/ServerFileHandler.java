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

public class ServerFileHandler extends ChannelInboundHandlerAdapter {

    private final MainHandler defaultHandler;
    private final File file;
    private final long fileLength;
    private final String path;

    private boolean loadCanceled = false;

    public ServerFileHandler(String path, long fileLength, MainHandler defaultHandler) {
        this.path = path;
        this.file = new File(path);
        this.defaultHandler = defaultHandler;
        this.fileLength = fileLength;
    }

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        loadCanceled = true;
        switchToDefaultHandler(ctx);
        throw new RuntimeException("Ошибка в получении файла", cause);
    }

    private void switchToDefaultHandler(ChannelHandlerContext ctx) {
        ctx.pipeline().addFirst("stringEncoder", new StringEncoder());
        ctx.pipeline().addFirst("stringDecoder", new StringDecoder());
        ctx.pipeline().replace("fileHandler", "defaultHandler", defaultHandler);

        if (loadCanceled) {
            defaultHandler.getCommands().get("DELETE").execute(path);
            ctx.writeAndFlush(Response.FAILED.name() + MainHandler.SEPARATOR);
        } else {
            ctx.writeAndFlush(Response.LOADED.name() + MainHandler.SEPARATOR);
        }
    }
}
