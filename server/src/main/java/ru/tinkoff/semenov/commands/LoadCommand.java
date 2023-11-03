package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Команда, начинающая процесс закачки НА сервер файла пользователя. Создает файл, куда будут записываться полученные
 * байты.
 */
public class LoadCommand implements Command {
    @Override
    public String execute(String args) {
        String path = args.substring(0, args.indexOf(ARGS_SEPARATOR));
        try {
            Files.createFile(Path.of(MainHandler.getPathToUsersData() + FILE_SEPARATOR + path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Response.EMPTY.name();
    }
}
