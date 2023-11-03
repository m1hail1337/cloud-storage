package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Команда для регистрации нового пользователя в системе
 */
public class RegisterCommand implements Command {

    /** Если регистрируется новый пользователь он добавляется в общую мапу и в файл, поэтому новые клиенты и клиенты
     * запустившие приложение после регистрации будут знать о нем.
     * @param args логин и пароль передаются в формате Nлогинпароль, где N - количество букв в пароле.
     * @return ответ сервера в формате
     * ({@link Response#SUCCESS} (если регистрация прошла успешно) или {@link Response#FAILED} (если пользователь с
     * таким логином уже есть в системе)
     */
    @Override
    public String execute(String args) {
        int loginLength = Character.getNumericValue(args.charAt(0));
        String login = args.substring(1, loginLength + 1);
        String password = args.substring(loginLength + 1);

        if (!MainHandler.getUsers().containsKey(login)) {
            MainHandler.getUsers().put(login, password);
            addUserAuthData(login, password);
            try {
                Files.createDirectory(Paths
                        .get(MainHandler.getPathToUsersData() + FILE_SEPARATOR + login));
            } catch (IOException e) {
                throw new RuntimeException("Не удалось создать директорию в " + MainHandler.getPathToUsersData(), e);
            }
            return Response.SUCCESS.name();
        }
        return Response.FAILED.name();
    }

    /**
     * Метод создающий запись о регистрационных данных нового пользователя
     * @param login логин нового пользователя
     * @param password пароль нового пользователя
     */
    private static void addUserAuthData(String login, String password) {
        try {
            Files.write(Paths.get(MainHandler.getPathToAuthData()),
                    ("\n" + login + " " + password).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + MainHandler.getPathToAuthData(), e);
        }
    }
}
