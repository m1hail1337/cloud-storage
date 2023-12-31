package ru.tinkoff.semenov.controllers;

import io.netty.handler.stream.ChunkedFile;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;
import ru.tinkoff.semenov.Utils;
import ru.tinkoff.semenov.enums.Response;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class CatalogController implements Initializable {

    public static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();
    private static final String PATH_TO_DIR_CREATOR_PAGE = "/directory_creator.fxml";
    private static final String PATH_TO_LOAD_PROGRESS_PAGE = "/load_progress.fxml";
    private static final String PATH_TO_DOWNLOAD_PROGRESS_PAGE = "/download_progress.fxml";

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
    private boolean lastActionIsCut;

    private final Map<String, Set<String>> catalog = new HashMap<>();
    private final Action downloadRequestAction = message -> {
        if (Utils.getStatus(message).equals(Response.FILE_LENGTH.name())) {
            downloadFile(filesList.getSelectionModel().getSelectedItem(),
                    Long.parseLong(Utils.getArgs(message)[0]));
        }
    };

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

    public void initCatalog(String[] paths) {
        this.root = paths[0].replace(PATH_SEPARATOR, "");
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

    @FXML
    private void exitApplication() {
        network.close();
        Platform.exit();
    }

    private void configureDoubleClickOpen() {
        PauseTransition doubleClickInterval = new PauseTransition(Duration.millis(300));
        IntegerProperty clicks = new SimpleIntegerProperty(0);

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

        MenuItem download = new MenuItem("Скачать");
        configureDownloadMenuItem(download);

        MenuItem load = new MenuItem("Загрузить");
        load.setOnAction(event ->
                loadFile(currentPath.getText())
        );

        MenuItem open = new MenuItem("Открыть");
        open.setOnAction(event ->
                openDirectory(filesList.getSelectionModel().getSelectedItem())
        );

        MenuItem createDir = new MenuItem("Создать папку");
        createDir.setOnAction(event ->
                showDirCreatorDialog()
        );

        MenuItem paste = new MenuItem("Вставить");
        configurePasteMenuItem(paste);

        MenuItem cut = new MenuItem("Вырезать");
        configureCutMenuItem(cut);

        MenuItem copy = new MenuItem("Копировать");
        configureCopyMenuItem(copy);

        MenuItem delete = new MenuItem("Удалить");
        configureDeleteMenuItem(delete);

        contextMenu.getItems().addAll(createDir, load, download, open, cut, copy, paste, delete);

        filesList.setOnContextMenuRequested(event -> {
            disableMenuItems(contextMenu.getItems());  // делаем все заблокированные, а потом разбираемся

            if (!event.getPickResult().getIntersectedNode().isFocused()) {
                createDir.setDisable(false);
                load.setDisable(false);

            } else {
                boolean isDirectory = Utils.isDirectory(filesList.getSelectionModel().getSelectedItem());
                open.setDisable(!isDirectory);
                download.setDisable(isDirectory);
                cut.setDisable(isDirectory);
                copy.setDisable(isDirectory);
                delete.setDisable(false);
            }

            paste.setDisable(buffer == null);
        });

        filesList.setContextMenu(contextMenu);
    }

    private void configureDownloadMenuItem(MenuItem download) {
        download.setOnAction(event -> {
            network.downloadRequest(currentPath.getText()
                    + PATH_SEPARATOR
                    + filesList.getSelectionModel().getSelectedItem());
            network.getDefaultHandler().setAction(downloadRequestAction);
        });
    }

    private void configureDeleteMenuItem(MenuItem deleteItem) {
        deleteItem.setOnAction(event -> {
            String toDelete = filesList.getSelectionModel().getSelectedItem();
            catalog.get(currentDirectory).remove(toDelete);
            catalog.remove(toDelete);
            network.deleteFile(currentPath.getText() + PATH_SEPARATOR + toDelete);
            refreshDirectoryContent(currentDirectory);

            if (buffer != null && buffer.equals(currentPath.getText() + PATH_SEPARATOR + toDelete)) {
                buffer = null;
            }
        });
    }

    private void configurePasteMenuItem(MenuItem paste) {
        paste.setOnAction(event -> {
            String destination = currentPath.getText();

            if (filesList.getSelectionModel().getSelectedItem() != null) {
                destination += PATH_SEPARATOR + filesList.getSelectionModel().getSelectedItem();
            }

            pasteFile(destination);
        });
    }

    private void configureCopyMenuItem(MenuItem copy) {
        copy.setOnAction(event -> {
            if (Utils.isRegularFile(filesList.getSelectionModel().getSelectedItem())) {
                buffer = currentPath.getText() + PATH_SEPARATOR + filesList.getSelectionModel().getSelectedItem();
                lastActionIsCut = false;
            }
        });
    }

    private void configureCutMenuItem(MenuItem cut) {
        cut.setOnAction(event -> {
            if (Utils.isRegularFile(filesList.getSelectionModel().getSelectedItem())) {
                buffer = currentPath.getText() + PATH_SEPARATOR + filesList.getSelectionModel().getSelectedItem();
                lastActionIsCut = true;
            }
        });
    }

    private void pasteFile(String destination) {
        Path pathToMove = Path.of(buffer);
        Path destinationPath = Path.of(destination);
        String fileName = pathToMove.getFileName().toString();

        catalog.get(destinationPath.getFileName().toString()).add(fileName);

        if (lastActionIsCut) {
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

    private void loadFile(String destinationDirectory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для загрузки");
        try {
            File selectedFile = fileChooser.showOpenDialog(filesList.getScene().getWindow());
            if (selectedFile != null) {
                ChunkedFile sendFile = new ChunkedFile(selectedFile);
                showLoadProgress(sendFile, selectedFile.getName(), destinationDirectory);
                network.loadFile(sendFile, destinationDirectory + PATH_SEPARATOR + selectedFile.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadFile(String filename, long fileLength) {
        Platform.runLater(this::showDownloadProgress);
        network.downloadRequestedFile(filename, fileLength);
    }

    private void showDirCreatorDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PATH_TO_DIR_CREATOR_PAGE));
            Stage stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.load()));
            DirectoryCreatorController controller = fxmlLoader.getController();
            controller.setNetwork(network);
            controller.setCurrentDirectory(currentPath.getText());
            controller.setCatalog(catalog);
            stage.setTitle("Создание папки");
            stage.setOnCloseRequest(event -> {
                String dirName = controller.getNewDirName();

                if (dirName != null) {
                    filesList.getItems().add(dirName);
                    catalog.put(dirName, new HashSet<>());
                    catalog.get(currentDirectory).add(dirName);
                }
            });

            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void showDownloadProgress() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PATH_TO_DOWNLOAD_PROGRESS_PAGE));
            Stage stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.load()));
            DownloadController controller = fxmlLoader.getController();
            controller.setNetwork(network);
            stage.setTitle("Скачивание файла");
            stage.setAlwaysOnTop(true);
            stage.setOnCloseRequest(event -> {
                if (!controller.isFileDownloaded()) {
                    network.getFileHandler().setDownloadCanceled(true);
                }
            });

            stage.show();
            controller.startProgress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showLoadProgress(ChunkedFile sendFile, String filename, String destination) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PATH_TO_LOAD_PROGRESS_PAGE));
            Stage stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.load()));
            LoadController controller = fxmlLoader.getController();
            controller.setFile(sendFile);
            controller.setNetwork(network);
            stage.setTitle("Отправка файла");
            stage.setAlwaysOnTop(true);
            stage.setOnCloseRequest(event -> {
                if (controller.isFileLoaded()) {
                    addNewFile(filename, destination);
                } else {
                    network.setLoadCanceled(true);
                }
            });

            stage.show();
            controller.startProgress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewFile(String filename, String destination) {
        catalog.get(Path.of(destination).getFileName().toString()).add(filename);

        if (currentPath.getText().equals(destination)) {
            filesList.getItems().add(filename);
        }
    }

    private void openDirectory(String directory) {
        currentDirectory = directory;
        refreshDirectoryContent(directory);
        currentPath.setText(currentPath.getText() + PATH_SEPARATOR + directory);
        prevDirButton.setDisable(false);
    }

    private void refreshDirectoryContent(String directory) {
        filesList.getItems().clear();
        filesList.getItems().addAll(catalog.get(directory));
        prevDirButton.requestFocus();
    }

    private void disableMenuItems(List<MenuItem> items) {
        items.forEach(item -> item.setDisable(true));
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
