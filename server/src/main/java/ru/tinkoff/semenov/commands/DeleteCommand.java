package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;
import ru.tinkoff.semenov.Response;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Команда удаления файла/директории с сервера. Если это директория, то также удалятся и все файлы внутри.
 */
public class DeleteCommand implements Command {
    @Override
    public String execute(String path) {
        Path filePath = Path.of(MainHandler.getPathToUsersData() + FILE_SEPARATOR + path);
        if (Files.exists(filePath)) {
            try {
                Files.walkFileTree(filePath, new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Не удалось удалить файл " + filePath.getFileName().toString(), e);
            }
        }

        return Response.EMPTY.name();
    }
}
