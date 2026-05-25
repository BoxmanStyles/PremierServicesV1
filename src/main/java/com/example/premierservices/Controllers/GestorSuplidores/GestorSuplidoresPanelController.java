package com.example.premierservices.Controllers.GestorSuplidores;

import com.example.premierservices.Models.Sesion;
import javafx.beans.property.SimpleStringProperty;
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

public class GestorSuplidoresPanelController {

    @FXML private TextField txtNombreEmpresa;

    @FXML private ComboBox<String> cmbPlan;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ImageView Logoimg;

    @FXML private TableView<ObservableList<String>> tablaSuplidores;
    @FXML private TableColumn<ObservableList<String>, String> colIdSuplidor;
    @FXML private TableColumn<ObservableList<String>, String> colNombreEmpresa;
    @FXML private TableColumn<ObservableList<String>, String> colEmail;
    @FXML private TableColumn<ObservableList<String>, String> colTelefono;
    @FXML private TableColumn<ObservableList<String>, String> colUbicacion;
    @FXML private TableColumn<ObservableList<String>, String> colPlan;
    @FXML private TableColumn<ObservableList<String>, String> colCalificacion;
    @FXML private TableColumn<ObservableList<String>, String> colEstado;

    private int idSuplidorSeleccionado = 0;
    private int idUsuarioSeleccionado = 0;
    private ObservableList<ObservableList<String>> todosLosDatos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cargarLogo();
        cmbPlan.setItems(FXCollections.observableArrayList("Rookie", "Elite", "Prime"));
        cmbEstado.setItems(FXCollections.observableArrayList("activo", "inactivo"));

        configurarColumnas();
        cargarTabla(null, null);

        // Selección en tabla → rellena el formulario de acción
        tablaSuplidores.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) seleccionarSuplidor(newVal);
        });

        // Búsqueda: el TextField fue removido del FXML; los filtros de la barra superior están activos
    }

    // ─── Selección de fila ────────────────────────────────────────────────────

    private void seleccionarSuplidor(ObservableList<String> fila) {
        try {
            idSuplidorSeleccionado = Integer.parseInt(fila.get(0));
            idUsuarioSeleccionado  = Integer.parseInt(fila.get(8)); // índice 8 = id_usuario
            txtNombreEmpresa.setText(fila.get(1));

            // Plan raw viene como "1","2","3"
            switch (fila.get(5)) {
                case "1" -> cmbPlan.setValue("Rookie");
                case "2" -> cmbPlan.setValue("Elite");
                case "3" -> cmbPlan.setValue("Prime");
                default  -> cmbPlan.setValue(null);
            }
            cmbEstado.setValue(fila.get(7));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Filtros de la barra superior ─────────────────────────────────────────

    @FXML void filtrarTodos(ActionEvent e)       { cargarTabla(null, null); }
    @FXML void filtrarRookie(ActionEvent e)      { cargarTabla("plan", "1"); }
    @FXML void filtrarElite(ActionEvent e)       { cargarTabla("plan", "2"); }
    @FXML void filtrarPrime(ActionEvent e)       { cargarTabla("plan", "3"); }
    @FXML void filtrarSuspendidos(ActionEvent e) { cargarTabla("estado", "inactivo"); }
    @FXML void buscarProveedor(ActionEvent e)    { cargarTabla(null, null); }

    private void filtrarPorTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            tablaSuplidores.setItems(todosLosDatos);
            return;
        }
        String lower = texto.toLowerCase();
        ObservableList<ObservableList<String>> resultado = FXCollections.observableArrayList();
        for (ObservableList<String> fila : todosLosDatos) {
            boolean coincide = fila.stream()
                    .limit(8) // solo columnas visibles
                    .anyMatch(v -> v != null && v.toLowerCase().contains(lower));
            if (coincide) resultado.add(fila);
        }
        tablaSuplidores.setItems(resultado);
    }

    // ─── Acción: Guardar Cambios ───────────────────────────────────────────────

    @FXML
    void onGuardarCambios(ActionEvent event) {
        if (idSuplidorSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Selecciona un suplidor de la tabla primero.", Alert.AlertType.WARNING);
            return;
        }
        if (cmbPlan.getValue() == null || cmbEstado.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Debes seleccionar un plan y un estado.", Alert.AlertType.WARNING);
            return;
        }

        int planId = switch (cmbPlan.getValue()) {
            case "Elite" -> 2;
            case "Prime" -> 3;
            default      -> 1; // Rookie
        };
        String nuevoEstado = cmbEstado.getValue();

        try (Connection con = conectar()) {
            // 1. Actualizar plan en tbl_suplidores
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE dbo.tbl_suplidores SET plan_id = ? WHERE id_suplidor = ?")) {
                ps.setInt(1, planId);
                ps.setInt(2, idSuplidorSeleccionado);
                ps.executeUpdate();
            }
            // 2. Actualizar estado en tbl_usuarios
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE dbo.tbl_usuarios SET estado = ? WHERE id_usuario = ?")) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idUsuarioSeleccionado);
                ps.executeUpdate();
            }

            mostrarInfo("Éxito", "Cambios guardados correctamente.");
            onLimpiarSeleccion(event);
            cargarTabla(null, null);

        } catch (SQLException e) {
            mostrarAlerta("Error al guardar", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onLimpiarSeleccion(ActionEvent event) {
        idSuplidorSeleccionado = 0;
        idUsuarioSeleccionado  = 0;
        txtNombreEmpresa.clear();
        cmbPlan.setValue(null);
        cmbEstado.setValue(null);
        tablaSuplidores.getSelectionModel().clearSelection();
    }

    // ─── Cerrar sesión ────────────────────────────────────────────────────────

    @FXML
    void cerrarSesion(ActionEvent event) {
        try {
            Sesion.limpiar();
            Parent root = FXMLLoader.load(getClass().getResource("/LoginGeneralV2.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Premier Services - Login");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cerrar sesión: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ─── Carga de tabla ───────────────────────────────────────────────────────

    private void cargarTabla(String tipofiltro, String valorFiltro) {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(
                "SELECT s.id_suplidor, s.nombre_empresa, u.email, s.telefono, s.ubicacion, " +
                "s.plan_id, s.calificacion_promedio, u.estado, s.id_usuario " +
                "FROM dbo.tbl_suplidores s " +
                "INNER JOIN dbo.tbl_usuarios u ON s.id_usuario = u.id_usuario");

        if (tipofiltro != null) {
            if ("plan".equals(tipofiltro)) {
                sql.append(" WHERE s.plan_id = ").append(valorFiltro);
            } else if ("estado".equals(tipofiltro)) {
                sql.append(" WHERE u.estado = '").append(valorFiltro).append("'");
            }
        }

        try (Connection con = conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql.toString())) {

            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("id_suplidor"));
                fila.add(rs.getString("nombre_empresa"));
                fila.add(rs.getString("email")               != null ? rs.getString("email")               : "");
                fila.add(rs.getString("telefono")            != null ? rs.getString("telefono")            : "");
                fila.add(rs.getString("ubicacion")           != null ? rs.getString("ubicacion")           : "");
                fila.add(rs.getString("plan_id"));
                fila.add(rs.getString("calificacion_promedio") != null ? rs.getString("calificacion_promedio") : "0.0");
                fila.add(rs.getString("estado"));
                fila.add(rs.getString("id_usuario")); // índice 8 (oculto, usado en updates)
                datos.add(fila);
            }

            todosLosDatos = datos;
            tablaSuplidores.setItems(datos);

        } catch (SQLException e) {
            mostrarAlerta("Error al cargar tabla", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void configurarColumnas() {
        colIdSuplidor.setCellValueFactory(p   -> new SimpleStringProperty(p.getValue().get(0)));
        colNombreEmpresa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(1)));
        colEmail.setCellValueFactory(p         -> new SimpleStringProperty(p.getValue().get(2)));
        colTelefono.setCellValueFactory(p      -> new SimpleStringProperty(p.getValue().get(3)));
        colUbicacion.setCellValueFactory(p     -> new SimpleStringProperty(p.getValue().get(4)));

        // Mostrar nombre del plan en vez del ID numérico
        colPlan.setCellValueFactory(p -> {
            String planName = switch (p.getValue().get(5)) {
                case "1" -> "🟢 Rookie";
                case "2" -> "🔵 Elite";
                case "3" -> "👑 Prime";
                default  -> p.getValue().get(5);
            };
            return new SimpleStringProperty(planName);
        });

        colCalificacion.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(6)));

        // Mostrar estado con color visual (ícono)
        colEstado.setCellValueFactory(p -> {
            String estado = p.getValue().get(7);
            return new SimpleStringProperty("activo".equals(estado) ? "✅ activo" : "🔴 inactivo");
        });
    }

    // ─── Logo y conexión ──────────────────────────────────────────────────────

    private void cargarLogo() {
        try {
            File f = new File("IMG/Logo.png");
            if (f.exists()) Logoimg.setImage(new javafx.scene.image.Image(f.toURI().toString()));
        } catch (Exception e) {
            System.err.println("Error cargando logo: " + e.getMessage());
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
