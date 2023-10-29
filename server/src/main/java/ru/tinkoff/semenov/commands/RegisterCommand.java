package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.*;

public class RegisterCommand implements Command {

    private static final String USERS_DATA = "server/src/main/resources/dirs";

    @Override
    public String execute(String args) {
        int loginLength = Character.getNumericValue(args.charAt(0));
        String login = args.substring(1, loginLength + 1);
        String password = args.substring(loginLength + 1);

        if (!MainHandler.getUsers().containsKey(login)) {
            MainHandler.getUsers().put(login, password);
            addUserAuthData(login, password);
            try {
                Files.createDirectory(Paths.get(USERS_DATA + FileSystems.getDefault().getSeparator() + login));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return Response.SUCCESS.name();
        }
        return Response.FAILED.name();
    }

    private static void addUserAuthData(String login, String password) {
        try {
            Files.write(Paths.get(MainHandler.getPathToAuthData()),
                    ("\n" + login + " " + password).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + MainHandler.getPathToAuthData(), e);
        }
    }
}
