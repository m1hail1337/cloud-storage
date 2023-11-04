package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Команда отправляет размер файла, который клиент хочет скачать
 */
public class FileLengthCommand implements Command {

    /**
     * Храним ссылку на стандартный хендлер для того, чтобы обновлять поле файла, который пользователь запросил для
     * скачивания.
     */
    private final MainHandler handler;

    public FileLengthCommand(MainHandler handler) {
        this.handler = handler;
    }

    /**
     * @param args название файла
     * @return Если такой файл есть возвращаем строку в формате FILE_LENGTH|N, где N - размер файла в байтах; если такого
     * файла нет возвращаем FAILED.
     */
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
