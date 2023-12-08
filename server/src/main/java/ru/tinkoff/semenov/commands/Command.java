package ru.tinkoff.semenov.commands;

import java.nio.file.FileSystems;

public interface Command {

    String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();
    String ARGS_SEPARATOR = "|";

    String execute(String args);
}
