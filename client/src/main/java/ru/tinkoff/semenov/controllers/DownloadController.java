package ru.tinkoff.semenov.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import ru.tinkoff.semenov.Network;

public class DownloadController {

    private static final int PROGRESSBAR_UPDATE_FREQ_MS = 100;

    private Network network;
    private boolean fileDownloaded = false;

    @FXML
    private Text infoText;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button finishButton;

    /**
     * Метод связывающий шкалу прогресса со статусом скачивания файла. При успешном скачивании становится доступна
     * {@link DownloadController#finishButton}
     */
    public void startProgress() {
        new Thread(() -> {
            long targetLength = network.getFileHandler().getTargetFileLength();
            while (network.getFileHandler().getFile().length() < targetLength) {
                Platform.runLater(() ->
                    progressBar.setProgress((double) network.getFileHandler().getFile().length() / targetLength)
                );
                try {
                    Thread.sleep(PROGRESSBAR_UPDATE_FREQ_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            progressBar.setProgress(1.0);
            fileDownloaded = true;
            infoText.setText("Файл успешно скачан!");
            infoText.setFill(Color.GREEN);
            finishButton.setVisible(true);
        }).start();
    }

    @FXML
    private void close() {
        Window window = finishButton.getScene().getWindow();
        Platform.runLater(() ->
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST))
        );
    }

    public boolean isFileDownloaded() {
        return fileDownloaded;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

}
