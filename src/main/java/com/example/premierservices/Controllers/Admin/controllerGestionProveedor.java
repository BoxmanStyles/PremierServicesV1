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
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.util.Optional;

public class controllerGestionProveedor {

    @FXML private TextField txtNombreEmpresa;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCalificacion;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cmbTipoUsuario;
    @FXML private ComboBox<String> cmbUbicacion;
    @FXML private TableView<ObservableList<String>> tablaProveedores;
    @FXML private TableColumn<ObservableList<String>, String> colIdUsuario;
    @FXML private TableColumn<ObservableList<String>, String> colIdSuplidor;
    @FXML private TableColumn<ObservableList<String>, String> colNombreEmpresa;
    @FXML private TableColumn<ObservableList<String>, String> colTelefono;
    @FXML private TableColumn<ObservableList<String>, String> colEmail;
    @FXML private TableColumn<ObservableList<String>, String> colUbicacion;
    @FXML private TableColumn<ObservableList<String>, String> colPlanId;
    @FXML private TableColumn<ObservableList<String>, String> colCalificacion;
    @FXML private ImageView Logoimg;

    private int idSuplidorSeleccionado = 0;
    private int idUsuarioSeleccionado = 0;

    @FXML
    public void initialize() {
        // Cargar logo
        cargarLogo();

        // Configurar ComboBox de planes
        ObservableList<String> planes = FXCollections.observableArrayList("Rookie", "Elite", "Prime");
        cmbTipoUsuario.setItems(planes);

        // Configurar ComboBox de ubicaciones
        ObservableList<String> ciudades = FXCollections.observableArrayList("Santiago", "Santo Domingo", "La Vega", "Puerto Plata", "San Francisco de Macorís");
        cmbUbicacion.setItems(ciudades);

        cargarTabla();

        // Listener para selección de la tabla
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    if (newSelection.size() > 1) {
                        idSuplidorSeleccionado = Integer.parseInt(newSelection.get(1));
                        idUsuarioSeleccionado = Integer.parseInt(newSelection.get(0));
                        cargarDatosProveedor(newSelection);
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Error al obtener el ID del proveedor", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cargarLogo() {
        try {
            File logoFile = new File("IMG/Logo.png");
            if (logoFile.exists()) {
                Logoimg.setImage(new javafx.scene.image.Image(logoFile.toURI().toString()));
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void cargarDatosProveedor(ObservableList<String> proveedor) {
        try {
            if (proveedor == null || proveedor.size() < 8) {
                mostrarAlerta("Error", "Datos del proveedor incompletos", Alert.AlertType.ERROR);
                return;
            }

            txtNombreEmpresa.setText(proveedor.get(2) != null ? proveedor.get(2) : "");
            txtTelefono.setText(proveedor.get(3) != null ? proveedor.get(3) : "");
            txtEmail.setText(proveedor.get(4) != null ? proveedor.get(4) : "");

            String ubicacion = proveedor.get(5);
            if (ubicacion != null && !ubicacion.isEmpty()) {
                cmbUbicacion.setValue(ubicacion);
            }

            String planId = proveedor.get(6);
            if (planId != null) {
                switch (planId) {
                    case "1":
                        cmbTipoUsuario.setValue("Rookie");
                        break;
                    case "2":
                        cmbTipoUsuario.setValue("Elite");
                        break;
                    case "3":
                        cmbTipoUsuario.setValue("Prime");
                        break;
                    default:
                        cmbTipoUsuario.setValue(null);
                }
            }

            String calificacion = proveedor.get(7);
            if (calificacion != null && !calificacion.equals("null")) {
                txtCalificacion.setText(calificacion);
            }

            // Obtener descripción desde la base de datos
            String sql = "SELECT descripcion FROM dbo.tbl_suplidores WHERE id_suplidor = ?";
            try (Connection con = conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idSuplidorSeleccionado);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtDescripcion.setText(rs.getString("descripcion") != null ? rs.getString("descripcion") : "");
                } else {
                    txtDescripcion.clear();
                }
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
        if (idSuplidorSeleccionado != 0) {
            mostrarAlerta("Advertencia", "Para registrar un nuevo proveedor, primero limpie los campos o seleccione 'Limpiar'", Alert.AlertType.WARNING);
            return;
        }

        if (txtNombreEmpresa.getText().isEmpty() || txtTelefono.getText().isEmpty() ||
                cmbUbicacion.getValue() == null || cmbTipoUsuario.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos obligatorios.\nCampos requeridos: Nombre Empresa, Teléfono, Ubicación y Plan.", Alert.AlertType.WARNING);
            return;
        }

        // Primero insertar en tbl_usuarios
        int nuevoIdUsuario;
        String sqlInsertUsuario = "INSERT INTO dbo.tbl_usuarios (nombre, email, contraseña, tipo_usuario, estado, fecha_registro) " +
                "VALUES (?, ?, 'temp123', 'proveedor', 'activo', GETDATE())";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sqlInsertUsuario, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, txtNombreEmpresa.getText());
            ps.setString(2, txtEmail.getText().isEmpty() ? null : txtEmail.getText());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                nuevoIdUsuario = rs.getInt(1);
            } else {
                mostrarAlerta("Error", "No se pudo crear el usuario", Alert.AlertType.ERROR);
                return;
            }

            int planId = switch (cmbTipoUsuario.getValue()) {
                case "Rookie" -> 1;
                case "Elite" -> 2;
                case "Prime" -> 3;
                default -> 1;
            };

            double calificacion = 0.0;
            try {
                if (!txtCalificacion.getText().isEmpty()) {
                    calificacion = Double.parseDouble(txtCalificacion.getText());
                    if (calificacion < 0 || calificacion > 5) {
                        mostrarAlerta("Calificación inválida", "La calificación debe estar entre 0 y 5.", Alert.AlertType.WARNING);
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Calificación inválida", "Ingresa un número válido entre 0 y 5.", Alert.AlertType.WARNING);
                return;
            }

            String sqlInsertSuplidor = "INSERT INTO dbo.tbl_suplidores (id_usuario, nombre_empresa, descripcion, ubicacion, telefono, plan_id, calificacion_promedio) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps2 = con.prepareStatement(sqlInsertSuplidor)) {
                ps2.setInt(1, nuevoIdUsuario);
                ps2.setString(2, txtNombreEmpresa.getText());
                ps2.setString(3, txtDescripcion.getText().isEmpty() ? null : txtDescripcion.getText());
                ps2.setString(4, cmbUbicacion.getValue());
                ps2.setString(5, txtTelefono.getText());
                ps2.setInt(6, planId);
                ps2.setDouble(7, calificacion);
                ps2.executeUpdate();

                mostrarInfo("Éxito", "Proveedor guardado correctamente.");
                limpiarCampos();
                cargarTabla();
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al guardar", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onCancelarClick(ActionEvent event) {
        limpiarCampos();
        idSuplidorSeleccionado = 0;
        idUsuarioSeleccionado = 0;
        tablaProveedores.getSelectionModel().clearSelection();
    }

    @FXML
    void onEditarClick(ActionEvent event) {
        if (idSuplidorSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un proveedor de la tabla para editar", Alert.AlertType.WARNING);
            return;
        }

        if (txtNombreEmpresa.getText().isEmpty() || txtTelefono.getText().isEmpty() ||
                cmbUbicacion.getValue() == null || cmbTipoUsuario.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        int planId = switch (cmbTipoUsuario.getValue()) {
            case "Rookie" -> 1;
            case "Elite" -> 2;
            case "Prime" -> 3;
            default -> 1;
        };

        double calificacion = 0.0;
        try {
            if (!txtCalificacion.getText().isEmpty()) {
                calificacion = Double.parseDouble(txtCalificacion.getText());
                if (calificacion < 0 || calificacion > 5) {
                    mostrarAlerta("Calificación inválida", "La calificación debe estar entre 0 y 5.", Alert.AlertType.WARNING);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Calificación inválida", "Ingresa un número válido entre 0 y 5.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection con = conectar()) {
            // Actualizar tbl_suplidores (sin email)
            String sqlUpdateSuplidor = "UPDATE dbo.tbl_suplidores SET nombre_empresa=?, descripcion=?, ubicacion=?, telefono=?, plan_id=?, calificacion_promedio=? WHERE id_suplidor=?";

            try (PreparedStatement ps = con.prepareStatement(sqlUpdateSuplidor)) {
                ps.setString(1, txtNombreEmpresa.getText());
                ps.setString(2, txtDescripcion.getText().isEmpty() ? null : txtDescripcion.getText());
                ps.setString(3, cmbUbicacion.getValue());
                ps.setString(4, txtTelefono.getText());
                ps.setInt(5, planId);
                ps.setDouble(6, calificacion);
                ps.setInt(7, idSuplidorSeleccionado);
                ps.executeUpdate();
            }

            // Actualizar tbl_usuarios (email)
            if (idUsuarioSeleccionado != 0) {
                String sqlUpdateUsuario = "UPDATE dbo.tbl_usuarios SET nombre=?, email=? WHERE id_usuario=?";

                try (PreparedStatement ps = con.prepareStatement(sqlUpdateUsuario)) {
                    ps.setString(1, txtNombreEmpresa.getText());
                    ps.setString(2, txtEmail.getText().isEmpty() ? null : txtEmail.getText());
                    ps.setInt(3, idUsuarioSeleccionado);
                    ps.executeUpdate();
                }
            }

            mostrarInfo("Éxito", "Proveedor actualizado correctamente.");
            limpiarCampos();
            cargarTabla();
            idSuplidorSeleccionado = 0;
            idUsuarioSeleccionado = 0;

        } catch (SQLException e) {
            mostrarAlerta("Error al actualizar", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
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

    @FXML
    void onEliminarClick(ActionEvent event) {
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
                // Eliminar de tbl_suplidores
                String sqlDeleteSuplidor = "DELETE FROM dbo.tbl_suplidores WHERE id_suplidor = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlDeleteSuplidor)) {
                    ps.setInt(1, idSuplidorSeleccionado);
                    ps.executeUpdate();
                }

                // Eliminar de tbl_usuarios
                String sqlDeleteUsuario = "DELETE FROM dbo.tbl_usuarios WHERE id_usuario = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlDeleteUsuario)) {
                    ps.setInt(1, idUsuarioSeleccionado);
                    ps.executeUpdate();
                }

                mostrarInfo("Éxito", "Proveedor eliminado correctamente");
                limpiarCampos();
                cargarTabla();
                idSuplidorSeleccionado = 0;
                idUsuarioSeleccionado = 0;

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
        txtCalificacion.clear();
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
        colEmail.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colUbicacion.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colPlanId.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));
        colCalificacion.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(7)));

        String sql = "SELECT s.id_usuario, s.id_suplidor, s.nombre_empresa, s.telefono, u.email, s.ubicacion, s.plan_id, s.calificacion_promedio " +
                "FROM dbo.tbl_suplidores s " +
                "INNER JOIN dbo.tbl_usuarios u ON s.id_usuario = u.id_usuario";

        try (Connection con = conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("id_usuario"));
                fila.add(rs.getString("id_suplidor"));
                fila.add(rs.getString("nombre_empresa"));
                fila.add(rs.getString("telefono"));
                fila.add(rs.getString("email") != null ? rs.getString("email") : "");
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


