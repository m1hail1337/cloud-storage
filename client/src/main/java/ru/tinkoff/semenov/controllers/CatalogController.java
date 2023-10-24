package ru.tinkoff.semenov.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import ru.tinkoff.semenov.Action;
import ru.tinkoff.semenov.Network;

import java.net.URL;
import java.util.ResourceBundle;

public class CatalogController implements Initializable {

    @FXML
    private Button turnOff;
    private Network network;
    private final Action action = args -> {
        // TODO: Реализовать логику для взаимодействия с сервером
    };

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        turnOff.setOnAction(event -> {
            network.close();
        });
    }
}
