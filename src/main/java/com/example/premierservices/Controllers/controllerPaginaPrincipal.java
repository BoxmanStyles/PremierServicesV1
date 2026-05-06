package com.example.premierservices.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class controllerPaginaPrincipal {
    @FXML
    private Pane PanelBuscador;

    @FXML
    protected void Buscador(ActionEvent actionEvent) {
        PanelBuscador.setVisible(true);

    }
    @FXML
    protected void SalirBuscar(ActionEvent actionEvent) {
        PanelBuscador.setVisible(false);
    }
}
