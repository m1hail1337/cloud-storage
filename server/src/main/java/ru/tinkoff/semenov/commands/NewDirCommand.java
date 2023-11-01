package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NewDirCommand implements Command {

    @Override
    public String execute(String path) {
        try {
            Files.createDirectory(Path.of(MainHandler.getPathToUsersData() + "\\" + path));
            return Response.SUCCESS.name();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.FAILED.name();
        }
    }
}
