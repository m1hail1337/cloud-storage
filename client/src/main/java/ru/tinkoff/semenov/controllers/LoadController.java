package ru.tinkoff.semenov.controllers;

import io.netty.handler.stream.ChunkedFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;

import java.net.URL;
import java.util.ResourceBundle;


public class LoadController implements Initializable {

    private static final int PROGRESSBAR_UPDATE_FREQ_MS = 100;

    private ChunkedFile file;
    private Network network;
    private boolean isFileLoaded = false;

    @FXML
    private Text infoText;
    @FXML
    private ProgressBar progress;
    @FXML
    private Button finishButton;

    private final Action loadAction = message -> {
        if (message.equals("LOADED")) {
            finishButton.setVisible(true);
            progress.setProgress(1.0);
            isFileLoaded = true;
            infoText.setFill(Color.GREEN);
            infoText.setText("Файл успешно сохранен.");

        } else if (message.equals("FAILED")) {
            infoText.setText("Ошибка на сервере");
            infoText.setFill(Color.RED);
        }
    };

    public void startProgress() {
        network.getHandler().setAction(loadAction);

        new Thread(() -> {
            while (file.currentOffset() < file.endOffset()) {

                Platform.runLater(() -> {
                    progress.setProgress((double) file.currentOffset() / file.endOffset());
                });
                try {
                    Thread.sleep(PROGRESSBAR_UPDATE_FREQ_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!finishButton.isVisible()) {
                infoText.setText("Я не завис :)");
                infoText.setFill(Color.GREEN);
            }
        }).start();
    }

    @FXML
    private void close() {
        Window window = finishButton.getScene().getWindow();
        Platform.runLater(() -> {
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progress.setProgress(0.0);
        infoText.setText("Статус загрузки");
    }

    public boolean isFileLoaded() {
        return isFileLoaded;
    }

    public void setFile(ChunkedFile file) {
        this.file = file;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
