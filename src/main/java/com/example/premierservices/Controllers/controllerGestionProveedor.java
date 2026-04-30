package com.example.premierservices.Controllers;

import BaseDeDatos.Conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.Optional;

public class controllerGestionProveedor {

    @FXML private TextField txtNombreEmpresa;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtTelefono1;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cmbTipoUsuario;
    @FXML private ComboBox<String> cmbUbicacion;
    @FXML private TableView<ObservableList<String>> tablaProveedores;
    @FXML private TableColumn<ObservableList<String>, String> colIdUsuario;
    @FXML private TableColumn<ObservableList<String>, String> colIdSuplidor;
    @FXML private TableColumn<ObservableList<String>, String> colNombreEmpresa;
    @FXML private TableColumn<ObservableList<String>, String> colTelefono;
    @FXML private TableColumn<ObservableList<String>, String> colUbicacion;
    @FXML private TableColumn<ObservableList<String>, String> colPlanId;
    @FXML private TableColumn<ObservableList<String>, String> colCalificacion;
    @FXML private Button editarButton;

    private int idSuplidorSeleccionado = 0;

    @FXML
    public void initialize() {
        ObservableList<String> planes = FXCollections.observableArrayList("Rookie", "Plan Elite", "Plan Prime");
        cmbTipoUsuario.setItems(planes);

        ObservableList<String> ciudades = FXCollections.observableArrayList("Santiago", "Santo Domingo", "La Vega", "Puerto Plata", "San Francisco de Macorís");
        cmbUbicacion.setItems(ciudades);

        cargarTabla();

        // Agregar listener para selección de la tabla
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    // Verificar que la fila tenga al menos 2 elementos (índice 1 para id_suplidor)
                    if (newSelection.size() > 1) {
                        idSuplidorSeleccionado = Integer.parseInt(newSelection.get(1));
                        cargarDatosProveedor(newSelection);
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Error al obtener el ID del proveedor", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cargarDatosProveedor(ObservableList<String> proveedor) {
        try {
            // Verificar que los datos existen antes de acceder a ellos
            if (proveedor == null || proveedor.size() < 7) {
                mostrarAlerta("Error", "Datos del proveedor incompletos", Alert.AlertType.ERROR);
                return;
            }

            // Cargar datos básicos desde la tabla
            txtNombreEmpresa.setText(proveedor.get(2) != null ? proveedor.get(2) : "");
            txtTelefono.setText(proveedor.get(3) != null ? proveedor.get(3) : "");

            // Cargar ubicación
            String ubicacion = proveedor.get(4);
            if (ubicacion != null && !ubicacion.isEmpty()) {
                cmbUbicacion.setValue(ubicacion);
            }

            // Cargar plan
            String planId = proveedor.get(5);
            if (planId != null) {
                switch (planId) {
                    case "1":
                        cmbTipoUsuario.setValue("Rookie");
                        break;
                    case "2":
                        cmbTipoUsuario.setValue("Plan Elite");
                        break;
                    case "3":
                        cmbTipoUsuario.setValue("Plan Prime");
                        break;
                    default:
                        cmbTipoUsuario.setValue(null);
                }
            }

            // Cargar calificación
            String calificacion = proveedor.get(6);
            if (calificacion != null) {
                txtTelefono1.setText(calificacion);
            }

            // Obtener email y descripción desde la base de datos
            String sql = "SELECT email, descripcion FROM dbo.tbl_suplidores WHERE id_suplidor = ?";
            try (Connection con = conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idSuplidorSeleccionado);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtEmail.setText(rs.getString("email") != null ? rs.getString("email") : "");
                    txtDescripcion.setText(rs.getString("descripcion") != null ? rs.getString("descripcion") : "");
                } else {
                    txtEmail.clear();
                    txtDescripcion.clear();
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error al cargar email/descripción: " + e.getMessage());
                // No mostrar alerta para no molestar al usuario
                txtEmail.clear();
                txtDescripcion.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar datos del proveedor: " + e.getMessage(), Alert.AlertType.ERROR);
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
        // Verificar que no haya un proveedor seleccionado
        if (idSuplidorSeleccionado != 0) {
            mostrarAlerta("Advertencia", "Para registrar un nuevo proveedor, primero limpie los campos o seleccione 'Limpiar'", Alert.AlertType.WARNING);
            return;
        }

        if (txtNombreEmpresa.getText().isEmpty() ||
                txtTelefono.getText().isEmpty()  ||
                cmbUbicacion.getValue() == null  ||
                cmbTipoUsuario.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        int planId = switch (cmbTipoUsuario.getValue()) {
            case "Rookie"     -> 1;
            case "Plan Elite" -> 2;
            case "Plan Prime" -> 3;
            default           -> 1;
        };

        double calificacion = 0.0;
        try {
            if (!txtTelefono1.getText().isEmpty()) {
                calificacion = Double.parseDouble(txtTelefono1.getText());
                if (calificacion < 0 || calificacion > 5) {
                    mostrarAlerta("Calificación inválida", "La calificación debe estar entre 0 y 5.", Alert.AlertType.WARNING);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Calificación inválida", "Ingresa un número válido entre 0 y 5.", Alert.AlertType.WARNING);
            return;
        }

        String sql = "INSERT INTO dbo.tbl_suplidores (id_usuario, nombre_empresa, email, descripcion, ubicacion, telefono, plan_id, calificacion_promedio) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, 1);
            ps.setString(2, txtNombreEmpresa.getText());
            ps.setString(3, txtEmail.getText().isEmpty() ? null : txtEmail.getText());
            ps.setString(4, txtDescripcion.getText().isEmpty() ? null : txtDescripcion.getText());
            ps.setString(5, cmbUbicacion.getValue());
            ps.setString(6, txtTelefono.getText());
            ps.setInt(7, planId);
            ps.setDouble(8, calificacion);

            ps.executeUpdate();

            mostrarInfo("Éxito", "Proveedor guardado correctamente.");
            limpiarCampos();
            cargarTabla();

        } catch (SQLException e) {
            mostrarAlerta("Error al guardar", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onCancelarClick(ActionEvent event) {
        limpiarCampos();
        idSuplidorSeleccionado = 0;
        tablaProveedores.getSelectionModel().clearSelection();
    }

    @FXML
    void onEditarClick(ActionEvent event) {
        if (idSuplidorSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un proveedor de la tabla para editar", Alert.AlertType.WARNING);
            return;
        }

        if (txtNombreEmpresa.getText().isEmpty() ||
                txtTelefono.getText().isEmpty()  ||
                cmbUbicacion.getValue() == null  ||
                cmbTipoUsuario.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        int planId = switch (cmbTipoUsuario.getValue()) {
            case "Rookie"     -> 1;
            case "Plan Elite" -> 2;
            case "Plan Prime" -> 3;
            default           -> 1;
        };

        double calificacion = 0.0;
        try {
            if (!txtTelefono1.getText().isEmpty()) {
                calificacion = Double.parseDouble(txtTelefono1.getText());
                if (calificacion < 0 || calificacion > 5) {
                    mostrarAlerta("Calificación inválida", "La calificación debe estar entre 0 y 5.", Alert.AlertType.WARNING);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Calificación inválida", "Ingresa un número válido entre 0 y 5.", Alert.AlertType.WARNING);
            return;
        }

        String sql = "UPDATE dbo.tbl_suplidores SET nombre_empresa=?, email=?, descripcion=?, ubicacion=?, telefono=?, plan_id=?, calificacion_promedio=? WHERE id_suplidor=?";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtNombreEmpresa.getText());
            ps.setString(2, txtEmail.getText().isEmpty() ? null : txtEmail.getText());
            ps.setString(3, txtDescripcion.getText().isEmpty() ? null : txtDescripcion.getText());
            ps.setString(4, cmbUbicacion.getValue());
            ps.setString(5, txtTelefono.getText());
            ps.setInt(6, planId);
            ps.setDouble(7, calificacion);
            ps.setInt(8, idSuplidorSeleccionado);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarInfo("Éxito", "Proveedor actualizado correctamente.");
                limpiarCampos();
                cargarTabla();
                idSuplidorSeleccionado = 0;
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el proveedor", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al actualizar", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onVolverClick(ActionEvent event) {
        System.out.println("Volver a lista");
    }

    @FXML
    void onCerrarSesionClick(ActionEvent event) {
        System.out.println("Cerrar sesión");
    }

    @FXML
    void OnEliminarClick(ActionEvent actionEvent) {
        if (idSuplidorSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un proveedor de la tabla", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Eliminar proveedor?");
        confirmacion.setContentText("Esta acción no se puede deshacer. ¿Está seguro de eliminar este proveedor?");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = conectar()) {
                String sqlDelete = "DELETE FROM dbo.tbl_suplidores WHERE id_suplidor = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                    ps.setInt(1, idSuplidorSeleccionado);
                    int filasAfectadas = ps.executeUpdate();

                    if (filasAfectadas > 0) {
                        mostrarInfo("Éxito", "Proveedor eliminado correctamente");
                        limpiarCampos();
                        cargarTabla();
                        idSuplidorSeleccionado = 0;
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el proveedor", Alert.AlertType.ERROR);
                    }
                }

            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al eliminar proveedor: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void limpiarCampos() {
        txtNombreEmpresa.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtTelefono1.clear();
        txtDescripcion.clear();
        cmbTipoUsuario.setValue(null);
        cmbUbicacion.setValue(null);
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

        colIdUsuario.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colIdSuplidor.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colNombreEmpresa.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colTelefono.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colUbicacion.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colPlanId.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colCalificacion.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));

        try (Connection con = conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_usuario, id_suplidor, nombre_empresa, telefono, ubicacion, plan_id, calificacion_promedio FROM dbo.tbl_suplidores")) {

            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("id_usuario"));
                fila.add(rs.getString("id_suplidor"));
                fila.add(rs.getString("nombre_empresa"));
                fila.add(rs.getString("telefono"));
                fila.add(rs.getString("ubicacion"));
                fila.add(rs.getString("plan_id"));
                fila.add(rs.getString("calificacion_promedio"));
                datos.add(fila);
            }

            tablaProveedores.setItems(datos);

        } catch (SQLException e) {
            mostrarAlerta("Error al cargar tabla", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}