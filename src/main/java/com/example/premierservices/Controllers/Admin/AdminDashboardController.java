package com.example.premierservices.Controllers.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {

    @FXML private Label lblFechaActual;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalProveedores;
    @FXML private Label lblTotalReservas;
    @FXML private Label lblTotalIngresos;
    @FXML private Label lblClientesMes;
    @FXML private Label lblProveedoresMes;
    @FXML private Label lblReservasMes;
    @FXML private Label lblIngresosMes;

    @FXML private BarChart<String, Number> barChartIngresos;
    @FXML private PieChart pieChartServicios;

    @FXML private Pane canvasClientesContainer;
    @FXML private Pane canvasPagosContainer;

    @FXML private TableView<ObservableList<String>> tablaUltimosPagos;
    @FXML private TableColumn<ObservableList<String>, String> colIdPago;
    @FXML private TableColumn<ObservableList<String>, String> colIdReserva;
    @FXML private TableColumn<ObservableList<String>, String> colMonto;
    @FXML private TableColumn<ObservableList<String>, String> colMetodoPago;
    @FXML private TableColumn<ObservableList<String>, String> colFechaPago;
    @FXML private TableColumn<ObservableList<String>, String> colEstadoPago;

    @FXML
    public void initialize() {
        lblFechaActual.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        colIdPago.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colIdReserva.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colMonto.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colMetodoPago.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colFechaPago.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colEstadoPago.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));

        cargarMetricas();
        cargarGraficoBarras();
        cargarGraficoPastel();
        cargarCanvasClientes();
        cargarCanvasPagos();
        cargarUltimosPagos();
    }

    private Connection conectar() {
        try {
            String url = "jdbc:sqlserver://26.228.126.202:1433;" +
                    "databaseName=PremierServicesV1;" +
                    "encrypt=true;trustServerCertificate=true";
            return DriverManager.getConnection(url, "wilenny", "1234");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void cargarMetricas() {
        try (Connection con = conectar()) {
            String sqlClientes = "SELECT COUNT(*) FROM dbo.tbl_clientes";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlClientes)) {
                if (rs.next()) lblTotalClientes.setText(String.valueOf(rs.getInt(1)));
            }

            String sqlProveedores = "SELECT COUNT(*) FROM dbo.tbl_suplidores";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlProveedores)) {
                if (rs.next()) lblTotalProveedores.setText(String.valueOf(rs.getInt(1)));
            }

            String sqlReservas = "SELECT COUNT(*) FROM dbo.tbl_reservas WHERE estado = 'Activa'";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlReservas)) {
                if (rs.next()) lblTotalReservas.setText(String.valueOf(rs.getInt(1)));
            }

            String sqlIngresos = "SELECT ISNULL(SUM(monto), 0) FROM dbo.tbl_pagos WHERE estado = 'Recibido'";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlIngresos)) {
                if (rs.next()) lblTotalIngresos.setText("$" + String.format("%,.2f", rs.getDouble(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarGraficoBarras() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos 2026");
        series.getData().add(new XYChart.Data<>("Ene", 12500));
        series.getData().add(new XYChart.Data<>("Feb", 14800));
        series.getData().add(new XYChart.Data<>("Mar", 16200));
        series.getData().add(new XYChart.Data<>("Abr", 18900));
        series.getData().add(new XYChart.Data<>("May", 21000));
        series.getData().add(new XYChart.Data<>("Jun", 23500));
        barChartIngresos.getData().clear();
        barChartIngresos.getData().add(series);
    }

    private void cargarGraficoPastel() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Fotografía", 35),
                new PieChart.Data("Videografía", 28),
                new PieChart.Data("Animación", 20),
                new PieChart.Data("Diseño Gráfico", 17)
        );
        pieChartServicios.setData(pieChartData);
        pieChartServicios.setLabelsVisible(true);
    }

    private void cargarCanvasClientes() {
        Canvas canvas = new Canvas(400, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int[] clientes = {45, 52, 58, 65, 72, 85};
        int max = 100;
        int height = 150;
        int barWidth = 50;
        int startX = 25;
        int startY = 30;

        gc.setFill(Color.web("#3498db"));
        for (int i = 0; i < clientes.length; i++) {
            int barHeight = (int) ((double) clientes[i] / max * height);
            gc.fillRect(startX + i * (barWidth + 5), startY + height - barHeight, barWidth, barHeight);
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(clientes[i]), startX + i * (barWidth + 5) + 15, startY + height - barHeight - 5);
            gc.fillText(getMes(i), startX + i * (barWidth + 5) + 15, startY + height + 15);
        }
        gc.setStroke(Color.GRAY);
        gc.strokeLine(startX, startY, startX, startY + height);
        gc.strokeLine(startX, startY + height, startX + 400, startY + height);

        canvasClientesContainer.getChildren().clear();
        canvasClientesContainer.getChildren().add(canvas);
    }

    private void cargarCanvasPagos() {
        Canvas canvas = new Canvas(400, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        String[] estados = {"Pendiente", "Recibido", "Anulado"};
        int[] valores = {45, 120, 15};
        int max = 150;
        int height = 150;
        int barWidth = 80;
        int startX = 25;
        int startY = 30;
        Color[] colores = {Color.web("#f39c12"), Color.web("#27ae60"), Color.web("#e74c3c")};

        for (int i = 0; i < estados.length; i++) {
            int barHeight = (int) ((double) valores[i] / max * height);
            gc.setFill(colores[i]);
            gc.fillRect(startX + i * (barWidth + 10), startY + height - barHeight, barWidth, barHeight);
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(valores[i]), startX + i * (barWidth + 10) + 30, startY + height - barHeight - 5);
            gc.fillText(estados[i], startX + i * (barWidth + 10) + 20, startY + height + 15);
        }
        gc.setStroke(Color.GRAY);
        gc.strokeLine(startX, startY, startX, startY + height);
        gc.strokeLine(startX, startY + height, startX + 400, startY + height);

        canvasPagosContainer.getChildren().clear();
        canvasPagosContainer.getChildren().add(canvas);
    }

    private String getMes(int index) {
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun"};
        return meses[index];
    }

    private void cargarUltimosPagos() {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();
        try (Connection con = conectar(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT TOP 10 id_pago, id_reserva, monto, metodo_pago, fecha_pago, estado FROM dbo.tbl_pagos ORDER BY id_pago DESC")) {
            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                fila.add(rs.getString("id_pago"));
                fila.add(rs.getString("id_reserva"));
                fila.add("$" + rs.getString("monto"));
                fila.add(rs.getString("metodo_pago"));
                fila.add(rs.getString("fecha_pago"));
                fila.add(rs.getString("estado"));
                datos.add(fila);
            }
            tablaUltimosPagos.setItems(datos);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onActualizarClick(ActionEvent event) {
        cargarMetricas();
        cargarUltimosPagos();
    }

    @FXML
    void onVolverClick(ActionEvent event) {
        System.out.println("Volver al panel admin");


    }
}