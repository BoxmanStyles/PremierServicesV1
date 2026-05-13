package com.example.premierservices.Controllers.GlobalController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class SeleccionPlanController {

    @FXML private Button btnAtras;
    @FXML private Button btnRookie;
    @FXML private Button btnElite;
    @FXML private Button btnPrime;
    @FXML private Button btnSiguiente;
    @FXML private Label labelSeleccionado;

    private String planSeleccionado = null;
    private int idSuplidor;

    @FXML
    public void initialize() {
        System.out.println("SeleccionPlanController inicializado");
    }

    public void setIdSuplidor(int id) {
        this.idSuplidor = id;
        System.out.println("ID Suplidor recibido: " + idSuplidor);
    }

    @FXML
    void irARegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegistrateProveedor.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Proveedor - Premier Services");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver al registro: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onRookieClicked(MouseEvent event) {
        planSeleccionado = "Rookie";
        labelSeleccionado.setText("Plan seleccionado: Rookie (Gratis)");
        actualizarBotonSiguiente();
    }

    @FXML
    void onEliteClicked(MouseEvent event) {
        planSeleccionado = "Elite";
        labelSeleccionado.setText("Plan seleccionado: Elite - USD 29/mes");
        actualizarBotonSiguiente();
    }

    @FXML
    void onPrimeClicked(MouseEvent event) {
        planSeleccionado = "Prime";
        labelSeleccionado.setText("Plan seleccionado: Prime - USD 79/mes");
        actualizarBotonSiguiente();
    }

    @FXML
    void seleccionarRookie(ActionEvent event) {
        planSeleccionado = "Rookie";
        labelSeleccionado.setText("Plan seleccionado: Rookie (Gratis)");
        actualizarBotonSiguiente();
    }

    @FXML
    void seleccionarElite(ActionEvent event) {
        planSeleccionado = "Elite";
        labelSeleccionado.setText("Plan seleccionado: Elite - USD 29/mes");
        actualizarBotonSiguiente();
    }

    @FXML
    void seleccionarPrime(ActionEvent event) {
        planSeleccionado = "Prime";
        labelSeleccionado.setText("Plan seleccionado: Prime - USD 79/mes");
        actualizarBotonSiguiente();
    }

    private void actualizarBotonSiguiente() {
        btnSiguiente.setStyle("-fx-background-color:#4A7FA9; -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:15px; -fx-background-radius:12; -fx-padding:13 60 13 60;");
    }

    @FXML
    void guardarSeleccion(ActionEvent event) {
        if (planSeleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione un plan para continuar.", Alert.AlertType.WARNING);
            return;
        }

        System.out.println("Plan seleccionado: " + planSeleccionado);
        System.out.println("ID Suplidor: " + idSuplidor);

        guardarPlanEnBaseDatos(planSeleccionado);
        irALogin(event);
    }

    private void guardarPlanEnBaseDatos(String plan) {
        System.out.println("Guardando plan: " + plan + " para el suplidor: " + idSuplidor);
        // Aquí puedes agregar la lógica para guardar el plan en la base de datos si es necesario
    }

    private void irALogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginGeneral.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar Sesión - Premier Services");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error al cargar LoginGeneral.fxml: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo cargar la pantalla de inicio de sesión.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}