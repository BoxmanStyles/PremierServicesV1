package com.example.premierservices.Controllers.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.sql.*;
import java.util.Optional;

public class controllerGestionCliente {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private PasswordField txtContrasena;
    @FXML private PasswordField txtConfirmarContrasena;

    @FXML private TableView<ObservableList<String>> tablaClientes;
    @FXML private TableColumn<ObservableList<String>, String> colIdCliente;
    @FXML private TableColumn<ObservableList<String>, String> colIdUsuario;
    @FXML private TableColumn<ObservableList<String>, String> colNombre;
    @FXML private TableColumn<ObservableList<String>, String> colApellido;
    @FXML private TableColumn<ObservableList<String>, String> colEmail;
    @FXML private TableColumn<ObservableList<String>, String> colTelefono;
    @FXML private TableColumn<ObservableList<String>, String> colDireccion;

    @FXML private ImageView Logoimg;

    private int idClienteSeleccionado = 0;
    private int idUsuarioSeleccionado = 0;

    @FXML
    public void initialize() {
        cargarLogo();
        cargarTabla();

        // Listener para selección de tabla
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                try {
                    idClienteSeleccionado = Integer.parseInt(newVal.get(0));
                    idUsuarioSeleccionado = Integer.parseInt(newVal.get(1));
                    cargarDatosCliente(newVal);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Error al obtener ID del cliente", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cargarLogo() {
        try {
            File logoFile = new File("IMG/Logo.png");
            if (logoFile.exists()) {
                Logoimg.setImage(new Image(logoFile.toURI().toString()));
                System.out.println("✅ Logo cargado desde archivo");
                return;
            }
            java.io.InputStream stream = getClass().getResourceAsStream("/IMG/Logo.png");
            if (stream != null) {
                Logoimg.setImage(new Image(stream));
                System.out.println("✅ Logo cargado desde classpath");
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void cargarDatosCliente(ObservableList<String> cliente) {
        if (cliente.size() >= 7) {
            txtNombre.setText(cliente.get(2));
            txtApellido.setText(cliente.get(3));
            txtEmail.setText(cliente.get(4));
            txtTelefono.setText(cliente.get(5));
            txtDireccion.setText(cliente.get(6));
            txtContrasena.clear();
            txtConfirmarContrasena.clear();
        }
    }

    private Connection conectar() {
        try {
            String url = "jdbc:sqlserver://26.228.126.202:1433;" +
                    "databaseName=PremierServicesV1;" +
                    "encrypt=true;trustServerCertificate=true";
            return DriverManager.getConnection(url, "wilenny", "1234");
        } catch (Exception e) {
            mostrarAlerta("Error de conexión", e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
    }

    @FXML
    void onRegistrarClick(ActionEvent event) {
        if (idClienteSeleccionado != 0) {
            mostrarAlerta("Advertencia", "Para registrar un nuevo cliente, primero limpie los campos o seleccione 'Limpiar'", Alert.AlertType.WARNING);
            return;
        }

        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || txtTelefono.getText().isEmpty() ||
                txtDireccion.getText().isEmpty() || txtContrasena.getText().isEmpty()) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        if (!txtContrasena.getText().equals(txtConfirmarContrasena.getText())) {
            mostrarAlerta("Error", "Las contraseñas no coinciden.", Alert.AlertType.ERROR);
            return;
        }

        String hashedPassword = BCrypt.hashpw(txtContrasena.getText(), BCrypt.gensalt());

        try (Connection con = conectar()) {
            con.setAutoCommit(false);

            // Insertar en tbl_usuarios
            String sqlUsuario = "INSERT INTO tbl_usuarios (nombre, email, contraseña, tipo_usuario, estado, fecha_registro) VALUES (?, ?, ?, 'cliente', 'activo', GETDATE())";
            int idUsuario = -1;
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtEmail.getText());
                ps.setString(3, hashedPassword);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    idUsuario = rs.getInt(1);
                }
            }

            // Insertar en tbl_clientes
            String sqlCliente = "INSERT INTO tbl_clientes (id_usuario, nombre, apellido, email, telefono, direccion, tipo_cliente) VALUES (?, ?, ?, ?, ?, ?, 'personal')";
            try (PreparedStatement ps = con.prepareStatement(sqlCliente)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, txtNombre.getText());
                ps.setString(3, txtApellido.getText());
                ps.setString(4, txtEmail.getText());
                ps.setString(5, txtTelefono.getText());
                ps.setString(6, txtDireccion.getText());
                ps.executeUpdate();
            }

            con.commit();
            mostrarInfo("Éxito", "Cliente registrado correctamente.");
            limpiarCampos();
            cargarTabla();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al registrar: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onEditarClick(ActionEvent event) {
        if (idClienteSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un cliente de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || txtTelefono.getText().isEmpty() ||
                txtDireccion.getText().isEmpty()) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection con = conectar()) {
            con.setAutoCommit(false);

            // Actualizar tbl_usuarios
            String sqlUsuario = "UPDATE tbl_usuarios SET nombre=?, email=? WHERE id_usuario=?";
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtEmail.getText());
                ps.setInt(3, idUsuarioSeleccionado);
                ps.executeUpdate();
            }

            // Actualizar tbl_clientes
            String sqlCliente = "UPDATE tbl_clientes SET nombre=?, apellido=?, email=?, telefono=?, direccion=? WHERE id_cliente=?";
            try (PreparedStatement ps = con.prepareStatement(sqlCliente)) {
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtApellido.getText());
                ps.setString(3, txtEmail.getText());
                ps.setString(4, txtTelefono.getText());
                ps.setString(5, txtDireccion.getText());
                ps.setInt(6, idClienteSeleccionado);
                ps.executeUpdate();
            }

            // Actualizar contraseña si se proporcionó
            if (!txtContrasena.getText().isEmpty()) {
                if (!txtContrasena.getText().equals(txtConfirmarContrasena.getText())) {
                    mostrarAlerta("Error", "Las contraseñas no coinciden.", Alert.AlertType.ERROR);
                    return;
                }
                String hashedPassword = BCrypt.hashpw(txtContrasena.getText(), BCrypt.gensalt());
                String sqlPassword = "UPDATE tbl_usuarios SET contraseña=? WHERE id_usuario=?";
                try (PreparedStatement ps = con.prepareStatement(sqlPassword)) {
                    ps.setString(1, hashedPassword);
                    ps.setInt(2, idUsuarioSeleccionado);
                    ps.executeUpdate();
                }
            }

            con.commit();
            mostrarInfo("Éxito", "Cliente actualizado correctamente.");
            limpiarCampos();
            cargarTabla();
            idClienteSeleccionado = 0;
            idUsuarioSeleccionado = 0;

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void OnEliminarClick(ActionEvent event) {
        if (idClienteSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un cliente de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Eliminar cliente?");
        confirmacion.setContentText("Esta acción no se puede deshacer. ¿Está seguro?");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection con = conectar()) {
                con.setAutoCommit(false);

                // Eliminar de tbl_clientes
                String sqlCliente = "DELETE FROM tbl_clientes WHERE id_cliente=?";
                try (PreparedStatement ps = con.prepareStatement(sqlCliente)) {
                    ps.setInt(1, idClienteSeleccionado);
                    ps.executeUpdate();
                }

                // Eliminar de tbl_usuarios
                String sqlUsuario = "DELETE FROM tbl_usuarios WHERE id_usuario=?";
                try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                    ps.setInt(1, idUsuarioSeleccionado);
                    ps.executeUpdate();
                }

                con.commit();
                mostrarInfo("Éxito", "Cliente eliminado correctamente.");
                limpiarCampos();
                cargarTabla();
                idClienteSeleccionado = 0;
                idUsuarioSeleccionado = 0;

            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onCancelarClick(ActionEvent event) {
        limpiarCampos();
        idClienteSeleccionado = 0;
        idUsuarioSeleccionado = 0;
        tablaClientes.getSelectionModel().clearSelection();
    }

    @FXML
    void onVolverClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminPanel.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel Admin - Premier Services");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar al panel: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        txtContrasena.clear();
        txtConfirmarContrasena.clear();
    }

    private void cargarTabla() {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();

        colIdCliente.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colIdUsuario.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colApellido.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colEmail.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colTelefono.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colDireccion.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));

        String sql = "SELECT c.id_cliente, c.id_usuario, c.nombre, c.apellido, c.email, c.telefono, c.direccion " +
                "FROM tbl_clientes c ORDER BY c.id_cliente DESC";

        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("id_cliente"));
                fila.add(rs.getString("id_usuario"));
                fila.add(rs.getString("nombre"));
                fila.add(rs.getString("apellido"));
                fila.add(rs.getString("email"));
                fila.add(rs.getString("telefono"));
                fila.add(rs.getString("direccion"));
                datos.add(fila);
            }
            tablaClientes.setItems(datos);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar clientes: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
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