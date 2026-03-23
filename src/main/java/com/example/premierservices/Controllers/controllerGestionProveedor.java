package com.example.premierservices.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

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

    @FXML
    public void initialize() {
        ObservableList<String> planes = FXCollections.observableArrayList("Rookie", "Plan Elite", "Plan Prime");
        cmbTipoUsuario.setItems(planes);

        ObservableList<String> ciudades = FXCollections.observableArrayList("Santiago", "Santo Domingo", "La Vega", "Puerto Plata", "San Francisco de Macorís");
        cmbUbicacion.setItems(ciudades);

        cargarTabla();
    }

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
    void onRegistrarClick(ActionEvent event) {

        if (txtNombreEmpresa.getText().isEmpty() ||
                txtTelefono.getText().isEmpty()  ||
                cmbUbicacion.getValue() == null  ||
                cmbTipoUsuario.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos obligatorios.");
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
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Calificación inválida", "Ingresa un número válido entre 1 y 5.");
            return;
        }

        String sql = "INSERT INTO dbo.tbl_suplidores (id_usuario, nombre_empresa, descripcion, ubicacion, telefono, plan_id, calificacion_promedio) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, 1);
            ps.setString(2, txtNombreEmpresa.getText());
            ps.setString(3, txtDescripcion.getText());
            ps.setString(4, cmbUbicacion.getValue());
            ps.setString(5, txtTelefono.getText());
            ps.setInt(6, planId);
            ps.setDouble(7, calificacion);

            ps.executeUpdate();

            mostrarInfo("Éxito", "Proveedor guardado correctamente.");
            limpiarCampos();
            cargarTabla();

        } catch (SQLException e) {
            mostrarAlerta("Error al guardar", e.getMessage());
        }
    }

    @FXML
    void onCancelarClick(ActionEvent event) {
        limpiarCampos();
    }

    @FXML
    void onEditarClick(ActionEvent event) {
        System.out.println("Editar proveedor");
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
        txtNombreEmpresa.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtTelefono1.clear();
        txtDescripcion.clear();
        cmbTipoUsuario.setValue(null);
        cmbUbicacion.setValue(null);
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
            mostrarAlerta("Error al cargar tabla", e.getMessage());
        }
    }
}