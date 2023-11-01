package ru.tinkoff.semenov.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;


public class CatalogController implements Initializable {

    public static final String PATH_SEPARATOR = "\\";
    private static final String PATH_TO_DIR_CREATOR_PAGE = "/directory_creator.fxml";

    @FXML
    private ListView<String> filesList;
    @FXML
    private TextField currentPath;
    @FXML
    private Button prevDirButton;
    private String currentDirectory;
    private Network network;
    private String root;
    private String buffer;
    private boolean isLastCut;

    private final Map<String, Set<String>> catalog = new TreeMap<>();

    private final Action action = (message) -> {
        // TODO: Load file function
    };

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prevDirButton.setOnAction(event -> {
            Path prevDir = Path.of(currentPath.getText()).getParent();
            currentPath.setText(prevDir.toString());
            currentDirectory = prevDir.getFileName().toString();
            if (currentPath.getText().equals(root)) {
                prevDirButton.setDisable(true);
            }

            refreshDirectoryContent(currentDirectory);
        });

        configureContextMenu();
        configureDoubleClickOpen();
    }

    private void configureDoubleClickOpen() {
        PauseTransition doubleClickInterval = new PauseTransition(Duration.millis(300));
        final IntegerProperty clicks = new SimpleIntegerProperty(0);

        doubleClickInterval.setOnFinished(event -> {
            if (clicks.get() >= 2) {
                String directory = filesList.getFocusModel().getFocusedItem();
                if (directory != null && Utils.isDirectory(directory)) {
                    openDirectory(filesList.getFocusModel().getFocusedItem());
                }
            }
            clicks.set(0);
        });

        filesList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                clicks.set(clicks.get() + 1);
                doubleClickInterval.play();
            }
        });
    }

    private void configureContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem load = new MenuItem("Загрузить");
        load.setOnAction(event -> {
            loadFile(currentDirectory);
        });

        MenuItem open = new MenuItem("Открыть");
        open.setOnAction(event -> {
            openDirectory(filesList.getFocusModel().getFocusedItem());
        });

        MenuItem createDir = new MenuItem("Создать папку");
        createDir.setOnAction(event -> {
            showDirCreatorDialog();
        });

        MenuItem paste = new MenuItem("Вставить");
        paste.setOnAction(event -> {
            String destination = currentPath.getText();

            if (filesList.getFocusModel().getFocusedItem() != null) {
                destination += PATH_SEPARATOR + filesList.getFocusModel().getFocusedItem();
            }

            pasteFile(destination);
        });

        MenuItem cut = new MenuItem("Вырезать");
        cut.setOnAction(event -> {
            if (Utils.isRegularFile(filesList.getFocusModel().getFocusedItem())) {
                buffer = currentPath.getText() + PATH_SEPARATOR + filesList.getFocusModel().getFocusedItem();
                isLastCut = true;
            }
        });

        MenuItem copy = new MenuItem("Копировать");
        copy.setOnAction(event -> {
            if (Utils.isRegularFile(filesList.getFocusModel().getFocusedItem())) {
                buffer = currentPath.getText() + PATH_SEPARATOR + filesList.getFocusModel().getFocusedItem();
                isLastCut = false;
            }
        });

        MenuItem delete = new MenuItem("Удалить");
        delete.setOnAction(event -> {
            String toDelete = filesList.getFocusModel().getFocusedItem();
            catalog.get(currentDirectory).remove(toDelete);
            catalog.remove(toDelete);
            network.deleteFile(currentPath.getText() + PATH_SEPARATOR + toDelete);
            refreshDirectoryContent(currentDirectory);
        });

        contextMenu.getItems().addAll(createDir, load, open, cut, copy, paste, delete);

        filesList.setContextMenu(contextMenu);
        filesList.setOnContextMenuRequested(event -> {
            if (!event.getPickResult().getIntersectedNode().isFocused() || filesList.getItems().isEmpty()) {
                toggleMenuItems(contextMenu.getItems(), true);
                createDir.setDisable(false);
                filesList.getSelectionModel().clearSelection();

            } else {
                toggleMenuItems(contextMenu.getItems(), false);
                open.setDisable(Utils.isRegularFile(filesList.getFocusModel().getFocusedItem()));
                createDir.setDisable(true);
            }

            paste.setDisable(buffer == null);
        });
    }

    private void pasteFile(String destination) {
        Path pathToMove = Path.of(buffer);
        Path destinationPath = Path.of(destination);
        String fileName = pathToMove.getFileName().toString();

        catalog.get(destinationPath.getFileName().toString()).add(fileName);
        if (isLastCut) {
            catalog.get(pathToMove.getParent().getFileName().toString()).remove(fileName);
            network.cutFile(buffer, destination + PATH_SEPARATOR + fileName);
            buffer = null;
        } else {
            network.copyFile(buffer, destination + PATH_SEPARATOR + fileName);
        }

        if (destination.equals(currentPath.getText())) {
            filesList.getItems().add(fileName);
            refreshDirectoryContent(destinationPath.getFileName().toString());
        }
    }

    private void showDirCreatorDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PATH_TO_DIR_CREATOR_PAGE));
            Stage stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.load()));
            DirectoryCreatorController controller = fxmlLoader.getController();
            controller.setNetwork(network);
            controller.setCurrentDirectory(currentPath.getText());
            controller.setFilesList(new ArrayList<>(filesList.getItems()));
            stage.setTitle("Создание папки");
            stage.setOnCloseRequest(event -> {
                String dirName = controller.getNewDirName();

                if (dirName != null) {
                    filesList.getItems().add(dirName);
                    catalog.put(dirName, new HashSet<>());
                    catalog.get(currentDirectory).add(dirName);
                }

                network.getHandler().setAction(action);
            });

            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadFile(String destinationDirectory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для загрузки");
        File selectedFile = fileChooser.showSaveDialog(filesList.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // TODO
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Метод, закрывающий клиент пользователя и соединение с сервером.
     */
    @FXML
    private void exitApplication() {
        network.close();
        Platform.exit();
    }

    /**
     * Метод осуществляющий инициализацию каталога пользователя, стартовой(root) точкой всегда
     * является папка с именованная логином.
     *
     * @param paths массив путей к файлам и директориям на сервере.
     */
    public void initCatalog(String[] paths) {
        this.root = paths[0].replace("\\", "");
        currentDirectory = root;
        currentPath.setText(root);
        for (String path : paths) {
            String[] locations = path.split("\\\\");
            for (int i = 0; i < locations.length - 1; i++) {
                catalog.getOrDefault(locations[i], new HashSet<>()).add(locations[i + 1]);
            }
            if (Utils.isDirectory(locations[locations.length - 1])) {
                catalog.put(locations[locations.length - 1], new HashSet<>());
            }
        }

        filesList.setFocusTraversable(false);
        refreshDirectoryContent(root);
    }

    /**
     * Осуществляет переход в указанную дочернюю директорию.
     *
     * @param directory директория перехода.
     */
    private void openDirectory(String directory) {
        currentDirectory = directory;
        refreshDirectoryContent(directory);
        currentPath.setText(currentPath.getText() + PATH_SEPARATOR + directory);
        prevDirButton.setDisable(false);
    }

    /**
     * Обновляет содержание папки после какого-либо действия по изменению данных.
     *
     * @param directory директория для обновления.
     */
    private void refreshDirectoryContent(String directory) {
        filesList.getItems().clear();
        filesList.getItems().addAll(catalog.get(directory));
        prevDirButton.requestFocus();
    }

    /**
     * @param items     пункты меню.
     * @param isDisable выключить - {@code true}, включить - {@code false}.
     */
    private void toggleMenuItems(List<MenuItem> items, boolean isDisable) {
        items.forEach(item -> item.setDisable(isDisable));
    }

}
