package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

public class AuthCommand implements Command {

    @Override
    public String execute(String args) {
        int loginLength = Character.getNumericValue(args.charAt(0));
        String login = args.substring(1, loginLength + 1);
        String password = args.substring(loginLength + 1);
        if (MainHandler.getUsers().containsKey(login)) {
            if (MainHandler.getUsers().get(login).equals(password)) {
                return Response.SUCCESS.name() + ARGS_SEPARATOR + MainHandler.getUserDirs(login);
            }
            return Response.FAILED.name() + ARGS_SEPARATOR + "Incorrect password";
        }
        return Response.FAILED.name() + ARGS_SEPARATOR + "There's no user with this login";
    }
}
