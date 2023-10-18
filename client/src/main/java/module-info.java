module ru.tinkoff.semenov {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.all;


    opens ru.tinkoff.semenov to javafx.fxml;
    opens ru.tinkoff.semenov.controllers to javafx.fxml;
    opens ru.tinkoff.semenov.enums to javafx.fxml;
    exports ru.tinkoff.semenov;
    exports ru.tinkoff.semenov.controllers;
    exports ru.tinkoff.semenov.enums;
}