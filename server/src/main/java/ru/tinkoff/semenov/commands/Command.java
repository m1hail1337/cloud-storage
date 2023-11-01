package ru.tinkoff.semenov.commands;

/**
 * Интерфейс команд управления сервером
 */
public interface Command {

    String execute(String args);
}
