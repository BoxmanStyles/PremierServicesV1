package com.example.premierservices.Controllers.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private int idClienteSeleccionado = 0;

    @FXML
    public void initialize() {
        cargarTabla();

        // Listener para selección de la tabla
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    if (newSelection.size() > 0) {
                        idClienteSeleccionado = Integer.parseInt(newSelection.get(0));
                        cargarDatosCliente(newSelection);
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Error al obtener el ID del cliente", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cargarDatosCliente(ObservableList<String> cliente) {
        try {
            if (cliente == null || cliente.size() < 7) {
                mostrarAlerta("Error", "Datos del cliente incompletos", Alert.AlertType.ERROR);
                return;
            }

            txtNombre.setText(cliente.get(2) != null ? cliente.get(2) : "");
            txtApellido.setText(cliente.get(3) != null ? cliente.get(3) : "");
            txtEmail.setText(cliente.get(4) != null ? cliente.get(4) : "");
            txtTelefono.setText(cliente.get(5) != null ? cliente.get(5) : "");
            txtDireccion.setText(cliente.get(6) != null ? cliente.get(6) : "");

            // Limpiar campos de contraseña por seguridad
            txtContrasena.clear();
            txtConfirmarContrasena.clear();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar datos del cliente: " + e.getMessage(), Alert.AlertType.ERROR);
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

    private int obtenerCantidadResenas(int idCliente) {
        int cantidad = 0;
        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM dbo.tbl_reseñas WHERE id_cliente = ?")) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cantidad = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar reseñas: " + e.getMessage());
        }
        return cantidad;
    }

    @FXML
    void onRegistrarClick(ActionEvent event) {
        if (idClienteSeleccionado != 0) {
            mostrarAlerta("Advertencia", "Para registrar un nuevo cliente, primero limpie los campos o seleccione 'Limpiar'", Alert.AlertType.WARNING);
            return;
        }

        // Validaciones - Todos los campos obligatorios según la estructura de la tabla
        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || txtContrasena.getText().isEmpty()) {
            mostrarAlerta("Campos incompletos", "Por favor completa los campos obligatorios: Nombre, Apellido, Email y Contraseña.", Alert.AlertType.WARNING);
            return;
        }

        // Validar contraseñas
        if (!txtContrasena.getText().equals(txtConfirmarContrasena.getText())) {
            mostrarAlerta("Error de contraseña", "Las contraseñas no coinciden.", Alert.AlertType.WARNING);
            return;
        }

        // INSERT incluyendo tipo_cliente con valor por defecto 'empresa'
        String sql = "INSERT INTO dbo.tbl_clientes (id_usuario, nombre, apellido, email, telefono, direccion, contrasena, tipo_cliente) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, 1); // id_usuario por defecto
            ps.setString(2, txtNombre.getText());
            ps.setString(3, txtApellido.getText());
            ps.setString(4, txtEmail.getText());

            // Teléfono puede ser NULL según la estructura
            if (txtTelefono.getText().isEmpty()) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, txtTelefono.getText());
            }

            // Dirección puede ser NULL según la estructura
            if (txtDireccion.getText().isEmpty()) {
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(6, txtDireccion.getText());
            }

            ps.setString(7, txtContrasena.getText());
            ps.setString(8, "empresa"); // Valor por defecto para tipo_cliente

            ps.executeUpdate();

            mostrarInfo("Éxito", "Cliente guardado correctamente.");
            limpiarCampos();
            cargarTabla();

        } catch (SQLException e) {
            mostrarAlerta("Error al guardar", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onEditarClick(ActionEvent event) {
        if (idClienteSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un cliente de la tabla para editar", Alert.AlertType.WARNING);
            return;
        }

        // Validaciones
        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() ||
                txtEmail.getText().isEmpty()) {
            mostrarAlerta("Campos incompletos", "Por favor completa los campos obligatorios: Nombre, Apellido y Email.", Alert.AlertType.WARNING);
            return;
        }

        StringBuilder sql = new StringBuilder("UPDATE dbo.tbl_clientes SET nombre=?, apellido=?, email=?, telefono=?, direccion=?");

        // Si se proporcionó nueva contraseña, actualizarla
        if (!txtContrasena.getText().isEmpty()) {
            if (!txtContrasena.getText().equals(txtConfirmarContrasena.getText())) {
                mostrarAlerta("Error de contraseña", "Las contraseñas no coinciden.", Alert.AlertType.WARNING);
                return;
            }
            sql.append(", contrasena=?");
        }

        sql.append(" WHERE id_cliente=?");

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            ps.setString(paramIndex++, txtNombre.getText());
            ps.setString(paramIndex++, txtApellido.getText());
            ps.setString(paramIndex++, txtEmail.getText());

            // Teléfono puede ser NULL
            if (txtTelefono.getText().isEmpty()) {
                ps.setNull(paramIndex++, Types.VARCHAR);
            } else {
                ps.setString(paramIndex++, txtTelefono.getText());
            }

            // Dirección puede ser NULL
            if (txtDireccion.getText().isEmpty()) {
                ps.setNull(paramIndex++, Types.VARCHAR);
            } else {
                ps.setString(paramIndex++, txtDireccion.getText());
            }

            if (!txtContrasena.getText().isEmpty()) {
                ps.setString(paramIndex++, txtContrasena.getText());
            }

            ps.setInt(paramIndex, idClienteSeleccionado);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarInfo("Éxito", "Cliente actualizado correctamente.");
                limpiarCampos();
                cargarTabla();
                idClienteSeleccionado = 0;
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el cliente", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al actualizar", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void OnEliminarClick(ActionEvent actionEvent) {
        if (idClienteSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un cliente de la tabla", Alert.AlertType.WARNING);
            return;
        }

        // Verificar cuántas reseñas tiene el cliente
        int cantidadResenas = obtenerCantidadResenas(idClienteSeleccionado);

        String mensaje = "Esta acción no se puede deshacer. ";
        if (cantidadResenas > 0) {
            mensaje += "El cliente tiene " + cantidadResenas + " reseña(s) asociada(s) que también se eliminarán. ";
        }
        mensaje += "¿Está seguro de eliminar este cliente?";

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Eliminar cliente?");
        confirmacion.setContentText(mensaje);

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = conectar()) {

                // Primero eliminar las reseñas asociadas (si las hay)
                if (cantidadResenas > 0) {
                    String sqlDeleteResenas = "DELETE FROM dbo.tbl_reseñas WHERE id_cliente = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlDeleteResenas)) {
                        ps.setInt(1, idClienteSeleccionado);
                        int resenasEliminadas = ps.executeUpdate();
                        System.out.println("Reseñas eliminadas: " + resenasEliminadas);
                    }
                }

                // Luego eliminar el cliente
                String sqlDeleteCliente = "DELETE FROM dbo.tbl_clientes WHERE id_cliente = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlDeleteCliente)) {
                    ps.setInt(1, idClienteSeleccionado);
                    int filasAfectadas = ps.executeUpdate();

                    if (filasAfectadas > 0) {
                        mostrarInfo("Éxito", "Cliente eliminado correctamente" +
                                (cantidadResenas > 0 ? " Se eliminaron " + cantidadResenas + " reseña(s)." : ""));
                        limpiarCampos();
                        cargarTabla();
                        idClienteSeleccionado = 0;
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el cliente", Alert.AlertType.ERROR);
                    }
                }

            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al eliminar cliente: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onCancelarClick(ActionEvent event) {
        limpiarCampos();
        idClienteSeleccionado = 0;
        tablaClientes.getSelectionModel().clearSelection();
    }

    @FXML
    void onVolverClick(ActionEvent event) {
        System.out.println("Volver a lista");
    }

    @FXML
    void onCerrarSesionClick(ActionEvent event) {
        System.out.println("Cerrar sesión");
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

    private void cargarTabla() {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();

        colIdCliente.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colIdUsuario.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colApellido.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colEmail.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colTelefono.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colDireccion.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));

        try (Connection con = conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_cliente, id_usuario, nombre, apellido, email, telefono, direccion FROM dbo.tbl_clientes")) {

            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("id_cliente"));
                fila.add(rs.getString("id_usuario"));
                fila.add(rs.getString("nombre"));
                fila.add(rs.getString("apellido"));
                fila.add(rs.getString("email"));
                fila.add(rs.getString("telefono") != null ? rs.getString("telefono") : "");
                fila.add(rs.getString("direccion") != null ? rs.getString("direccion") : "");
                datos.add(fila);
            }

            tablaClientes.setItems(datos);

        } catch (SQLException e) {
            mostrarAlerta("Error al cargar tabla", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}