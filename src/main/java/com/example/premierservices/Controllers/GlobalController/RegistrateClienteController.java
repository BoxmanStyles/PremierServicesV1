package com.example.premierservices.Controllers.GlobalController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;

public class RegistrateClienteController {

    @FXML private TextField nombreField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private CheckBox terminosCheckBox;
    @FXML private Label lblMensaje;
    @FXML private Button registrarButton, cancelarButton;

    @FXML
    public void initialize() {
        lblMensaje.setText("");
        nombreField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        emailField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        passwordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        confirmPasswordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
    }

    @FXML
    private void onRegistrarClick() {
        if (!validarCampos()) return;

        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        boolean terminos = terminosCheckBox.isSelected();

        if (!terminos) {
            mostrarMensaje("Debe aceptar los términos y condiciones", "error");
            return;
        }
        if (!password.equals(confirmPasswordField.getText())) {
            mostrarMensaje("Las contraseñas no coinciden", "error");
            return;
        }

        // Generar hash de la contraseña
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Connection con = null;
        try {
            con = conectar();
            con.setAutoCommit(false);

            // 1. Insertar usuario con contraseña hasheada
            String sqlUser = "INSERT INTO tbl_usuarios (nombre, email, contraseña, tipo_usuario, fecha_registro, estado) VALUES (?, ?, ?, 'cliente', GETDATE(), 'activo')";
            int idUsuario;
            try (PreparedStatement pst = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, nombre);
                pst.setString(2, email);
                pst.setString(3, hashedPassword);
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    idUsuario = rs.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID del usuario");
                }
            }

            // 2. Insertar cliente con TODOS los datos
            String sqlCliente = "INSERT INTO tbl_clientes (id_usuario, nombre, apellido, email, telefono, direccion, contrasena, tipo_cliente) VALUES (?, ?, ?, ?, ?, ?, ?, 'personal')";
            try (PreparedStatement pst = con.prepareStatement(sqlCliente)) {
                pst.setInt(1, idUsuario);
                pst.setString(2, nombre);
                pst.setString(3, ""); // Apellido vacío por defecto (puedes agregar un campo en el formulario)
                pst.setString(4, email);
                pst.setString(5, ""); // Teléfono - puedes agregar un campo en el formulario
                pst.setString(6, ""); // Dirección - puedes agregar un campo en el formulario
                pst.setString(7, hashedPassword);
                pst.executeUpdate();
            }

            con.commit();
            mostrarMensaje("Registro exitoso. Ahora inicia sesión.", "exito");

            // Redirigir al login después de 2 segundos
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> irALogin(null));
                }
            }, 2000);

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            // Mostrar mensaje de error más amigable
            if (e.getMessage().contains("duplicate") || e.getMessage().contains("UNIQUE")) {
                mostrarMensaje("El correo electrónico ya está registrado", "error");
            } else {
                mostrarMensaje("Error al registrar: " + e.getMessage(), "error");
            }
            e.printStackTrace();
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @FXML private void onCancelarClick() { limpiarCampos(); }
    @FXML private void irALogin(ActionEvent event) { navegarA("/LoginGeneralV2.fxml", "Iniciar Sesión", event); }
    @FXML private void volverAOpcionRegister(ActionEvent event) { navegarA("/OpcionRegister.fxml", "Elegir tipo de registro", event); }

    private void navegarA(String fxml, String titulo, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (event != null) ? (Stage) ((Node) event.getSource()).getScene().getWindow() : (Stage) registrarButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean validarCampos() {
        if (nombreField.getText().trim().isEmpty()) {
            mostrarMensaje("Ingrese su nombre completo", "error"); return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            mostrarMensaje("Ingrese su correo electrónico", "error"); return false;
        }
        if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarMensaje("Correo inválido", "error"); return false;
        }
        if (passwordField.getText().isEmpty()) {
            mostrarMensaje("Ingrese una contraseña", "error"); return false;
        }
        if (passwordField.getText().length() < 8) {
            mostrarMensaje("Mínimo 8 caracteres", "error"); return false;
        }
        return true;
    }

    private void limpiarCampos() {
        nombreField.clear(); emailField.clear(); passwordField.clear(); confirmPasswordField.clear();
        terminosCheckBox.setSelected(false);
        limpiarMensaje();
    }

    private void limpiarMensaje() { lblMensaje.setText(""); lblMensaje.setVisible(false); }

    private void mostrarMensaje(String msg, String tipo) {
        lblMensaje.setText(msg);
        lblMensaje.setVisible(true);
        if ("error".equals(tipo)) {
            lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
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