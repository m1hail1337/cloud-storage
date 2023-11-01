package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CopyCommand implements Command {
    @Override
    public String execute(String args) {
        Path from = Path.of(MainHandler.getPathToUsersData() + "\\" + args.substring(0, args.indexOf("|")));
        Path to = Path.of(MainHandler.getPathToUsersData() + "\\" + args.substring(args.indexOf("|") + 1));
        try {
            Files.copy(from, to);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось копировать файл " + from.getFileName() + " в " + to);
        }
        return "";
    }
}

