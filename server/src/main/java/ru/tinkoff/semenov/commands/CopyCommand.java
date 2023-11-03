package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Команда, копирующая файл из одной директории в другую.
 */
public class CopyCommand implements Command {
    @Override
    public String execute(String args) {
        Path from = Path.of(
                MainHandler.getPathToUsersData()
                        + FILE_SEPARATOR
                        + args.substring(0, args.indexOf(ARGS_SEPARATOR)));
        Path to = Path.of(
                MainHandler.getPathToUsersData()
                        + FILE_SEPARATOR
                        + args.substring(args.indexOf(ARGS_SEPARATOR) + 1));
        try {
            Files.copy(from, to);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось копировать файл " + from.getFileName() + " в " + to);
        }
        return Response.EMPTY.name();
    }
}

