package ru.tinkoff.semenov.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.Utils;

import java.util.Map;
import java.util.Set;

/**
 * Контроллер создания новой папки (директории)
 */
public class DirectoryCreatorController {

    /**
     * Канал подключения к серверу
     */
    private Network network;

    /**
     * Каталог. Представляет собой пары Директория - Множество файлов и директорий внутри
     */
    private Map<String, Set<String>> catalog;

    /**
     * Текущая директория
     */
    private String currentDirectory;

    /**
     * Название новой папки
     */
    private String newDirName;

    /**
     * Поле ввода названия новой папки
     */
    @FXML
    private TextField newDirField;

    /**
     * Сообщение об ошибке создания папки
     */
    @FXML
    private Text invalidMessage;

    /**
     * Попытка создать новую папку.
     * <br> *ВАЖНО*: Так как я использую мапу и сеты в качестве каталога я не могу иметь двух папок с одинаковым
     * названием.
     */
    @FXML
    private void onCreateNewDir() {
        String newDirectory = newDirField.getText();

        if (Utils.isDirectory(newDirectory) && !catalog.containsKey(newDirectory)) {
            this.newDirName = newDirectory;
            network.createNewDirectory(currentDirectory + CatalogController.PATH_SEPARATOR + newDirectory);

            closeCreator(newDirField.getScene().getWindow());
        } else {
            invalidMessage.setText("Недопустимое имя папки");
            invalidMessage.setVisible(true);
        }
    }

    /**
     * Отмена создания папки (закрытие окна)
     */
    @FXML
    private void cancelCreateNewDir(ActionEvent event) {
        closeCreator(((Button) event.getSource()).getScene().getWindow());
    }

    /**
     * Закрытие окна создания папки
     * @param window окно создания папки
     */
    private void closeCreator(Window window) {
        Platform.runLater(() -> {
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public void setCatalog(Map<String, Set<String>> catalog) {
        this.catalog = catalog;
    }

    public String getNewDirName() {
        return newDirName;
    }
}
