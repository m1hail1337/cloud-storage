package ru.tinkoff.semenov;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.tinkoff.semenov.controllers.AuthController;

import java.io.IOException;

public class Main extends Application {

    private static final String PATH_TO_AUTH_PAGE = "/auth.fxml";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoaderAuth = new FXMLLoader(Main.class.getResource(PATH_TO_AUTH_PAGE));
        Scene scene = new Scene(fxmlLoaderAuth.load(), 320, 280);
        Network network = ((AuthController) fxmlLoaderAuth.getController()).getNetwork();
        stage.setTitle("Авторизация");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> network.close());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
