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

/**
 * Контроллер окна скачивания файла с сервера
 */
public class DownloadController {

    /**
     * Частота обновления шкалы прогресса (в миллисекундах)
     */
    private static final int PROGRESSBAR_UPDATE_FREQ_MS = 100;

    /**
     * Канал подключения к серверу
     */
    private Network network;

    /**
     * Флаг успешного скачивания
     */
    private boolean isFileDownloaded = false;

    /**
     * Текст с информацией о состоянии скачивания
     */
    @FXML
    private Text infoText;

    /**
     * Шкала прогресса
     */
    @FXML
    private ProgressBar progressBar;

    /**
     * Кнопка отмены скачивания
     */
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

                Platform.runLater(() -> {
                    progressBar.setProgress((double) network.getFileHandler().getFile().length() / targetLength);
                });
                try {
                    Thread.sleep(PROGRESSBAR_UPDATE_FREQ_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            progressBar.setProgress(1.0);
            isFileDownloaded = true;
            infoText.setText("Файл успешно скачан!");
            infoText.setFill(Color.GREEN);
            finishButton.setVisible(true);
        }).start();
    }

    /**
     * Метод закрывающий окно скачивания
     */
    @FXML
    private void close() {
        Window window = finishButton.getScene().getWindow();
        Platform.runLater(() -> {
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }

    public boolean isFileDownloaded() {
        return isFileDownloaded;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

}
