package ru.tinkoff.semenov;

import io.netty.channel.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainHandler extends SimpleChannelInboundHandler<String> {
    private static final String SEPARATOR = "|";
    private static final String PATH_TO_AUTH_DATA = "server/src/main/resources/users.txt";
    private static Map<String, String> users = new HashMap<>();
    private static final Map<String, Command> commands = new HashMap<>() {{
        put("AUTH", args -> {
            int loginLength = Character.getNumericValue(args.charAt(0));
            String login = args.substring(1, loginLength + 1);
            String password = args.substring(loginLength + 1);
            if (users.containsKey(login) && users.get(login).equals(password)) {
                return "success";
            }
            return "failed";
        });
        put("REGISTER", args -> {
            int loginLength = Character.getNumericValue(args.charAt(0));
            String login = args.substring(1, loginLength + 1);
            String password = args.substring(loginLength + 1);
            if (!users.containsKey(login)) {
                users.put(login, password);
                addUserAuthData(login, password);
                return "success";
            }
            return "failed";
        });
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
        try (Stream<String> lines = Files.lines(filePath)){
            users = lines.collect(Collectors.toMap(k -> k.split("\\s")[0], v -> v.split("\\s")[1]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addUserAuthData(String login, String password) {
        try {
            Files.write(Paths.get(PATH_TO_AUTH_DATA),
                    ("\n" + login + " " + password).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
