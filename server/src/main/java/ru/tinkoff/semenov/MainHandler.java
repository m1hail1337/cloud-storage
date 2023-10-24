package ru.tinkoff.semenov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.tinkoff.semenov.commands.AuthCommand;
import ru.tinkoff.semenov.commands.Command;
import ru.tinkoff.semenov.commands.RegisterCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainHandler extends SimpleChannelInboundHandler<String> {

    private static final String PATH_TO_AUTH_DATA = "server/src/main/resources/users.txt";
    private static final String SEPARATOR = "|";
    private static final Map<String, String> users = new HashMap<>();

    private static final Map<String, Command> commands = new HashMap<>() {{
        put("AUTH", new AuthCommand());
        put("REGISTER", new RegisterCommand());
        // TODO:  put("GET_DATA", args -> {});
    }};

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        initUsersMap();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        String command = msg.substring(0, msg.indexOf(SEPARATOR));
        String args = msg.substring(msg.indexOf(SEPARATOR) + 1);
        ctx.channel().writeAndFlush(commands.get(command).execute(args));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        throw new RuntimeException(cause);
    }

    private void initUsersMap() {
        Path filePath = Paths.get(PATH_TO_AUTH_DATA);
        try (Stream<String> lines = Files.lines(filePath)) {
            users.clear();
            users.putAll(lines.collect(Collectors.toMap(k -> k.split("\\s")[0], v -> v.split("\\s")[1])));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + PATH_TO_AUTH_DATA, e);
        }
    }

    public static String getPathToAuthData() {
        return PATH_TO_AUTH_DATA;
    }

    public static Map<String, String> getUsers() {
        return users;
    }
}
