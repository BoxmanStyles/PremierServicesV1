package com.example.premierservices.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML private DatePicker dpFechaFactura;
    @FXML private ChoiceBox<String> cbEstadoFactura;

    // Campos de Pago
    @FXML private TextField txtIdReserva;
    @FXML private TextField txtTipoServicio;
    @FXML private TextField txtMontoPago;
    @FXML private ChoiceBox<String> cbMetodoPago;
    @FXML private ChoiceBox<String> cbEstadoPago;
    @FXML private DatePicker dpFechaPago;

    // Tablas
    @FXML private TableView<ObservableList<String>> tablaPagos;
    @FXML private TableView<ObservableList<String>> tablaFacturas;

    // Columnas Pagos
    @FXML private TableColumn<ObservableList<String>, String> colIdPago;
    @FXML private TableColumn<ObservableList<String>, String> colIdReserva;
    @FXML private TableColumn<ObservableList<String>, String> colMonto;
    @FXML private TableColumn<ObservableList<String>, String> colMetodoPago;
    @FXML private TableColumn<ObservableList<String>, String> colFechaPago;
    @FXML private TableColumn<ObservableList<String>, String> colEstadoPago;
    @FXML private TableColumn<ObservableList<String>, String> colTipoServicio;

    // Columnas Facturas
    @FXML private TableColumn<ObservableList<String>, String> colNumeroFactura;
    @FXML private TableColumn<ObservableList<String>, String> colComprobante;
    @FXML private TableColumn<ObservableList<String>, String> colFechaFactura;
    @FXML private TableColumn<ObservableList<String>, String> colIdSuplidorFactura;
    @FXML private TableColumn<ObservableList<String>, String> colSubtotal;
    @FXML private TableColumn<ObservableList<String>, String> colItbis;
    @FXML private TableColumn<ObservableList<String>, String> colDescuento;
    @FXML private TableColumn<ObservableList<String>, String> colTotal;
    @FXML private TableColumn<ObservableList<String>, String> colEstadoFactura;

    private int idPagoSeleccionado = 0;
    private int idFacturaSeleccionada = 0;

    @FXML
    public void initialize() {
        // ========== CONFIGURACIÓN PARA FACTURA ==========
        // Usar los valores EXACTOS que permite la restricción CHK_Estado
        // Valores permitidos: 'Pendiente', 'pagada', 'anulada'
        ObservableList<String> estadosFactura = FXCollections.observableArrayList("Pendiente", "pagada", "anulada");
        cbEstadoFactura.setItems(estadosFactura);

        // ========== CONFIGURACIÓN PARA PAGO ==========
        ObservableList<String> metodosPago = FXCollections.observableArrayList("Efectivo", "Tarjeta Credito", "Tarjeta Debito", "Transferencia", "PayPal");
        cbMetodoPago.setItems(metodosPago);

        ObservableList<String> estadosPago = FXCollections.observableArrayList("Pendiente", "Recibido", "Anulado");
        cbEstadoPago.setItems(estadosPago);

        // Cargar datos
        cargarPagos();
        cargarFacturas();

        // Listeners para selección en tablas
        tablaPagos.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                seleccionarPago(newVal);
            }
        });

        tablaFacturas.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                seleccionarFactura(newVal);
            }
        });
    }

    private void seleccionarPago(ObservableList<String> pago) {
        if (pago.size() >= 7) {
            try {
                idPagoSeleccionado = Integer.parseInt(pago.get(0));
                txtIdReserva.setText(pago.get(1));
                txtMontoPago.setText(pago.get(2));
                cbMetodoPago.setValue(pago.get(3));
                if (pago.get(4) != null && !pago.get(4).isEmpty()) {
                    dpFechaPago.setValue(LocalDate.parse(pago.get(4).split(" ")[0]));
                }
                cbEstadoPago.setValue(pago.get(5));
                txtTipoServicio.setText(pago.get(6) != null ? pago.get(6) : "");
            } catch (Exception e) {
                System.err.println("Error al seleccionar pago: " + e.getMessage());
            }
        }
    }

    private void seleccionarFactura(ObservableList<String> factura) {
        if (factura.size() >= 9) {
            try {
                idFacturaSeleccionada = Integer.parseInt(factura.get(0));
                txtNumeroFactura.setText(factura.get(1));
                txtIdSuplidor.setText(factura.get(3));
                txtSubtotal.setText(factura.get(4));
                txtItbis.setText(factura.get(5));
                txtDescuento.setText(factura.get(6));
                txtTotal.setText(factura.get(7));
                if (factura.get(2) != null && !factura.get(2).isEmpty()) {
                    dpFechaFactura.setValue(LocalDate.parse(factura.get(2)));
                }
                cbEstadoFactura.setValue(factura.get(8));
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

    // ==================== MÉTODOS PARA PAGOS ====================

    @FXML
    void onRegistrarPagoClick(ActionEvent event) {
        if (txtIdReserva.getText().isEmpty() || txtMontoPago.getText().isEmpty() ||
                txtTipoServicio.getText().isEmpty() || cbMetodoPago.getValue() == null ||
                cbEstadoPago.getValue() == null || dpFechaPago.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos del pago.\n" +
                    "Campos requeridos: ID Reserva, Monto, Tipo de Servicio, Método de Pago, Estado y Fecha.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idReserva = Integer.parseInt(txtIdReserva.getText().trim());
            double monto = Double.parseDouble(txtMontoPago.getText().trim());
            String tipoServicio = txtTipoServicio.getText().trim();

            String sql = "INSERT INTO dbo.tbl_pagos (id_reserva, monto, metodo_pago, fecha_pago, estado, tipo_servicio) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idReserva);
                ps.setDouble(2, monto);
                ps.setString(3, cbMetodoPago.getValue());
                ps.setDate(4, Date.valueOf(dpFechaPago.getValue()));
                ps.setString(5, cbEstadoPago.getValue());
                ps.setString(6, tipoServicio);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Pago registrado correctamente.\n" +
                        "Reserva: " + idReserva + "\n" +
                        "Servicio: " + tipoServicio + "\n" +
                        "Monto: $" + monto);
                limpiarPago();
                cargarPagos();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores numéricos inválidos.\n" +
                    "Asegúrate de que el ID de Reserva y el Monto sean números válidos.\n" +
                    "Ejemplo: ID Reserva = 1, Monto = 500.00", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al registrar pago: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onEditarPagoClick(ActionEvent event) {
        if (idPagoSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un pago de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        if (txtIdReserva.getText().isEmpty() || txtMontoPago.getText().isEmpty() ||
                txtTipoServicio.getText().isEmpty() || cbMetodoPago.getValue() == null ||
                cbEstadoPago.getValue() == null || dpFechaPago.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos del pago.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idReserva = Integer.parseInt(txtIdReserva.getText().trim());
            double monto = Double.parseDouble(txtMontoPago.getText().trim());
            String tipoServicio = txtTipoServicio.getText().trim();

            String sql = "UPDATE dbo.tbl_pagos SET id_reserva=?, monto=?, metodo_pago=?, fecha_pago=?, estado=?, tipo_servicio=? WHERE id_pago=?";

            try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idReserva);
                ps.setDouble(2, monto);
                ps.setString(3, cbMetodoPago.getValue());
                ps.setDate(4, Date.valueOf(dpFechaPago.getValue()));
                ps.setString(5, cbEstadoPago.getValue());
                ps.setString(6, tipoServicio);
                ps.setInt(7, idPagoSeleccionado);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Pago actualizado correctamente.");
                limpiarPago();
                cargarPagos();
                idPagoSeleccionado = 0;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores numéricos inválidos.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar pago: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void onEliminarPagoClick(ActionEvent event) {
        if (idPagoSeleccionado == 0) {
            mostrarAlerta("Advertencia", "Seleccione un pago de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Eliminar pago?");
        confirmacion.setContentText("Esta acción no se puede deshacer. ¿Está seguro?");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.tbl_pagos WHERE id_pago = ?")) {
                ps.setInt(1, idPagoSeleccionado);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Pago eliminado correctamente.");
                limpiarPago();
                cargarPagos();
                idPagoSeleccionado = 0;
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al eliminar pago: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void onLimpiarPagoClick(ActionEvent event) {
        limpiarPago();
        idPagoSeleccionado = 0;
        tablaPagos.getSelectionModel().clearSelection();
    }

    // ==================== MÉTODOS PARA FACTURAS ====================

    @FXML
    void onRegistrarFacturaClick(ActionEvent event) {
        if (txtIdSuplidor.getText().isEmpty() || txtSubtotal.getText().isEmpty() ||
                txtTotal.getText().isEmpty() || dpFechaFactura.getValue() == null || cbEstadoFactura.getValue() == null) {
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

            // Generar comprobante - NUNCA puede ser nulo
            String comprobante = txtNumeroFactura.getText().trim();
            if (comprobante == null || comprobante.isEmpty()) {
                comprobante = "FACT-" + numeroFactura;
            }

            // Obtener el estado seleccionado (ya está en el formato correcto)
            String estado = cbEstadoFactura.getValue();

            // Debug
            System.out.println("=== Registrando Factura ===");
            System.out.println("Número Factura: " + numeroFactura);
            System.out.println("Comprobante: " + comprobante);
            System.out.println("Fecha: " + dpFechaFactura.getValue());
            System.out.println("ID Suplidor: " + idSuplidor);
            System.out.println("Subtotal: " + subtotal);
            System.out.println("ITBIS: " + itbis);
            System.out.println("Descuento: " + descuento);
            System.out.println("Total: " + total);
            System.out.println("Estado: '" + estado + "'");

            String sql = "INSERT INTO dbo.tbl_factura (numero_factura, comprobante, fecha, id_suplidor, subtotal, itbis, descuento, total, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, numeroFactura);
                ps.setString(2, comprobante);
                ps.setDate(3, Date.valueOf(dpFechaFactura.getValue()));
                ps.setInt(4, idSuplidor);
                ps.setDouble(5, subtotal);
                ps.setDouble(6, itbis);
                ps.setDouble(7, descuento);
                ps.setDouble(8, total);
                ps.setString(9, estado);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Factura registrada correctamente.\nNúmero: " + numeroFactura + "\nComprobante: " + comprobante);
                limpiarFactura();
                cargarFacturas();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores numéricos inválidos.\n" +
                    "Asegúrate de que los campos numéricos contengan solo números.\n" +
                    "Ejemplos válidos:\n" +
                    "- Suplidor: 1\n" +
                    "- Sub-Total: 1000\n" +
                    "- ITBIS: 18\n" +
                    "- Descuento: 25\n" +
                    "- Total: 1180", Alert.AlertType.ERROR);
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
                txtTotal.getText().isEmpty() || dpFechaFactura.getValue() == null || cbEstadoFactura.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos obligatorios de la factura.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int idSuplidor = Integer.parseInt(txtIdSuplidor.getText().trim());
            double subtotal = Double.parseDouble(txtSubtotal.getText().trim());
            double total = Double.parseDouble(txtTotal.getText().trim());
            double itbis = txtItbis.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtItbis.getText().trim());
            double descuento = txtDescuento.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtDescuento.getText().trim());

            String comprobante = txtNumeroFactura.getText().trim();
            if (comprobante == null || comprobante.isEmpty()) {
                comprobante = "FACT-" + idFacturaSeleccionada;
            }

            String estado = cbEstadoFactura.getValue();

            String sql = "UPDATE dbo.tbl_factura SET comprobante=?, fecha=?, id_suplidor=?, subtotal=?, itbis=?, descuento=?, total=?, estado=? WHERE numero_factura=?";

            try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, comprobante);
                ps.setDate(2, Date.valueOf(dpFechaFactura.getValue()));
                ps.setInt(3, idSuplidor);
                ps.setDouble(4, subtotal);
                ps.setDouble(5, itbis);
                ps.setDouble(6, descuento);
                ps.setDouble(7, total);
                ps.setString(8, estado);
                ps.setInt(9, idFacturaSeleccionada);
                ps.executeUpdate();

                mostrarInfo("Éxito", "Factura actualizada correctamente.");
                limpiarFactura();
                cargarFacturas();
                idFacturaSeleccionada = 0;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores numéricos inválidos.\n" +
                    "Asegúrate de que los campos numéricos contengan solo números.", Alert.AlertType.ERROR);
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
        System.out.println("Regresar...");
    }

    // ==================== MÉTODOS PARA CARGAR DATOS ====================

    private void cargarPagos() {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();

        colIdPago.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colIdReserva.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colMonto.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colMetodoPago.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colFechaPago.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colEstadoPago.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colTipoServicio.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));

        try (Connection con = conectar(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_pago, id_reserva, monto, metodo_pago, fecha_pago, estado, tipo_servicio FROM dbo.tbl_pagos ORDER BY id_pago DESC")) {

            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("id_pago"));
                fila.add(rs.getString("id_reserva"));
                fila.add(rs.getString("monto"));
                fila.add(rs.getString("metodo_pago"));
                fila.add(rs.getString("fecha_pago"));
                fila.add(rs.getString("estado"));
                fila.add(rs.getString("tipo_servicio") != null ? rs.getString("tipo_servicio") : "");
                datos.add(fila);
            }
            tablaPagos.setItems(datos);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar pagos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarFacturas() {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();

        colNumeroFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colComprobante.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colFechaFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colIdSuplidorFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colSubtotal.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colItbis.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colDescuento.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));
        colTotal.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(7)));
        colEstadoFactura.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(8)));

        try (Connection con = conectar(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT numero_factura, comprobante, fecha, id_suplidor, subtotal, itbis, descuento, total, estado FROM dbo.tbl_factura ORDER BY numero_factura DESC")) {

            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("numero_factura"));
                fila.add(rs.getString("comprobante"));
                fila.add(rs.getString("fecha"));
                fila.add(rs.getString("id_suplidor"));
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

    // ==================== MÉTODOS AUXILIARES ====================

    private void limpiarPago() {
        txtIdReserva.clear();
        txtTipoServicio.clear();
        txtMontoPago.clear();
        cbMetodoPago.setValue(null);
        cbEstadoPago.setValue(null);
        dpFechaPago.setValue(null);
    }

    private void limpiarFactura() {
        txtNumeroFactura.clear();
        txtIdSuplidor.clear();
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