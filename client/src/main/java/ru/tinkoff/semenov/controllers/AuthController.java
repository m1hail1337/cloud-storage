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
import ru.tinkoff.semenov.enums.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    private static final String PATH_TO_REGISTER_PAGE = "/register.fxml";
    private static final String PATH_TO_CATALOG_PAGE = "/catalog.fxml";
    private static final int MAX_ATTEMPTS = 3;
    private Network network;
    private int currentAttempts = MAX_ATTEMPTS;

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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PATH_TO_REGISTER_PAGE));
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
            currentAttempts--;
            if ((args[0]).equals(Response.SUCCESS.name())) {
                successAuthAction();
            } else if (currentAttempts == 0) {
                attemptsOverAction();
            } else {
                attemptsInfo.setText("Осталось попыток: " + currentAttempts);
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
                FXMLLoader fxmlLoaderCatalog = new FXMLLoader(Main.class.getResource(PATH_TO_CATALOG_PAGE));
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
            if (currentAttempts == 0) {
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
