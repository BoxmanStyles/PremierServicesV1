package com.example.premierservices.Controllers.GlobalController;

import com.example.premierservices.Models.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.sql.*;

public class LoginGeneralControllerV2 {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label lblMensaje;
    @FXML private Button AtrasLogin;

    @FXML
    public void initialize() {
        lblMensaje.setText("");
        emailField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
        passwordField.textProperty().addListener((obs, o, n) -> limpiarMensaje());
    }

    @FXML
    private void onLoginClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Complete todos los campos", "error");
            return;
        }

        try (Connection con = conectar()) {
            String sql = "SELECT u.id_usuario, u.contraseña, u.tipo_usuario, u.nombre, " +
                    "c.id_cliente, s.id_suplidor " +
                    "FROM tbl_usuarios u " +
                    "LEFT JOIN tbl_clientes c ON u.id_usuario = c.id_usuario " +
                    "LEFT JOIN tbl_suplidores s ON u.id_usuario = s.id_usuario " +
                    "WHERE u.email = ? AND u.estado = 'activo'";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, email);
                ResultSet rs = pst.executeQuery();

                if (!rs.next()) {
                    mostrarMensaje("Cuenta no existente", "error");
                    return;
                }

                String storedPassword = rs.getString("contraseña");
                boolean passwordOk;

                // Verificar si la contraseña almacenada es un hash BCrypt (empieza con $2a$)
                if (storedPassword.startsWith("$2a$")) {
                    passwordOk = BCrypt.checkpw(password, storedPassword);
                } else {
                    // Migración: comparar en texto plano (para usuarios antiguos)
                    passwordOk = storedPassword.equals(password);
                    if (passwordOk) {
                        // Actualizar a hash BCrypt
                        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                        String updateSql = "UPDATE tbl_usuarios SET contraseña = ? WHERE id_usuario = ?";
                        try (PreparedStatement updateStmt = con.prepareStatement(updateSql)) {
                            updateStmt.setString(1, hashed);
                            updateStmt.setInt(2, rs.getInt("id_usuario"));
                            updateStmt.executeUpdate();
                        }
                    }
                }

                if (!passwordOk) {
                    mostrarMensaje("Contraseña incorrecta", "error");
                    passwordField.clear();
                    return;
                }

                int idUsuario = rs.getInt("id_usuario");
                String tipo = rs.getString("tipo_usuario");
                String nombre = rs.getString("nombre");
                Sesion.setIdUsuario(idUsuario);
                Sesion.setNombre(nombre);
                Sesion.setTipo(tipo);

                if ("cliente".equals(tipo)) {
                    int idCliente = rs.getInt("id_cliente");
                    Sesion.setIdCliente(idCliente);
                    abrirPantallaCliente();
                } else if ("suplidor".equals(tipo)) {
                    int idSuplidor = rs.getInt("id_suplidor");
                    Sesion.setIdSuplidor(idSuplidor);
                    abrirPantallaProveedor();
                } else if ("admin".equals(tipo)) {
                    abrirPantallaAdmin();
                } else if ("gestor_suplidor".equals(tipo)) {
                    abrirPantallaGestorSuplidores();
                } else if ("gestor_factura".equals(tipo)) {
                    abrirPantallaGestorFacturacion();
                } else {
                    mostrarMensaje("Tipo de usuario no soportado", "error");
                }
            }
        } catch (SQLException e) {
            mostrarMensaje("Error de conexión: " + e.getMessage(), "error");
            e.printStackTrace();
        } catch (IOException e) {
            mostrarMensaje("Error al cargar la pantalla: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void abrirPantallaCliente() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaginaPrincipal(Sesion_Iniciada).fxml"));
        Parent root = loader.load();
        com.example.premierservices.Controllers.Clientes.PaginaPrincipalSesionController controller = loader.getController();
        controller.setDatosCliente(Sesion.getIdCliente(), Sesion.getNombre(), null);
        cambiarEscena(root, "Premier Services - Cliente");
    }

    private void abrirPantallaProveedor() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProveedorDashboard.fxml"));
        Parent root = loader.load();
        com.example.premierservices.Controllers.Proveedores.ProveedorDashboardController controller = loader.getController();
        controller.setIdSuplidor(Sesion.getIdSuplidor());
        cambiarEscena(root, "Premier Services - Panel Proveedor");
    }

    private void abrirPantallaAdmin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminPaginaPrincipal.fxml"));
        Parent root = loader.load();
        cambiarEscena(root, "Premier Services - Administración");
    }

    private void abrirPantallaGestorSuplidores() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestorSuplidoresPanel.fxml"));
        Parent root = loader.load();
        cambiarEscena(root, "Premier Services - Gestión de Suplidores");
    }

    private void abrirPantallaGestorFacturacion() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestorFacturacionPanel.fxml"));
        Parent root = loader.load();
        cambiarEscena(root, "Premier Services - Gestión de Facturación");
    }

    private void cambiarEscena(Parent root, String titulo) {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(titulo);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML private void onRegisterClick() { navegarA("/OpcionRegister.fxml", "Elegir tipo de registro"); }
    @FXML private void volverAPaginaPrincipal() { navegarA("/PaginaPrincipal(Sin_Sesion).fxml", "Premier Services - Eventos"); }

    private void navegarA(String fxml, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) AtrasLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarMensaje(String msg, String tipo) {
        lblMensaje.setText(msg);
        lblMensaje.setVisible(true);
        if ("error".equals(tipo))
            lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        else
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void limpiarMensaje() { lblMensaje.setText(""); lblMensaje.setVisible(false); }

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