package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Команда, создающая новую директорию(папку) на сервере в каталоге пользователя.
 */
public class NewDirCommand implements Command {

    @Override
    public String execute(String path) {
        Path newDirPath = Path.of(MainHandler.getPathToUsersData() + FILE_SEPARATOR + path);
        try {
            Files.createDirectory(newDirPath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Не удалось создать папку " + newDirPath.getFileName() + "в " + newDirPath.getParent(),
                    e);
        }
        return Response.EMPTY.name();
    }
}
