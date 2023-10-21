package ru.tinkoff.semenov.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.enums.Response;

public class RegisterController  {

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField loginField;
    @FXML
    private Button regButton;
    @FXML
    private Text statusInfo;

    private Network network;
    private String newLogin;
    private String newPassword;
    private final Action registerAction = args -> {
        if (args[0].equals(Response.SUCCESS.name())) {
            newLogin = loginField.getText().trim();
            newPassword = passwordField.getText().trim();
            statusInfo.setText("Пользователь " + newLogin + " успешно зарегистрирован.");
            statusInfo.setFill(Color.GREEN);
            statusInfo.setVisible(true);
            regButton.setVisible(false);
        } else if (args[0].equals(Response.FAILED.name())) {
            statusInfo.setText("Пользователь " + loginField.getText().trim() + " уже существует.");
            statusInfo.setFill(Color.RED);
            statusInfo.setVisible(true);
        } else {
            statusInfo.setText("Ошибка команды сервера.");
            statusInfo.setFill(Color.RED);
        }
    };

    @FXML
    private void onRegister() {
        network.getHandler().setAction(registerAction);
        network.register(loginField.getText().trim(), passwordField.getText().trim());
    }

    public String getNewLogin() {
        return newLogin;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
