package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileLengthCommand implements Command {

    private final MainHandler handler;

    public FileLengthCommand(MainHandler handler) {
        this.handler = handler;
    }

    @Override
    public String execute(String args) {
        Path pathToFile = Path.of(MainHandler.getPathToUsersData() + FILE_SEPARATOR + args);
        if (Files.exists(pathToFile)) {
            try {
                long fileLength = Files.size(pathToFile);
                handler.setCurrentDownloadFile(pathToFile.toFile());
                return Response.FILE_LENGTH.name() + ARGS_SEPARATOR + fileLength;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Response.FAILED.name();
    }
}
