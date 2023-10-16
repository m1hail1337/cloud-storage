package ru.tinkoff.semenov;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.tinkoff.semenov.controllers.AuthController;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoaderAuth = new FXMLLoader(Main.class.getResource("/auth.fxml"));
        Scene scene = new Scene(fxmlLoaderAuth.load(), 320, 280);
        stage.setTitle("Авторизация");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> ((AuthController) fxmlLoaderAuth.getController()).getNetwork().close());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}