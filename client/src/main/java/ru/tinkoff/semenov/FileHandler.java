package ru.tinkoff.semenov;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedFile;

import java.nio.charset.StandardCharsets;
// TODO: Реализовать класс для приема файлов
public class FileHandler extends SimpleChannelInboundHandler<ChunkedFile> {

    private static final String PATH_TO_DOWNLOADS = "client\\downloads";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChunkedFile msg) throws Exception {
        System.out.println("wtf" + msg.readChunk(ByteBufAllocator.DEFAULT).toString(StandardCharsets.UTF_8));
    }
}
