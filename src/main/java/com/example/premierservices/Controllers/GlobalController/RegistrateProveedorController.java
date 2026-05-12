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

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        Connection con = null;

        try {
            con = conectar();
            if (con == null) {
                mostrarMensaje("Error de conexión a la base de datos", "error");
                return;
            }

            // Verificar si el email ya existe
            if (emailYaExiste(email, con)) {
                mostrarMensaje("El correo ya está registrado. Usa otro o inicia sesión.", "error");
                return;
            }

            con.setAutoCommit(false);

            // Insertar usuario
            String sqlUser = "INSERT INTO tbl_usuarios (nombre, email, contraseña, tipo_usuario, fecha_registro, estado) VALUES (?, ?, ?, 'suplidor', GETDATE(), 'activo')";
            int idUsuario;
            try (PreparedStatement pst = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, nombre);
                pst.setString(2, email);
                pst.setString(3, hashedPassword);
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                rs.next();
                idUsuario = rs.getInt(1);
            }

            // Insertar suplidor
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

            // Obtener id_suplidor
            int idSuplidor = -1;
            String sqlGetSuplidor = "SELECT id_suplidor FROM tbl_suplidores WHERE id_usuario = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlGetSuplidor)) {
                pst.setInt(1, idUsuario);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    idSuplidor = rs.getInt("id_suplidor");
                }
            }

            // Insertar suscripción inicial (plan provisional)
            String sqlSuscripcion = "INSERT INTO tbl_suscripciones (id_suplidor, id_plan, fecha_inicio, fecha_fin, estado) VALUES (?, 1, GETDATE(), DATEADD(month, 1, GETDATE()), 'activa')";
            try (PreparedStatement pst = con.prepareStatement(sqlSuscripcion)) {
                pst.setInt(1, idSuplidor);
                pst.executeUpdate();
            }

            con.commit();
            mostrarMensaje("Registro exitoso. Elige tu plan de suscripción.", "exito");

            // Abrir SeleccionPlan.fxml (asegúrate de que esté en la raíz de resources)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SeleccionPlan.fxml"));
            Parent root = loader.load();
            SeleccionPlanController planController = loader.getController();
            planController.setIdSuplidor(idSuplidor);

            Stage stage = (Stage) registrarButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Premier Services — Elige tu Plan");
            stage.centerOnScreen();
            stage.show();

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            if (e.getMessage().contains("UNIQUE KEY") || e.getMessage().contains("duplicate")) {
                mostrarMensaje("El correo ya está registrado. Usa otro o inicia sesión.", "error");
            } else {
                mostrarMensaje("Error al registrar: " + e.getMessage(), "error");
            }
            e.printStackTrace();
        } catch (IOException e) {
            mostrarMensaje("No se encontró la pantalla de selección de planes. Verifica la ruta de SeleccionPlan.fxml", "error");
            e.printStackTrace();
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

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
        if ("error".equals(tipo))
            lblMensajePersonal.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        else
            lblMensajePersonal.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
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

    // Nuevo método para verificar si el email ya existe
    private boolean emailYaExiste(String email, Connection con) throws SQLException {
        String sql = "SELECT 1 FROM tbl_usuarios WHERE email = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        }
    }
}