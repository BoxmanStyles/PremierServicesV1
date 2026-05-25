package com.example.premierservices.Controllers.GestorFacturacion;

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

import java.sql.*;
import java.time.LocalDate;

public class GestorFacturacionPanelController {

    // ─── Campos del formulario ────────────────────────────────────────────────
    @FXML private TextField   txtNumeroFactura;
    @FXML private TextField   txtItbis;
    @FXML private TextField   txtIdSuplidor;
    @FXML private TextField   txtDescuento;
    @FXML private TextField   txtSubtotal;
    @FXML private TextField   txtTotal;
    @FXML private TextField   txtDescripcionFactura;
    @FXML private DatePicker  dpFechaFactura;
    @FXML private ChoiceBox<String> cbEstadoFactura;
    @FXML private ImageView   Logoimg;

    // ─── Tabla de facturas ────────────────────────────────────────────────────
    @FXML private TableView<ObservableList<String>> tablaFacturas;
    @FXML private TableColumn<ObservableList<String>, String> colNumeroFactura;
    @FXML private TableColumn<ObservableList<String>, String> colComprobante;
    @FXML private TableColumn<ObservableList<String>, String> colFechaFactura;
    @FXML private TableColumn<ObservableList<String>, String> colIdSuplidorFactura;
    @FXML private TableColumn<ObservableList<String>, String> colDescripcionFactura;
    @FXML private TableColumn<ObservableList<String>, String> colSubtotal;
    @FXML private TableColumn<ObservableList<String>, String> colItbis;
    @FXML private TableColumn<ObservableList<String>, String> colDescuento;
    @FXML private TableColumn<ObservableList<String>, String> colTotal;
    @FXML private TableColumn<ObservableList<String>, String> colEstadoFactura;

    private int idFacturaSeleccionada = 0;
    // Filtro activo (null = todas)
    private String filtroEstadoActual = null;

    @FXML
    public void initialize() {
        cbEstadoFactura.setItems(FXCollections.observableArrayList("Pendiente", "pagada", "anulada"));
        cargarLogo();
        configurarColumnas();
        cargarFacturas(null);

        tablaFacturas.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) seleccionarFactura(newVal);
        });
    }

    // ─── Filtros de la barra superior ─────────────────────────────────────────

    @FXML void filtrarTodas(ActionEvent e)      { filtroEstadoActual = null;        cargarFacturas(null); }
    @FXML void filtrarPendientes(ActionEvent e) { filtroEstadoActual = "Pendiente"; cargarFacturas("Pendiente"); }
    @FXML void filtrarPagadas(ActionEvent e)    { filtroEstadoActual = "pagada";    cargarFacturas("pagada"); }
    @FXML void filtrarAnuladas(ActionEvent e)   { filtroEstadoActual = "anulada";   cargarFacturas("anulada"); }

    @FXML
    void buscarFactura(ActionEvent e) {
        // Botón de búsqueda visual — refrescar con filtro activo
        cargarFacturas(filtroEstadoActual);
    }

    // ─── Selección de fila ────────────────────────────────────────────────────

    private void seleccionarFactura(ObservableList<String> factura) {
        if (factura.size() < 10) return;
        try {
            idFacturaSeleccionada = Integer.parseInt(factura.get(0));
            txtNumeroFactura.setText(factura.get(1));
            txtIdSuplidor.setText(factura.get(3));
            txtDescripcionFactura.setText(factura.get(4));
            txtSubtotal.setText(factura.get(5));
            txtItbis.setText(factura.get(6));
            txtDescuento.setText(factura.get(7));
            txtTotal.setText(factura.get(8));
            if (factura.get(2) != null && !factura.get(2).isEmpty()) {
                dpFechaFactura.setValue(LocalDate.parse(factura.get(2).split(" ")[0]));
            }
            cbEstadoFactura.setValue(factura.get(9));
        } catch (Exception e) {
            System.err.println("Error al seleccionar factura: " + e.getMessage());
        }
    }

    // ─── Crear factura ────────────────────────────────────────────────────────

    @FXML
    void onRegistrarFacturaClick(ActionEvent event) {
        if (txtIdSuplidor.getText().isEmpty() || txtSubtotal.getText().isEmpty() ||
                txtTotal.getText().isEmpty() || dpFechaFactura.getValue() == null ||
                cbEstadoFactura.getValue() == null) {
            mostrarAlerta("Campos incompletos",
                    "Campos requeridos: Suplidor, Sub-Total, Total, Fecha y Estado.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            int    idSuplidor  = Integer.parseInt(txtIdSuplidor.getText().trim());
            double subtotal    = Double.parseDouble(txtSubtotal.getText().trim());
            double total       = Double.parseDouble(txtTotal.getText().trim());
            double itbis       = txtItbis.getText().trim().isEmpty()    ? 0 : Double.parseDouble(txtItbis.getText().trim());
            double descuento   = txtDescuento.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtDescuento.getText().trim());
            String descripcion = txtDescripcionFactura.getText().trim();
            String estado      = cbEstadoFactura.getValue();

            // Obtener siguiente número de factura
            int numeroFactura;
            try (Connection con = conectar();
                 Statement st   = con.createStatement();
                 ResultSet rs   = st.executeQuery("SELECT ISNULL(MAX(numero_factura), 0) + 1 AS next_num FROM dbo.tbl_factura")) {
                numeroFactura = rs.next() ? rs.getInt("next_num") : 1;
            } catch (SQLException ex) {
                numeroFactura = (int)(System.currentTimeMillis() % 10000);
            }

            String comprobante = "FACT-" + numeroFactura;
            String sql = "INSERT INTO dbo.tbl_factura " +
                    "(numero_factura, comprobante, fecha, id_suplidor, descripcion_factura, subtotal, itbis, descuento, total, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection con = conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, numeroFactura);
                ps.setString(2, comprobante);
                ps.setDate(3, Date.valueOf(dpFechaFactura.getValue()));
                ps.setInt(4, idSuplidor);
                ps.setString(5, descripcion);
                ps.setDouble(6, subtotal);
                ps.setDouble(7, itbis);
                ps.setDouble(8, descuento);
                ps.setDouble(9, total);
                ps.setString(10, estado);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Factura creada. Número: " + numeroFactura);
                limpiarFactura();
                cargarFacturas(filtroEstadoActual);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Verifica que los valores numéricos sean válidos.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error al registrar", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ─── Editar factura (solo estado y campos) ────────────────────────────────

    @FXML
    void onEditarFacturaClick(ActionEvent event) {
        if (idFacturaSeleccionada == 0) {
            mostrarAlerta("Advertencia", "Selecciona una factura de la tabla.", Alert.AlertType.WARNING);
            return;
        }
        if (txtIdSuplidor.getText().isEmpty() || txtSubtotal.getText().isEmpty() ||
                txtTotal.getText().isEmpty() || dpFechaFactura.getValue() == null ||
                cbEstadoFactura.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Completa todos los campos requeridos.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int    idSuplidor  = Integer.parseInt(txtIdSuplidor.getText().trim());
            double subtotal    = Double.parseDouble(txtSubtotal.getText().trim());
            double total       = Double.parseDouble(txtTotal.getText().trim());
            double itbis       = txtItbis.getText().trim().isEmpty()    ? 0 : Double.parseDouble(txtItbis.getText().trim());
            double descuento   = txtDescuento.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtDescuento.getText().trim());
            String descripcion = txtDescripcionFactura.getText().trim();
            String comprobante = txtNumeroFactura.getText().trim().isEmpty()
                    ? "FACT-" + idFacturaSeleccionada : txtNumeroFactura.getText().trim();
            String estado      = cbEstadoFactura.getValue();

            String sql = "UPDATE dbo.tbl_factura SET comprobante=?, fecha=?, id_suplidor=?, " +
                    "descripcion_factura=?, subtotal=?, itbis=?, descuento=?, total=?, estado=? " +
                    "WHERE numero_factura=?";

            try (Connection con = conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, comprobante);
                ps.setDate(2, Date.valueOf(dpFechaFactura.getValue()));
                ps.setInt(3, idSuplidor);
                ps.setString(4, descripcion);
                ps.setDouble(5, subtotal);
                ps.setDouble(6, itbis);
                ps.setDouble(7, descuento);
                ps.setDouble(8, total);
                ps.setString(9, estado);
                ps.setInt(10, idFacturaSeleccionada);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Factura actualizada correctamente.");
                limpiarFactura();
                cargarFacturas(filtroEstadoActual);
                idFacturaSeleccionada = 0;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores numéricos inválidos.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error al actualizar", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onLimpiarFacturaClick(ActionEvent event) {
        limpiarFactura();
        idFacturaSeleccionada = 0;
        tablaFacturas.getSelectionModel().clearSelection();
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

    // ─── Carga de tabla con filtro opcional ───────────────────────────────────

    private void cargarFacturas(String estadoFiltro) {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(
                "SELECT numero_factura, comprobante, fecha, id_suplidor, descripcion_factura, " +
                "subtotal, itbis, descuento, total, estado FROM dbo.tbl_factura");

        if (estadoFiltro != null) {
            sql.append(" WHERE estado = '").append(estadoFiltro).append("'");
        }
        sql.append(" ORDER BY numero_factura DESC");

        try (Connection con = conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql.toString())) {

            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("numero_factura"));
                fila.add(rs.getString("comprobante"));
                fila.add(rs.getString("fecha"));
                fila.add(rs.getString("id_suplidor"));
                fila.add(rs.getString("descripcion_factura") != null ? rs.getString("descripcion_factura") : "");
                fila.add(rs.getString("subtotal"));
                fila.add(rs.getString("itbis"));
                fila.add(rs.getString("descuento"));
                fila.add(rs.getString("total"));
                fila.add(rs.getString("estado"));
                datos.add(fila);
            }
            tablaFacturas.setItems(datos);

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar facturas: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void configurarColumnas() {
        colNumeroFactura.setCellValueFactory(p      -> new SimpleStringProperty(p.getValue().get(0)));
        colComprobante.setCellValueFactory(p        -> new SimpleStringProperty(p.getValue().get(1)));
        colFechaFactura.setCellValueFactory(p       -> new SimpleStringProperty(p.getValue().get(2)));
        colIdSuplidorFactura.setCellValueFactory(p  -> new SimpleStringProperty(p.getValue().get(3)));
        colDescripcionFactura.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(4)));
        colSubtotal.setCellValueFactory(p           -> new SimpleStringProperty(p.getValue().get(5)));
        colItbis.setCellValueFactory(p              -> new SimpleStringProperty(p.getValue().get(6)));
        colDescuento.setCellValueFactory(p          -> new SimpleStringProperty(p.getValue().get(7)));
        colTotal.setCellValueFactory(p              -> new SimpleStringProperty(p.getValue().get(8)));
        colEstadoFactura.setCellValueFactory(p      -> {
            String estado = p.getValue().get(9);
            String icon = switch (estado != null ? estado : "") {
                case "pagada"   -> "✅ pagada";
                case "anulada"  -> "❌ anulada";
                default         -> "⏳ Pendiente";
            };
            return new SimpleStringProperty(icon);
        });
    }

    private void limpiarFactura() {
        txtNumeroFactura.clear();
        txtIdSuplidor.clear();
        txtDescripcionFactura.clear();
        txtSubtotal.clear();
        txtItbis.clear();
        txtDescuento.clear();
        txtTotal.clear();
        dpFechaFactura.setValue(null);
        cbEstadoFactura.setValue(null);
    }

    // ─── Logo y conexión ──────────────────────────────────────────────────────

    private void cargarLogo() {
        try {
            java.io.File f = new java.io.File("IMG/Logo.png");
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
