package com.example.premierservices.Controllers.GlobalController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SeleccionPlanController {

    @FXML private Label signoElite;
    @FXML private Label signoPrime;
    @FXML private Label labelSeleccionado;
    @FXML private Button btnRookie;
    @FXML private Button btnElite;
    @FXML private Button btnPrime;
    @FXML private Button btnSiguiente;
    @FXML private Button btnAtras;

    private int idSuplidor;
    private int planSeleccionadoId = -1; // 1=Rookie, 2=Elite, 3=Prime

    @FXML
    public void initialize() {
        // Asignar el símbolo $ desde código para evitar problemas de expresión en FXML
        if (signoElite != null) signoElite.setText("$");
        if (signoPrime != null) signoPrime.setText("$");

        // Deshabilitar botón "Continuar" hasta que se seleccione un plan
        btnSiguiente.setDisable(true);
        btnSiguiente.setStyle("-fx-background-color:#cbd5e1; -fx-text-fill:#94a3b8;");
    }

    public void setIdSuplidor(int idSuplidor) {
        this.idSuplidor = idSuplidor;
    }

    @FXML
    private void seleccionarRookie() {
        planSeleccionadoId = 1;
        labelSeleccionado.setText("Plan seleccionado: Rookie (Gratis)");
        habilitarContinuar();
    }

    @FXML
    private void seleccionarElite() {
        planSeleccionadoId = 2;
        labelSeleccionado.setText("Plan seleccionado: Elite - $29/mes");
        habilitarContinuar();
    }

    @FXML
    private void seleccionarPrime() {
        planSeleccionadoId = 3;
        labelSeleccionado.setText("Plan seleccionado: Prime - $79/mes");
        habilitarContinuar();
    }

    private void habilitarContinuar() {
        btnSiguiente.setDisable(false);
        btnSiguiente.setStyle("-fx-background-color:#4A7FA9; -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:15px; -fx-background-radius:12; -fx-padding:13 60 13 60;");
    }

    @FXML
    private void guardarSeleccion() {
        if (planSeleccionadoId == -1) {
            labelSeleccionado.setText("⚠️ Por favor, selecciona un plan primero");
            return;
        }

        // Actualizar el plan en la base de datos
        try (Connection con = conectar()) {
            String sql = "UPDATE tbl_suscripciones SET id_plan = ?, fecha_inicio = GETDATE(), fecha_fin = DATEADD(month, 1, GETDATE()) WHERE id_suplidor = ? AND estado = 'activa'";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, planSeleccionadoId);
                pst.setInt(2, idSuplidor);
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    labelSeleccionado.setText("✅ Plan actualizado correctamente. Redirigiendo...");
                    // Redirigir al dashboard del proveedor (o login)
                    irADashboard();
                } else {
                    labelSeleccionado.setText("❌ Error al guardar el plan. Intenta de nuevo.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            labelSeleccionado.setText("❌ Error de base de datos: " + e.getMessage());
        }
    }

    private void irADashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardProveedor.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnSiguiente.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Premier Services - Dashboard");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            labelSeleccionado.setText("No se pudo cargar el dashboard. Revisa la ruta del FXML.");
        }
    }

    @FXML
    private void volverARegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegistrateProveedor.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAtras.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Proveedor");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Connection conectar() {
        try {
            String url = "jdbc:sqlserver://26.228.126.202:1433;databaseName=PremierServicesV1;encrypt=true;trustServerCertificate=true";
            return DriverManager.getConnection(url, "wilenny", "1234");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}