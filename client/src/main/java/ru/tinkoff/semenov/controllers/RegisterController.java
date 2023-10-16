package ru.tinkoff.semenov.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import ru.tinkoff.semenov.Network;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable  {

    private Network network;
    private String newLogin;
    private String newPassword;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField loginField;

    @FXML
    private Button regButton;

    @FXML
    private Text statusInfo;

    @FXML
    private void onRegister() {
        network.register(loginField.getText().trim(), passwordField.getText().trim());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network(args -> {
            if (args[0].equals("success")) {
                newLogin = loginField.getText().trim();
                newPassword = passwordField.getText().trim();
                statusInfo.setText("Пользователь " + newLogin + " зарегистрирован.");
                statusInfo.setFill(Color.GREEN);
                statusInfo.setVisible(true);
                regButton.setVisible(false);
                network.close();
            } else if (args[0].equals("failed")) {
                statusInfo.setText("Пользователь " + loginField.getText().trim() + " уже существует.");
                statusInfo.setFill(Color.RED);
                statusInfo.setVisible(true);
            }
        });
    }

    public Network getNetwork() {
        return network;
    }

    public String getNewLogin() {
        return newLogin;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
