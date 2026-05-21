package com.example.premierservices.Controllers.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import java.time.LocalDate;
import java.util.Optional;

public class controllerGestionReservas {

    // Filtros
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private ComboBox<String> cmbFiltroProveedor;
    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;

    // Tabla
    @FXML private TableView<ObservableList<String>> tablaReservas;
    @FXML private TableColumn<ObservableList<String>, String> colIdReserva;
    @FXML private TableColumn<ObservableList<String>, String> colCliente;
    @FXML private TableColumn<ObservableList<String>, String> colServicio;
    @FXML private TableColumn<ObservableList<String>, String> colProveedor;
    @FXML private TableColumn<ObservableList<String>, String> colFechaEvento;
    @FXML private TableColumn<ObservableList<String>, String> colMonto;
    @FXML private TableColumn<ObservableList<String>, String> colEstado;

    // Estadísticas
    @FXML private Label lblTotalReservas;
    @FXML private Label lblPendientes;
    @FXML private Label lblConfirmadas;
    @FXML private Label lblTotalIngresos;

    // Botones
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;
    @FXML private Button btnCompletar;
    @FXML private Button btnActualizar;

    @FXML private ImageView Logoimg;

    private ObservableList<ObservableList<String>> todasReservas = FXCollections.observableArrayList();
    private FilteredList<ObservableList<String>> reservasFiltradas;
    private int idReservaSeleccionada = 0;

    @FXML
    public void initialize() {
        cargarLogo();
        configurarFiltros();
        cargarTabla();
        actualizarEstadisticas();

        // Listener para selección de tabla
        tablaReservas.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.size() > 0) {
                try {
                    idReservaSeleccionada = Integer.parseInt(newVal.get(0));
                } catch (NumberFormatException e) {
                    idReservaSeleccionada = 0;
                }
            } else {
                idReservaSeleccionada = 0;
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

    private void configurarFiltros() {
        // Estados
        ObservableList<String> estados = FXCollections.observableArrayList("Todos", "pendiente", "confirmada", "cancelada", "completada");
        cmbFiltroEstado.setItems(estados);
        cmbFiltroEstado.setValue("Todos");

        // Proveedores
        cargarProveedoresFiltro();
    }

    private void cargarProveedoresFiltro() {
        ObservableList<String> proveedores = FXCollections.observableArrayList();
        proveedores.add("Todos");

        String sql = "SELECT nombre_empresa FROM dbo.tbl_suplidores ORDER BY nombre_empresa";
        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                proveedores.add(rs.getString("nombre_empresa"));
            }
        } catch (SQLException e) {
            System.err.println("Error cargando proveedores: " + e.getMessage());
        }
        cmbFiltroProveedor.setItems(proveedores);
        cmbFiltroProveedor.setValue("Todos");
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
    void onFiltrarClick(ActionEvent event) {
        aplicarFiltros();
    }

    @FXML
    void onLimpiarFiltrosClick(ActionEvent event) {
        cmbFiltroEstado.setValue("Todos");
        cmbFiltroProveedor.setValue("Todos");
        dpFechaDesde.setValue(null);
        dpFechaHasta.setValue(null);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String estadoFiltro = cmbFiltroEstado.getValue();
        String proveedorFiltro = cmbFiltroProveedor.getValue();
        LocalDate fechaDesde = dpFechaDesde.getValue();
        LocalDate fechaHasta = dpFechaHasta.getValue();

        reservasFiltradas.setPredicate(fila -> {
            if (fila == null || fila.size() < 7) return false;

            // Filtro por estado
            if (estadoFiltro != null && !estadoFiltro.equals("Todos")) {
                String estado = fila.get(6);
                if (estado == null || !estado.equalsIgnoreCase(estadoFiltro)) return false;
            }

            // Filtro por proveedor
            if (proveedorFiltro != null && !proveedorFiltro.equals("Todos")) {
                String proveedor = fila.get(3);
                if (proveedor == null || !proveedor.equals(proveedorFiltro)) return false;
            }

            // Filtro por fecha
            if (fechaDesde != null || fechaHasta != null) {
                try {
                    String fechaEventoStr = fila.get(4);
                    if (fechaEventoStr != null && !fechaEventoStr.isEmpty()) {
                        LocalDate fechaEvento = LocalDate.parse(fechaEventoStr.split(" ")[0]);
                        if (fechaDesde != null && fechaEvento.isBefore(fechaDesde)) return false;
                        if (fechaHasta != null && fechaEvento.isAfter(fechaHasta)) return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        });

        tablaReservas.setItems(reservasFiltradas);
        actualizarEstadisticasFiltradas();
    }

    private void actualizarEstadisticasFiltradas() {
        int total = reservasFiltradas.size();
        long pendientes = reservasFiltradas.stream().filter(f -> f.get(6) != null && f.get(6).equalsIgnoreCase("pendiente")).count();
        long confirmadas = reservasFiltradas.stream().filter(f -> f.get(6) != null && f.get(6).equalsIgnoreCase("confirmada")).count();
        double ingresos = reservasFiltradas.stream()
                .filter(f -> f.get(6) != null && (f.get(6).equalsIgnoreCase("confirmada") || f.get(6).equalsIgnoreCase("completada")))
                .mapToDouble(f -> {
                    try {
                        return Double.parseDouble(f.get(5));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }).sum();

        lblTotalReservas.setText(String.valueOf(total));
        lblPendientes.setText(String.valueOf(pendientes));
        lblConfirmadas.setText(String.valueOf(confirmadas));
        lblTotalIngresos.setText(String.format("$ %.2f", ingresos));
    }

    @FXML
    void onConfirmarClick(ActionEvent event) {
        if (idReservaSeleccionada == 0) {
            mostrarAlerta("Advertencia", "Seleccione una reserva de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Confirmar esta reserva?");
        confirmacion.setContentText("La reserva será marcada como confirmada.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cambiarEstadoReserva("confirmada");
        }
    }

    @FXML
    void onCancelarClick(ActionEvent event) {
        if (idReservaSeleccionada == 0) {
            mostrarAlerta("Advertencia", "Seleccione una reserva de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar");
        confirmacion.setHeaderText("¿Cancelar esta reserva?");
        confirmacion.setContentText("La reserva será marcada como cancelada.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cambiarEstadoReserva("cancelada");
        }
    }

    @FXML
    void onCompletarClick(ActionEvent event) {
        if (idReservaSeleccionada == 0) {
            mostrarAlerta("Advertencia", "Seleccione una reserva de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Completar");
        confirmacion.setHeaderText("¿Marcar esta reserva como completada?");
        confirmacion.setContentText("La reserva será marcada como completada.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cambiarEstadoReserva("completada");
        }
    }

    @FXML
    void onActualizarClick(ActionEvent event) {
        cargarTabla();
        actualizarEstadisticas();
        mostrarInfo("Actualizado", "Los datos han sido actualizados.");
    }

    private void cambiarEstadoReserva(String nuevoEstado) {
        String sql = "UPDATE dbo.tbl_reservas SET estado = ? WHERE id_reserva = ?";

        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idReservaSeleccionada);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                String mensaje = "";
                switch (nuevoEstado) {
                    case "confirmada":
                        mensaje = "confirmada";
                        break;
                    case "cancelada":
                        mensaje = "cancelada";
                        break;
                    case "completada":
                        mensaje = "completada";
                        break;
                }
                mostrarInfo("Éxito", "Reserva " + mensaje + " correctamente.");
                cargarTabla();
                actualizarEstadisticas();
                idReservaSeleccionada = 0;
                tablaReservas.getSelectionModel().clearSelection();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar la reserva.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void actualizarEstadisticas() {
        int total = todasReservas.size();
        long pendientes = todasReservas.stream().filter(f -> f.get(6) != null && f.get(6).equalsIgnoreCase("pendiente")).count();
        long confirmadas = todasReservas.stream().filter(f -> f.get(6) != null && f.get(6).equalsIgnoreCase("confirmada")).count();
        double ingresos = todasReservas.stream()
                .filter(f -> f.get(6) != null && (f.get(6).equalsIgnoreCase("confirmada") || f.get(6).equalsIgnoreCase("completada")))
                .mapToDouble(f -> {
                    try {
                        return Double.parseDouble(f.get(5));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }).sum();

        lblTotalReservas.setText(String.valueOf(total));
        lblPendientes.setText(String.valueOf(pendientes));
        lblConfirmadas.setText(String.valueOf(confirmadas));
        lblTotalIngresos.setText(String.format("$ %.2f", ingresos));
    }

    private void cargarTabla() {
        todasReservas.clear();

        colIdReserva.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colCliente.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colServicio.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colProveedor.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colFechaEvento.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colMonto.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colEstado.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));

        String sql = "SELECT " +
                "r.id_reserva, " +
                "CONCAT(c.nombre, ' ', c.apellido) AS cliente, " +
                "s.nombre_servicio, " +
                "p.nombre_empresa AS proveedor, " +
                "FORMAT(r.fecha_evento, 'yyyy-MM-dd') AS fecha_evento, " +
                "ISNULL(r.total, 0) AS total, " +
                "r.estado " +
                "FROM dbo.tbl_reservas r " +
                "INNER JOIN dbo.tbl_clientes c ON r.id_cliente = c.id_cliente " +
                "INNER JOIN dbo.tbl_servicios s ON r.id_servicio = s.id_servicio " +
                "INNER JOIN dbo.tbl_suplidores p ON s.id_suplidor = p.id_suplidor " +
                "ORDER BY r.id_reserva DESC";

        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("id_reserva"));
                fila.add(rs.getString("cliente"));
                fila.add(rs.getString("nombre_servicio"));
                fila.add(rs.getString("proveedor"));
                fila.add(rs.getString("fecha_evento"));
                fila.add(rs.getString("total"));
                fila.add(rs.getString("estado"));
                todasReservas.add(fila);
            }

            reservasFiltradas = new FilteredList<>(todasReservas, p -> true);
            tablaReservas.setItems(reservasFiltradas);
            tablaReservas.refresh();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar reservas: " + e.getMessage(), Alert.AlertType.ERROR);
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