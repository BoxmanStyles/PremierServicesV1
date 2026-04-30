package com.example.premierservices.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AdminPanelController {

    @FXML private Button btnGestionClientes;
    @FXML private Button btnGestionProveedores;
    @FXML private Button btnGestionPagosFacturas;
    @FXML private Button btnGestionReservas;
    @FXML private Button btnGestionPortafolios;
    @FXML private Button btnDashboard;


    private void abrirVentana(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) btnGestionClientes.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onGestionClientesClick(ActionEvent event) {
        abrirVentana("/AdminGestionCliente.fxml", "Gestión de Clientes - Premier Services");
    }

    @FXML
    void onGestionProveedoresClick(ActionEvent event) {
        abrirVentana("/AdminGestionProveedor.fxml", "Gestión de Proveedores - Premier Services");
    }

    @FXML
    void onGestionPagosFacturasClick(ActionEvent event) {
        abrirVentana("/AdminGestionPagosFacturas.fxml", "Gestión de Pagos y Facturas - Premier Services");
    }

    @FXML
    void onGestionReservasClick(ActionEvent event) {
        mostrarAlerta("En desarrollo", "La pantalla de Gestión de Reservas está en desarrollo.", Alert.AlertType.INFORMATION);
    }

    @FXML
    void onGestionPortafoliosClick(ActionEvent event) {
        abrirVentana("/AdminEditorPortafolio.fxml", "Gestion de los Portafolios - Premier Services");
    }

    @FXML
    void onDashboardClick(ActionEvent event) {
        abrirVentana("/AdminDashBoard.fxml", "DashBoard - Premier Services");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}