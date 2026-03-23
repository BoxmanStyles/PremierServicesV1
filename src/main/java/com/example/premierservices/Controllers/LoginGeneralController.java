package com.example.premierservices.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class LoginGeneralController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label lblMensaje;
    @FXML private Button loginButton;

    private Connection conectar() {
        try {
            String url = "jdbc:sqlserver://26.228.126.202:1433;" +
                    "databaseName=PremierServicesV1;" +
                    "encrypt=true;trustServerCertificate=true";
            return DriverManager.getConnection(url, "wilenny", "1234");
        } catch (Exception e) {
            mostrarAlerta("Error de conexión", e.getMessage());
            return null;
        }
    }

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        verificarCredenciales(email, pass);
    }

    private void verificarCredenciales(String email, String pass) {
        Connection con = conectar();
        if (con == null) return;

        String sql = "SELECT nombre, tipo_usuario FROM dbo.tbl_usuarios WHERE email = ? AND contraseña = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("nombre");
                String tipo = rs.getString("tipo_usuario");

                lblMensaje.setText("¡Bienvenido, " + nombre + "!");
                lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

                System.out.println("Login exitoso: " + nombre + " (" + tipo + ")");

            } else {
                mostrarError("Correo o contraseña incorrectos.");
            }

        } catch (SQLException e) {
            mostrarAlerta("Error en la base de datos", e.getMessage());
        } finally {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }

    @FXML
    protected void onRegistroProveedorClick() {
        System.out.println("Navegando a Registro de Proveedor...");
    }

    @FXML
    protected void onRecuperarPasswordClick() {
        lblMensaje.setText("Funcionalidad de recuperación en desarrollo.");
        lblMensaje.setStyle("-fx-text-fill: #3498db;");
    }

    private void mostrarError(String msg) {
        lblMensaje.setText("❌ " + msg);
        lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}