package com.example.premierservices.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class RegistroProveedorController {

    @FXML private TextField        nombreField;
    @FXML private TextField        emailField;
    @FXML private PasswordField    passwordField;
    @FXML private PasswordField    confirmPasswordField;
    @FXML private TextField        nombreEmpresaField;
    @FXML private TextField        telefonoField;
    @FXML private ComboBox<String> ubicacionComboBox;
    @FXML private TextArea         descripcionArea;
    @FXML private CheckBox         terminosCheckBox;
    @FXML private Button           registrarButton;
    @FXML private Label            lblMensaje;

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
    public void initialize() {
        System.out.println("Registro de Proveedores");

        ubicacionComboBox.getItems().addAll(
                "Santo Domingo",
                "Santiago",
                "La Vega",
                "Puerto Plata",
                "San Pedro de Macorís",
                "La Romana",
                "San Cristóbal",
                "Moca",
                "San Francisco de Macorís",
                "Higüey",
                "Punta Cana",
                "Boca Chica"
        );

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !esEmailValido(newVal)) {
                emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-font-size: 14; -fx-padding: 10;");
            } else {
                emailField.setStyle("-fx-font-size: 14; -fx-padding: 10;");
            }
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.equals(passwordField.getText())) {
                confirmPasswordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-font-size: 14; -fx-padding: 10;");
            } else {
                confirmPasswordField.setStyle("-fx-font-size: 14; -fx-padding: 10;");
            }
        });
    }


    @FXML
    protected void onRegistrarClick() {
        System.out.println("Registrar");
        lblMensaje.setText("");

        if (!validarCampos()) return;

        if (!terminosCheckBox.isSelected()) {
            mostrarError("Debe aceptar los términos y condiciones.");
            return;
        }

        String nombre        = nombreField.getText().trim();
        String email         = emailField.getText().trim();
        String password      = passwordField.getText();
        String nombreEmpresa = nombreEmpresaField.getText().trim();
        String telefono      = telefonoField.getText().trim();
        String ubicacion     = ubicacionComboBox.getValue();
        String descripcion   = descripcionArea.getText().trim();

        registrarProveedor(nombre, email, password, nombreEmpresa, telefono, ubicacion, descripcion);
    }


    private void registrarProveedor(String nombre, String email, String password,
                                    String nombreEmpresa, String telefono,
                                    String ubicacion, String descripcion) {

        Connection con = conectar();
        if (con == null) return;

        try {
            String sqlVerificar = "SELECT COUNT(*) FROM dbo.tbl_usuarios WHERE email = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlVerificar)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    mostrarError("Ya existe una cuenta con ese correo electrónico.");
                    return;
                }
            }


            String sqlUsuario = "INSERT INTO dbo.tbl_usuarios (nombre, email, contraseña, tipo_usuario) " + "VALUES (?, ?, ?, 'suplidor')";
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setString(1, nombre);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.executeUpdate();
            }

            int idUsuario = -1;
            String sqlGetId = "SELECT id_usuario FROM dbo.tbl_usuarios WHERE email = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlGetId)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    idUsuario = rs.getInt("id_usuario");
                }
            }

            if (idUsuario == -1) {
                mostrarError("No se pudo obtener el ID del usuario. Intente de nuevo.");
                return;
            }

            String sqlSuplidor = "INSERT INTO dbo.tbl_suplidores " +
                    "(id_usuario, nombre_empresa, descripcion, ubicacion, telefono, plan_id, calificacion_promedio) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlSuplidor)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, nombreEmpresa);
                ps.setString(3, descripcion);
                ps.setString(4, ubicacion);
                ps.setString(5, telefono);
                ps.setInt(6, 1);
                ps.setDouble(7, 0.0);
                ps.executeUpdate();
            }

            mostrarInfo("Registro exitoso", "¡Bienvenido, " + nombre + "! Tu cuenta fue creada.");
            System.out.println("Proveedor registrado correctamente: " + email);
            limpiarFormulario();
            lblMensaje.setText("✓ ¡Bienvenido, " + nombre + "! Tu cuenta fue creada.");
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13;");

        } catch (SQLException e) {
            mostrarAlerta("Error al registrar", e.getMessage());
            System.err.println("SQLException: " + e.getMessage());
        } finally {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }


    @FXML
    protected void onLoginClick() {
        System.out.println("🔗 Link a Login presionado");
        // TODO: Navegar a la pantalla de Login cuando esté lista
        // Stage stage = (Stage) registrarButton.getScene().getWindow();
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/premierservices/Login.fxml"));
        // stage.setScene(new Scene(loader.load()));
    }


    private boolean validarCampos() {

        if (nombreField.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio.");
            nombreField.requestFocus();
            return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            mostrarError("El correo electrónico es obligatorio.");
            emailField.requestFocus();
            return false;
        }
        if (!esEmailValido(emailField.getText().trim())) {
            mostrarError("El formato del correo no es válido.");
            emailField.requestFocus();
            return false;
        }
        if (passwordField.getText().isEmpty()) {
            mostrarError("La contraseña es obligatoria.");
            passwordField.requestFocus();
            return false;
        }
        if (passwordField.getText().length() < 8) {
            mostrarError("La contraseña debe tener al menos 8 caracteres.");
            passwordField.requestFocus();
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            mostrarError("Las contraseñas no coinciden.");
            confirmPasswordField.requestFocus();
            return false;
        }
        if (nombreEmpresaField.getText().trim().isEmpty()) {
            mostrarError("El nombre de la empresa es obligatorio.");
            nombreEmpresaField.requestFocus();
            return false;
        }
        if (telefonoField.getText().trim().isEmpty()) {
            mostrarError("El teléfono es obligatorio.");
            telefonoField.requestFocus();
            return false;
        }
        if (ubicacionComboBox.getValue() == null) {
            mostrarError("Debe seleccionar una ubicación.");
            ubicacionComboBox.requestFocus();
            return false;
        }
        if (descripcionArea.getText().trim().isEmpty()) {
            mostrarError("La descripción es obligatoria.");
            descripcionArea.requestFocus();
            return false;
        }
        if (descripcionArea.getText().trim().length() < 20) {
            mostrarError("La descripción debe tener al menos 20 caracteres.");
            descripcionArea.requestFocus();
            return false;
        }

        return true;
    }

    private boolean esEmailValido(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }


    private void limpiarFormulario() {
        nombreField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        nombreEmpresaField.clear();
        telefonoField.clear();
        ubicacionComboBox.setValue(null);
        descripcionArea.clear();
        terminosCheckBox.setSelected(false);
        lblMensaje.setText("");
        emailField.setStyle("-fx-font-size: 14; -fx-padding: 10;");
        confirmPasswordField.setStyle("-fx-font-size: 14; -fx-padding: 10;");
    }


    private void mostrarError(String msg) {
        lblMensaje.setText("❌ " + msg);
        lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 13;");
        System.err.println("❌ " + msg);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}