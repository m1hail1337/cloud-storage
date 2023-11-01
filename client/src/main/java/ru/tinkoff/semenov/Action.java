package ru.tinkoff.semenov;

/**
 * Интерфейс события, которое должно обрабатываться в данный момент
 */
public interface Action {
    void handle(String args);
}
