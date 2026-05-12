package com.example.premierservices.Controllers.Admin;

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

    // Botón "X" - Regresa a la pantalla principal de administración
    @FXML
    private void SalirAdminPanel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminPaginaPrincipal.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) btnGestionClientes.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Administración de Servicios - Premier Services");
            currentStage.setMaximized(true);
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar a la pantalla principal: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void Buscador(ActionEvent event) {
        mostrarAlerta("Búsqueda", "Funcionalidad de búsqueda en desarrollo.", Alert.AlertType.INFORMATION);
    }

    private void abrirVentana(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
            // No cerramos la ventana actual para permitir regresar fácilmente
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML void onGestionClientesClick(ActionEvent event) {
        abrirVentana("/AdminGestionCliente.fxml", "Gestión de Clientes - Premier Services");
    }

    @FXML void onGestionProveedoresClick(ActionEvent event) {
        abrirVentana("/AdminGestionProveedor.fxml", "Gestión de Proveedores - Premier Services");
    }

    @FXML void onGestionPagosFacturasClick(ActionEvent event) {
        abrirVentana("/AdminGestionPagosFacturas.fxml", "Gestión de Pagos y Facturas - Premier Services");
    }

    @FXML void onGestionReservasClick(ActionEvent event) {
        mostrarAlerta("En desarrollo", "La pantalla de Gestión de Reservas está en desarrollo.", Alert.AlertType.INFORMATION);
    }

    @FXML void onGestionPortafoliosClick(ActionEvent event) {
        abrirVentana("/AdminEditorPortafolio.fxml", "Gestión de Portafolios - Premier Services");
    }

    @FXML void onDashboardClick(ActionEvent event) {
        abrirVentana("/AdminDashBoard.fxml", "Dashboard - Premier Services");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}