package com.example.premierservices.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class RegistrateProveedorController {

    @FXML private TextField nombreField, emailField, telefonoField, nombreEmpresaField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<String> ubicacionComboBox;
    @FXML private TextArea descripcionArea;
    @FXML private CheckBox terminosCheckBox;
    @FXML private Label lblMensajePersonal;
    @FXML private Button registrarButton;

    private final List<String> ubicaciones = Arrays.asList(
            "Santo Domingo", "Santiago", "La Vega", "Puerto Plata",
            "San Pedro de Macorís", "La Romana", "Higüey"
    );

    @FXML
    public void initialize() {
        ubicacionComboBox.getItems().addAll(ubicaciones);
        lblMensajePersonal.setText("");
        nombreField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        emailField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        passwordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        confirmPasswordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        telefonoField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        nombreEmpresaField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        ubicacionComboBox.valueProperty().addListener((obs, o, n) -> limpiarMensaje());
    }

    @FXML
    private void onRegistrarClick() {
        if (!validarCampos()) return;

        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String telefono = telefonoField.getText().trim();
        String nombreEmpresa = nombreEmpresaField.getText().trim();
        String ubicacion = ubicacionComboBox.getValue();
        String descripcion = descripcionArea.getText().trim();
        boolean terminos = terminosCheckBox.isSelected();

        if (!terminos) {
            mostrarMensaje("Debe aceptar los términos y condiciones", "error");
            return;
        }
        if (!password.equals(confirmPasswordField.getText())) {
            mostrarMensaje("Las contraseñas no coinciden", "error");
            return;
        }

        Connection con = null;
        try {
            con = conectar();
            con.setAutoCommit(false);

            // 1. Insertar usuario
            String sqlUser = "INSERT INTO tbl_usuarios (nombre, email, contraseña, tipo_usuario, fecha_registro, estado) VALUES (?, ?, ?, 'suplidor', GETDATE(), 'activo')";
            int idUsuario;
            try (PreparedStatement pst = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, nombre);
                pst.setString(2, email);
                pst.setString(3, password);
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                rs.next();
                idUsuario = rs.getInt(1);
            }

            // 2. Insertar suplidor
            String sqlSuplidor = "INSERT INTO tbl_suplidores (id_usuario, nombre_empresa, descripcion, ubicacion, telefono, plan_id, calificacion_promedio, correo) VALUES (?, ?, ?, ?, ?, 1, 0, ?)";
            try (PreparedStatement pst = con.prepareStatement(sqlSuplidor)) {
                pst.setInt(1, idUsuario);
                pst.setString(2, nombreEmpresa);
                pst.setString(3, descripcion);
                pst.setString(4, ubicacion);
                pst.setString(5, telefono);
                pst.setString(6, email);
                pst.executeUpdate();
            }

            // 3. Insertar suscripción inicial
            String sqlSuscripcion = "INSERT INTO tbl_suscripciones (id_suplidor, id_plan, fecha_inicio, fecha_fin, estado) VALUES ((SELECT id_suplidor FROM tbl_suplidores WHERE id_usuario = ?), 1, GETDATE(), DATEADD(month, 1, GETDATE()), 'activa')";
            try (PreparedStatement pst = con.prepareStatement(sqlSuscripcion)) {
                pst.setInt(1, idUsuario);
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
            mostrarMensaje("Error al registrar: " + e.getMessage(), "error");
            e.printStackTrace();
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @FXML
    private void irALogin(ActionEvent event) {
        navegarA("/LoginGeneralV2.fxml", "Iniciar Sesión", event);
    }

    @FXML
    private void volverAOpcionRegister(ActionEvent event) {
        navegarA("/OpcionRegister.fxml", "Elegir tipo de registro", event);
    }

    private void navegarA(String fxml, String titulo, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) registrarButton.getScene().getWindow();
            }
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validarCampos() {
        if (nombreField.getText().trim().isEmpty()) {
            mostrarMensaje("Ingrese su nombre", "error"); return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            mostrarMensaje("Ingrese su correo", "error"); return false;
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
        if (telefonoField.getText().trim().isEmpty()) {
            mostrarMensaje("Ingrese teléfono", "error"); return false;
        }
        if (nombreEmpresaField.getText().trim().isEmpty()) {
            mostrarMensaje("Nombre de empresa requerido", "error"); return false;
        }
        if (ubicacionComboBox.getValue() == null) {
            mostrarMensaje("Seleccione ubicación", "error"); return false;
        }
        return true;
    }

    private void limpiarMensaje() { lblMensajePersonal.setText(""); lblMensajePersonal.setVisible(false); }

    private void mostrarMensaje(String msg, String tipo) {
        lblMensajePersonal.setText(msg);
        lblMensajePersonal.setVisible(true);
        if (tipo.equals("error"))
            lblMensajePersonal.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        else if (tipo.equals("exito"))
            lblMensajePersonal.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        else
            lblMensajePersonal.setStyle("-fx-text-fill: #34495e;");
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