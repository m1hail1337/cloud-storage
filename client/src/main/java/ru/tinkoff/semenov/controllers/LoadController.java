package ru.tinkoff.semenov.controllers;

import io.netty.handler.stream.ChunkedFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.Utils;
import ru.tinkoff.semenov.enums.Response;

public class LoadController {

    private static final int PROGRESSBAR_UPDATE_FREQ_MS = 100;


    private ChunkedFile file;
    private Network network;
    private boolean fileLoaded = false;

    @FXML
    private Text infoText;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button finishButton;

    private final Action loadAction = message -> {
        if (Utils.getStatus(message).equals(Response.LOADED.name())) {
            finishButton.setVisible(true);
            progressBar.setProgress(1.0);
            fileLoaded = true;
            infoText.setFill(Color.GREEN);
            infoText.setText("Файл успешно сохранен.");

        } else if (Utils.getStatus(message).equals(Response.FAILED.name())) {
            infoText.setText("Ошибка на сервере");
            infoText.setFill(Color.RED);
        }
    };

    /**
     * Запускаем шкалу прогресса, подвязывая ее к процессу отправки файла.
     * <br>*ВАЖНО*: Шкала подвязывается только к отправке. Поэтому, чтобы убедиться в том, что все
     * байты ПОЛУЧЕНЫ и файл сохранен корректно ждем ответного сообщения. Пока ждем выводим, что программа не зависла.
     */
    public void startProgress() {
        network.getDefaultHandler().setAction(loadAction);

        new Thread(() -> {
            while (file.currentOffset() < file.endOffset()) {

                Platform.runLater(() ->
                    progressBar.setProgress((double) file.currentOffset() / file.endOffset())
                );
                try {
                    Thread.sleep(PROGRESSBAR_UPDATE_FREQ_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!finishButton.isVisible()) {
                infoText.setText("Ждем подтверждения успешного сохранения...");
                infoText.setFill(Color.GREEN);
            }
        }).start();
    }

    @FXML
    private void close() {
        Window window = finishButton.getScene().getWindow();
        Platform.runLater(() ->
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST))
        );
    }

    public boolean isFileLoaded() {
        return fileLoaded;
    }

    public void setFile(ChunkedFile file) {
        this.file = file;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
