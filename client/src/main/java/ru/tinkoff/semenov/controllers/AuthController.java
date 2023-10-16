package ru.tinkoff.semenov.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ru.tinkoff.semenov.Main;
import ru.tinkoff.semenov.Network;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    private Network network;
    private int attempts = 3;

    @FXML
    private Text attemptsInfo;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;
    @FXML
    private Button regButton;
    @FXML
    private Button continueButton;

    @FXML
    private void onAuthorize() {
        network.authorize(loginField.getText().trim(), passwordField.getText().trim());
    }

    @FXML
    private void onRegister() {
        regButton.setDisable(true);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 320, 180));
            stage.setTitle("Регистрация");
            stage.setAlwaysOnTop(true);
            stage.setOnCloseRequest(event -> {
                setFieldsForNewUser(fxmlLoader.getController());
                regButton.setDisable(false);
            });
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network(args -> {
            attempts--;
            if ((args[0]).equals("success")) {
                successAuthAction();
            } else if (attempts == 0) {
                attemptsOverAction();
            } else {
                attemptsInfo.setText("Осталось попыток: " + attempts);
            }
        });
    }

    private void successAuthAction() {
        attemptsInfo.setText("Добро пожаловать, " + loginField.getText() + "!");
        attemptsInfo.setFill(Color.GREEN);
        regButton.setVisible(false);
        authButton.setVisible(false);
        continueButton.setTextFill(Color.GREEN);
        continueButton.setVisible(true);
        continueButton.setOnAction(event -> {
            try {
                FXMLLoader fxmlLoaderCatalog = new FXMLLoader(Main.class.getResource("/catalog.fxml"));
                Stage stage = new Stage();
                Scene catalog = new Scene(fxmlLoaderCatalog.load(), 800, 400);
                stage.setTitle("Каталог");
                stage.setScene(catalog);
                ((Stage) continueButton.getScene().getWindow()).close();
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        network.close();
    }

    private void setFieldsForNewUser(RegisterController controller) {
        controller.getNetwork().close();
        if (controller.getNewLogin() != null) {
            loginField.setText(controller.getNewLogin());
            passwordField.setText(controller.getNewPassword());
            attemptsInfo.setText("");
            if (attempts == 0) {
                authButton.setDisable(false);
                loginField.setDisable(false);
                passwordField.setDisable(false);
                loginField.setEditable(false);
                passwordField.setEditable(false);
            }
        }
    }

    private void attemptsOverAction() {
        attemptsInfo.setText("Попытки закончились...");
        attemptsInfo.setFill(Color.RED);
        loginField.setDisable(true);
        passwordField.setDisable(true);
        authButton.setDisable(true);
    }

    public Network getNetwork() {
        return network;
    }
}
