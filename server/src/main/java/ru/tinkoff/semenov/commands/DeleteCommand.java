package ru.tinkoff.semenov.commands;

import ru.tinkoff.semenov.MainHandler;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DeleteCommand implements Command {
    @Override
    public String execute(String path) {
        try {
            Files.walkFileTree(Path.of(MainHandler.getPathToUsersData() + "\\" + path), new SimpleFileVisitor<>() {

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
            throw new RuntimeException("Не удалось удалить файл: "+ path, e);
        }
        return "";
    }
}
