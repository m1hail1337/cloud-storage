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
import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Main;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.enums.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

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

    private static final String PATH_TO_REGISTER_PAGE = "/register.fxml";
    private static final String PATH_TO_CATALOG_PAGE = "/catalog.fxml";
    private static final int MAX_ATTEMPTS = 3;

    private int currentAttempts = MAX_ATTEMPTS;
    private Network network;

    private final Action authAction = (args -> {
        currentAttempts--;
        if ((args[0]).equals(Response.SUCCESS.name())) {
            onSuccessAuth();
        } else if (currentAttempts == 0) {
            onAttemptsOver();
        } else {
            attemptsInfo.setText("Осталось попыток: " + currentAttempts);
        }
    });

    @FXML
    private void onAuthorize() {
        network.getHandler().setAction(authAction);
        network.authorize(loginField.getText().trim(), passwordField.getText().trim());
    }

    @FXML
    private void onRegister() {
        regButton.setDisable(true);
        showRegisterStage();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network();
    }

    private void onSuccessAuth() {
        attemptsInfo.setText("Добро пожаловать, " + loginField.getText() + "!");
        attemptsInfo.setFill(Color.GREEN);
        regButton.setVisible(false);
        authButton.setVisible(false);

        showButtonToCatalog();
    }

    private void onSuccessRegistered(RegisterController controller) {
        if (controller.getRegisteredUser() != null) {
            loginField.setText(controller.getRegisteredUser().login());
            passwordField.setText(controller.getRegisteredUser().password());
            attemptsInfo.setText("");

            if (currentAttempts == 0) {
                onRegisteredDisable();
            }
        }
    }

    private void onAttemptsOver() {
        attemptsInfo.setText("Попытки закончились...");
        attemptsInfo.setFill(Color.RED);
        loginField.setDisable(true);
        passwordField.setDisable(true);
        authButton.setDisable(true);
    }

    private void onRegisteredDisable() {
        authButton.setDisable(false);
        loginField.setDisable(false);
        passwordField.setDisable(false);
        loginField.setEditable(false);
        passwordField.setEditable(false);
    }

    private void showButtonToCatalog() {
        continueButton.setTextFill(Color.GREEN);
        continueButton.setVisible(true);
        continueButton.setOnAction(event -> {
            try {
                FXMLLoader fxmlLoaderCatalog = new FXMLLoader(Main.class.getResource(PATH_TO_CATALOG_PAGE));
                Stage stage = new Stage();
                Scene catalog = new Scene(fxmlLoaderCatalog.load(), 800, 400);
                ((CatalogController) fxmlLoaderCatalog.getController()).setNetwork(network);
                stage.setTitle("Каталог");
                stage.setScene(catalog);
                ((Stage) continueButton.getScene().getWindow()).close();
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void showRegisterStage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PATH_TO_REGISTER_PAGE));
            Parent root = fxmlLoader.load();
            RegisterController registerController = fxmlLoader.getController();
            registerController.setNetwork(network);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 320, 180));
            stage.setTitle("Регистрация");
            stage.setAlwaysOnTop(true);
            stage.setOnCloseRequest(event -> {
                onSuccessRegistered(registerController);
                regButton.setDisable(false);
                network.getHandler().setAction(authAction);
            });
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Network getNetwork() {
        return network;
    }
}
