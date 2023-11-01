package ru.tinkoff.semenov.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.Utils;
import ru.tinkoff.semenov.enums.Response;

import java.util.List;

public class DirectoryCreatorController {

    private Network network;
    private List<String> filesList;
    private String currentDirectory;
    private String newDirName;

    @FXML
    private TextField newDirField;
    @FXML
    private Text invalidMessage;

    private final Action createDirAction = (message) -> {
        if (Utils.getStatus(message).equals(Response.SUCCESS.name())) {
            this.newDirName = newDirField.getText();
            Window window = newDirField.getScene().getWindow();

            Platform.runLater(() -> {
                window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
            });
        } else {
            invalidMessage.setText("Ошибка сервера");
        }
    };

    @FXML
    private void onCreateNewDir() {
        String newDirectory = newDirField.getText();

        if (Utils.isDirectory(newDirectory) && !filesList.contains(newDirectory)) {
            network.getHandler().setAction(createDirAction);
            network.createNewDirectory(currentDirectory + CatalogController.PATH_SEPARATOR + newDirectory);

        } else {
            invalidMessage.setText("Недопустимое имя папки");
            invalidMessage.setVisible(true);
        }
    }

    @FXML
    private void cancelCreateNewDir(ActionEvent event) {
        ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public void setFilesList(List<String> filesList) {
        this.filesList = filesList;
    }

    public String getNewDirName() {
        return newDirName;
    }
}
