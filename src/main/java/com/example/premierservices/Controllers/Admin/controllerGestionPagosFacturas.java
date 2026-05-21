package com.example.premierservices.Controllers.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class controllerGestionPagosFacturas {

    // Campos de Factura
    @FXML private TextField txtNumeroFactura;
    @FXML private TextField txtItbis;
    @FXML private TextField txtIdSuplidor;
    @FXML private TextField txtDescuento;
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtTotal;
    @FXML private TextField txtDescripcionFactura;
    @FXML private DatePicker dpFechaFactura;
    @FXML private ChoiceBox<String> cbEstadoFactura;
    @FXML private ImageView Logoimg;

    // Tabla Facturas
    @FXML private TableView<ObservableList<String>> tablaFacturas;

    // Columnas Facturas
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

    @FXML
    public void initialize() {
        // Configurar ChoiceBox de estados
        ObservableList<String> estadosFactura = FXCollections.observableArrayList("Pendiente", "pagada", "anulada");
        cbEstadoFactura.setItems(estadosFactura);

        // Cargar logo
        cargarLogo();

        // Cargar datos
        cargarFacturas();

        // Listener para selección en tabla
        tablaFacturas.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                seleccionarFactura(newVal);
            }
        });
    }

    private void cargarLogo() {
        try {
            java.io.File logoFile = new java.io.File("IMG/Logo.png");
            if (logoFile.exists()) {
                Logoimg.setImage(new javafx.scene.image.Image(logoFile.toURI().toString()));
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void seleccionarFactura(ObservableList<String> factura) {
        if (factura.size() >= 10) {
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
    void onRegistrarFacturaClick(ActionEvent event) {
        if (txtIdSuplidor.getText().isEmpty() || txtSubtotal.getText().isEmpty() ||
                txtTotal.getText().isEmpty() || dpFechaFactura.getValue() == null ||
                cbEstadoFactura.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos obligatorios de la factura.\n" +
                    "Campos requeridos: Suplidor, Sub-Total, Total, Fecha y Estado.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idSuplidor = Integer.parseInt(txtIdSuplidor.getText().trim());
            double subtotal = Double.parseDouble(txtSubtotal.getText().trim());
            double total = Double.parseDouble(txtTotal.getText().trim());
            double itbis = txtItbis.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtItbis.getText().trim());
            double descuento = txtDescuento.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtDescuento.getText().trim());
            String descripcionFactura = txtDescripcionFactura.getText().trim();

            // Obtener el próximo número de factura
            int numeroFactura;
            try {
                String sqlMax = "SELECT ISNULL(MAX(numero_factura), 0) + 1 AS next_num FROM dbo.tbl_factura";
                try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlMax)) {
                    if (rs.next()) {
                        numeroFactura = rs.getInt("next_num");
                    } else {
                        numeroFactura = 1;
                    }
                }
            } catch (SQLException e) {
                numeroFactura = (int) (System.currentTimeMillis() % 10000);
            }

            String comprobante = "FACT-" + numeroFactura;
            String estado = cbEstadoFactura.getValue();

            String sql = "INSERT INTO dbo.tbl_factura (numero_factura, comprobante, fecha, id_suplidor, descripcion_factura, subtotal, itbis, descuento, total, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, numeroFactura);
                ps.setString(2, comprobante);
                ps.setDate(3, Date.valueOf(dpFechaFactura.getValue()));
                ps.setInt(4, idSuplidor);
                ps.setString(5, descripcionFactura);
                ps.setDouble(6, subtotal);
                ps.setDouble(7, itbis);
                ps.setDouble(8, descuento);
                ps.setDouble(9, total);
                ps.setString(10, estado);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Factura registrada correctamente.\nNúmero: " + numeroFactura);
                limpiarFactura();
                cargarFacturas();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores numéricos inválidos. Verifica que los campos numéricos contengan solo números.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al registrar factura: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onEditarFacturaClick(ActionEvent event) {
        if (idFacturaSeleccionada == 0) {
            mostrarAlerta("Advertencia", "Seleccione una factura de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        if (txtIdSuplidor.getText().isEmpty() || txtSubtotal.getText().isEmpty() ||
                txtTotal.getText().isEmpty() || dpFechaFactura.getValue() == null ||
                cbEstadoFactura.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos obligatorios de la factura.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idSuplidor = Integer.parseInt(txtIdSuplidor.getText().trim());
            double subtotal = Double.parseDouble(txtSubtotal.getText().trim());
            double total = Double.parseDouble(txtTotal.getText().trim());
            double itbis = txtItbis.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtItbis.getText().trim());
            double descuento = txtDescuento.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtDescuento.getText().trim());
            String descripcionFactura = txtDescripcionFactura.getText().trim();
            String comprobante = txtNumeroFactura.getText().trim();
            if (comprobante.isEmpty()) comprobante = "FACT-" + idFacturaSeleccionada;
            String estado = cbEstadoFactura.getValue();

            String sql = "UPDATE dbo.tbl_factura SET comprobante=?, fecha=?, id_suplidor=?, descripcion_factura=?, subtotal=?, itbis=?, descuento=?, total=?, estado=? WHERE numero_factura=?";

            try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, comprobante);
                ps.setDate(2, Date.valueOf(dpFechaFactura.getValue()));
                ps.setInt(3, idSuplidor);
                ps.setString(4, descripcionFactura);
                ps.setDouble(5, subtotal);
                ps.setDouble(6, itbis);
                ps.setDouble(7, descuento);
                ps.setDouble(8, total);
                ps.setString(9, estado);
                ps.setInt(10, idFacturaSeleccionada);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Factura actualizada correctamente.");
                limpiarFactura();
                cargarFacturas();
                idFacturaSeleccionada = 0;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores numéricos inválidos.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar factura: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onEliminarFacturaClick(ActionEvent event) {
        if (idFacturaSeleccionada == 0) {
            mostrarAlerta("Advertencia", "Seleccione una factura de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Eliminar factura?");
        confirmacion.setContentText("Esta acción no se puede deshacer. ¿Está seguro?");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.tbl_factura WHERE numero_factura = ?")) {
                ps.setInt(1, idFacturaSeleccionada);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Factura eliminada correctamente.");
                limpiarFactura();
                cargarFacturas();
                idFacturaSeleccionada = 0;
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al eliminar factura: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void onLimpiarFacturaClick(ActionEvent event) {
        limpiarFactura();
        idFacturaSeleccionada = 0;
        tablaFacturas.getSelectionModel().clearSelection();
    }

    @FXML
    void onAtrasClick(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/AdminPanel.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Panel Admin - Premier Services");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar al panel: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarFacturas() {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();

        colNumeroFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colComprobante.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colFechaFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colIdSuplidorFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colDescripcionFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colSubtotal.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colItbis.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));
        colDescuento.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(7)));
        colTotal.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(8)));
        colEstadoFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(9)));

        String sql = "SELECT numero_factura, comprobante, fecha, id_suplidor, descripcion_factura, subtotal, itbis, descuento, total, estado FROM dbo.tbl_factura ORDER BY numero_factura DESC";

        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
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
        }
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