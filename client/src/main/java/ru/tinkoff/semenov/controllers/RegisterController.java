package ru.tinkoff.semenov.controllers;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.Utils;
import ru.tinkoff.semenov.enums.Response;

/**
 * Контроллер окна регистрации нового пользователя
 */
public class RegisterController {

    /**
     * Рекорд с данными только что зарегистрированного пользователя
     * @param login логин нового пользователя
     * @param password пароль нового пользователя
     */
    public record RegisteredUser(String login, String password) { }

    /**
     * Поле ввода логина
     */
    @FXML
    private TextField loginField;

    /**
     * Поле ввода пароля
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Кнопка регистрации
     */
    @FXML
    private Button regButton;

    /**
     * Информация о статусе регистрации
     */
    @FXML
    private Text statusInfo;

    /**
     * Канал подключения к серверу
     */
    private Network network;

    private RegisteredUser registeredUser;

    /**
     * Если успешно зарегистрировались, то вызываем {@link RegisterController#onSuccessRegister()}, если получаем
     * {@link Response#FAILED} - то такой пользователь уже есть, если получаем что-то еще другое выводим ошибку.
     */
    private final Action registerAction = message -> {
        if (Utils.getStatus(message).equals(Response.SUCCESS.name())) {
            onSuccessRegister();

        } else if (Utils.getStatus(message).equals(Response.FAILED.name())) {
            statusInfo.setText("Пользователь " + loginField.getText().trim() + " уже существует.");
            statusInfo.setFill(Color.RED);
            statusInfo.setVisible(true);

        } else {
            statusInfo.setText("Ошибка сервера.");
            statusInfo.setFill(Color.RED);
        }
    };

    /**
     * Действие при нажатии {@link RegisterController#regButton}
     */
    @FXML
    private void onRegister() {
        network.getDefaultHandler().setAction(registerAction);
        network.register(loginField.getText().trim(), passwordField.getText().trim());
    }

    /**
     * При успешной регистрации выводим, что все ок и прячем часть интерфейса, пользователю остается только закрыть это
     * окно
     */
    private void onSuccessRegister() {
        registeredUser = new RegisteredUser(loginField.getText().trim(), passwordField.getText().trim());
        statusInfo.setText("Пользователь " + registeredUser.login() + " успешно зарегистрирован.");
        statusInfo.setFill(Color.GREEN);
        statusInfo.setVisible(true);
        regButton.setVisible(false);
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public RegisteredUser getRegisteredUser() {
        return registeredUser;
    }
}
