package com.example.premierservices.Controllers;

import com.example.premierservices.Models.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
            String sqlCheck = "SELECT id_usuario, contraseña, tipo_usuario, nombre FROM tbl_usuarios WHERE email = ? AND estado = 'activo'";
            try (PreparedStatement pstCheck = con.prepareStatement(sqlCheck)) {
                pstCheck.setString(1, email);
                ResultSet rsCheck = pstCheck.executeQuery();

                if (!rsCheck.next()) {
                    mostrarMensaje("Cuenta no existente", "error");
                    return;
                }

                String dbPassword = rsCheck.getString("contraseña");
                if (!dbPassword.equals(password)) {
                    mostrarMensaje("Contraseña incorrecta", "error");
                    passwordField.clear();
                    return;
                }

                int idUsuario = rsCheck.getInt("id_usuario");
                String tipo = rsCheck.getString("tipo_usuario");
                String nombre = rsCheck.getString("nombre");
                Sesion.setIdUsuario(idUsuario);
                Sesion.setNombre(nombre);
                Sesion.setTipo(tipo);

                if ("cliente".equals(tipo)) {
                    String sqlCliente = "SELECT id_cliente FROM tbl_clientes WHERE id_usuario = ?";
                    try (PreparedStatement pstCliente = con.prepareStatement(sqlCliente)) {
                        pstCliente.setInt(1, idUsuario);
                        ResultSet rsCliente = pstCliente.executeQuery();
                        if (rsCliente.next()) {
                            Sesion.setIdCliente(rsCliente.getInt("id_cliente"));
                        }
                    }
                    abrirPantallaCliente();
                } else if ("suplidor".equals(tipo)) {
                    String sqlSuplidor = "SELECT id_suplidor FROM tbl_suplidores WHERE id_usuario = ?";
                    try (PreparedStatement pstSuplidor = con.prepareStatement(sqlSuplidor)) {
                        pstSuplidor.setInt(1, idUsuario);
                        ResultSet rsSuplidor = pstSuplidor.executeQuery();
                        if (rsSuplidor.next()) {
                            Sesion.setIdSuplidor(rsSuplidor.getInt("id_suplidor"));
                        }
                    }
                    abrirPantallaProveedor();
                } else if ("admin".equals(tipo)) {
                    abrirPantallaAdmin();
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
        String fxmlPath = "/PaginaPrincipal(Sesion_Iniciada).fxml";
        URL url = getClass().getResource(fxmlPath);
        if (url == null) url = getClass().getResource("/com/example/premierservices/PaginaPrincipal(Sesion_Iniciada).fxml");
        if (url == null) {
            mostrarMensaje("No se encontró la pantalla principal del cliente", "error");
            return;
        }
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        PaginaPrincipalSesionController controller = loader.getController();
        controller.setDatosCliente(Sesion.getIdCliente(), Sesion.getNombre(), null);
        cambiarEscena(root, "Premier Services - Cliente");
    }

    private void abrirPantallaProveedor() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProveedorDashboard.fxml"));
        Parent root = loader.load();
        ProveedorDashboardController controller = loader.getController();
        controller.setIdSuplidor(Sesion.getIdSuplidor()); // pasar ID del proveedor
        cambiarEscena(root, "Premier Services - Panel Proveedor");
    }

    private void abrirPantallaAdmin() throws IOException {
        String[] rutas = {"/AdminPaginaPrincipal.fxml", "/com/example/premierservices/AdminPaginaPrincipal.fxml"};
        URL url = null;
        for (String ruta : rutas) {
            url = getClass().getResource(ruta);
            if (url != null) break;
        }
        if (url == null) {
            mostrarMensaje("No se encontró la pantalla de administración", "error");
            return;
        }
        Parent root = FXMLLoader.load(url);
        cambiarEscena(root, "Premier Services - Administración");
    }

    private void cambiarEscena(Parent root, String titulo) {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(titulo);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void onRegisterClick() {
        navegarA("/OpcionRegister.fxml", "Elegir tipo de registro");
    }

    @FXML
    private void volverAPaginaPrincipal() {
        navegarA("/PaginaPrincipal(Sin_Sesion).fxml", "Premier Services - Eventos");
    }

    private void navegarA(String fxml, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) AtrasLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(String msg, String tipo) {
        lblMensaje.setText(msg);
        lblMensaje.setVisible(true);
        if ("error".equals(tipo))
            lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        else
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void limpiarMensaje() {
        lblMensaje.setText("");
        lblMensaje.setVisible(false);
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