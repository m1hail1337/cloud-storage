package ru.tinkoff.semenov.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Main;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.Utils;
import ru.tinkoff.semenov.enums.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Контроллер JavaFX представления для начального состояния приложения - авторизации.
 * <br>*ВАЖНО*: Этот контроллер играет важную роль, т.к. создает подключение к серверу ({@link Network})
 */
public class AuthController implements Initializable {

    /**
     * Информация о статусе входа пользователя
     */
    @FXML
    private Text attemptsInfo;

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
     * Кнопка авторизации
     */
    @FXML
    private Button authButton;

    /**
     * Кнопка регистрации
     */
    @FXML
    private Button regButton;

    /**
     * Кнопка перехода к каталогу. Открывается только после успешной авторизации
     */
    @FXML
    private Button continueButton;

    /**
     * Путь к fxml-файлу со страницей регистрации
     */
    private static final String PATH_TO_REGISTER_PAGE = "/register.fxml";

    /**
     * Путь к fxml-файлу со страницей каталога
     */
    private static final String PATH_TO_CATALOG_PAGE = "/catalog.fxml";

    /**
     * Начальное количество попыток
     */
    private static final int MAX_ATTEMPTS = 3;

    /**
     * Текущее количество попыток
     */
    private int currentAttempts = MAX_ATTEMPTS;

    /**
     * Канал подключения к серверу
     */
    private Network network;

    /**
     * При успешной авторизации {@link AuthController#onSuccessAuth(String[] args)}, где args - данные пользователя с
     * сервера, при неуспешной авторизации уменьшаем счетчик попыток, при окончании попыток блокируем интерфейс
     */
    private final Action authAction = (message -> {
        currentAttempts--;
        if ((Utils.getStatus(message)).equals(Response.SUCCESS.name())) {
            onSuccessAuth(Utils.getArgs(message));
        } else if (currentAttempts == 0) {
            onAttemptsOver();
        } else {
            attemptsInfo.setText("Осталось попыток: " + currentAttempts);
        }
    });


    /**
     * Действие при нажатии на кнопку {@link AuthController#authButton}
     */
    @FXML
    private void onAuthorize() {
        network.getDefaultHandler().setAction(authAction);
        network.authorize(loginField.getText().trim(), passwordField.getText().trim());
    }

    /**
     * Действие при нажатии на кнопку {@link AuthController#regButton}
     */
    @FXML
    private void onRegister() {
        regButton.setDisable(true);
        showRegisterStage();
    }


    /**
     * При инициализации подключаемся к серверу
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network();
    }

    /**
     * Изменяем интерфейс при успешной авторизации
     * @param args данные пользователя пришедшие с сервера
     */
    private void onSuccessAuth(String[] args) {
        attemptsInfo.setText("Добро пожаловать, " + loginField.getText() + "!");
        attemptsInfo.setFill(Color.GREEN);
        regButton.setVisible(false);
        authButton.setVisible(false);

        showButtonToCatalog(args);
    }

    /**
     * Метод позволяет вставить логин и пароль в соответствующие поля, если пользователь успешно зарегистрировался
     * @param controller контроллер окна регистрации
     */
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

    /**
     * При окончании попыток блокируем интерфейс, можно только зарегистрироваться
     */
    private void onAttemptsOver() {
        attemptsInfo.setText("Попытки закончились...");
        attemptsInfo.setFill(Color.RED);
        loginField.setDisable(true);
        passwordField.setDisable(true);
        authButton.setDisable(true);
    }

    /**
     * Пока регистрируемся в форме ничего нельзя делать
     */
    private void onRegisteredDisable() {
        authButton.setDisable(false);
        loginField.setDisable(false);
        passwordField.setDisable(false);
        loginField.setEditable(false);
        passwordField.setEditable(false);
    }

    /**
     * По нажатию кнопки {@link AuthController#continueButton} переходим к каталогу
     * @param userPaths массив с путями для каждого файла пользователя на сервере
     */
    private void showButtonToCatalog(String[] userPaths) {
        continueButton.setTextFill(Color.GREEN);
        continueButton.setVisible(true);
        continueButton.setOnAction(event -> {
            showCatalogStage(userPaths);
        });
    }

    /**
     * Открываем окно регистрации
     */
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
                network.getDefaultHandler().setAction(authAction);
            });
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Открываем каталог и закрываем окно авторизации
     * @param userPaths массив с путями для каждого файла пользователя на сервере
     */
    private void showCatalogStage(String[] userPaths) {
        try {
            FXMLLoader fxmlLoaderCatalog = new FXMLLoader(Main.class.getResource(PATH_TO_CATALOG_PAGE));
            Stage stage = new Stage();
            Scene catalog = new Scene(fxmlLoaderCatalog.load(), 800, 400);
            CatalogController catalogController = fxmlLoaderCatalog.getController();
            catalogController.setNetwork(network);
            catalogController.initCatalog(userPaths);
            stage.setTitle("Каталог");
            stage.setScene(catalog);
            ((Stage) continueButton.getScene().getWindow()).close();
            stage.setOnCloseRequest(event -> {
                network.close();
            });
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Network getNetwork() {
        return network;
    }
}
