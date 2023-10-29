package ru.tinkoff.semenov.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;


public class CatalogController implements Initializable {

    private static final String PATH_SEPARATOR = "\\";

    @FXML
    private ListView<String> filesList;
    @FXML
    private TextField currentPath;
    private Network network;
    private String root;

    private final Map<String, List<String>> catalog = new HashMap<>();

    private final Action action = args -> {

    };

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem open = new MenuItem("Открыть");
        MenuItem cut = new MenuItem("Вырезать");
        MenuItem copy = new MenuItem("Копировать");
        MenuItem delete = new MenuItem("Удалить");
        contextMenu.getItems().addAll(open, cut, copy, delete);
        filesList.setContextMenu(contextMenu);
        filesList.setOnContextMenuRequested(event -> {
            contextMenu.show(filesList, event.getScreenX(), event.getScreenY());
        });
    }

    public void exitApplication(ActionEvent event) {
        network.close();
        Platform.exit();
    }

    public void initCatalog(String[] paths) {
        this.root = paths[0];
        currentPath.setText(root + PATH_SEPARATOR);
        for (String path : paths) {
            String[] locations = path.split("\\\\");
            for (int i = 0; i < locations.length - 1; i++) {
                catalog.getOrDefault(locations[i], new ArrayList<>()).add(locations[i + 1]);
            }
            if (!locations[locations.length - 1].contains(".")) {
                catalog.put(locations[locations.length - 1], new ArrayList<>());
            }
        }
        filesList.getItems().addAll(catalog.get(root));
    }
}
