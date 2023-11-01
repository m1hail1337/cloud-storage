package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

/**
 * Команда по авторизации пользователя в системе
 */
public class AuthCommand implements Command {

    /**
     * Авторизация: Проверка есть ли пользователь среди зарегистрированных. Если да отправляем в ответ
     * {@link Response#SUCCESS} и его директории ({@link MainHandler#getUserDirs(String login)}),
     * если нет, то {@link Response#FAILED} и сообщение почему не получилось подключиться
     * @param args логин и пароль передаются в формате Nлогинпароль, где N - количество букв в пароле.
     * @return ответ сервера в формате
     * ({@link Response#SUCCESS} или {@link Response#FAILED})|(информация о директориях (через разделитель
     * {@link MainHandler#SEPARATOR}) или сообщение о типе ошибки)
     */
    @Override
    public String execute(String args) {
        int loginLength = Character.getNumericValue(args.charAt(0));
        String login = args.substring(1, loginLength + 1);
        String password = args.substring(loginLength + 1);
        if (MainHandler.getUsers().containsKey(login)) {
            if (MainHandler.getUsers().get(login).equals(password)) {
                return Response.SUCCESS.name() + MainHandler.SEPARATOR + MainHandler.getUserDirs(login);
            }
            return Response.FAILED.name() + MainHandler.SEPARATOR + "Incorrect password";
        }
        return Response.FAILED.name() + MainHandler.SEPARATOR + "There's no user with this login";
    }
}
