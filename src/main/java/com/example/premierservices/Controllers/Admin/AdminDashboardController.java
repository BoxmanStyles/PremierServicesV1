package com.example.premierservices.Controllers.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardController {

    @FXML private Label lblFechaActual;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalProveedores;
    @FXML private Label lblTotalReservas;
    @FXML private Label lblTotalIngresos;
    @FXML private Label lblReservasPendientes;
    @FXML private Label lblReservasConfirmadas;
    @FXML private Label lblReservasCompletadas;

    @FXML private BarChart<String, Number> barChartIngresos;
    @FXML private PieChart pieChartServicios;

    @FXML private Pane canvasClientesContainer;
    @FXML private Pane canvasReservasContainer;

    @FXML private TableView<ObservableList<String>> tablaUltimasReservas;
    @FXML private TableColumn<ObservableList<String>, String> colIdReserva;
    @FXML private TableColumn<ObservableList<String>, String> colCliente;
    @FXML private TableColumn<ObservableList<String>, String> colServicio;
    @FXML private TableColumn<ObservableList<String>, String> colProveedor;
    @FXML private TableColumn<ObservableList<String>, String> colFechaEvento;
    @FXML private TableColumn<ObservableList<String>, String> colMonto;
    @FXML private TableColumn<ObservableList<String>, String> colEstado;

    @FXML
    public void initialize() {
        lblFechaActual.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Configurar columnas de la tabla
        colIdReserva.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
        colCliente.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
        colServicio.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
        colProveedor.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(3)));
        colFechaEvento.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(4)));
        colMonto.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(5)));
        colEstado.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(6)));

        cargarMetricas();
        cargarGraficoBarras();
        cargarGraficoPastel();
        cargarCanvasClientes();
        cargarCanvasReservas();
        cargarUltimasReservas();
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
            // Total Clientes
            String sqlClientes = "SELECT COUNT(*) FROM dbo.tbl_clientes";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlClientes)) {
                if (rs.next()) lblTotalClientes.setText(String.valueOf(rs.getInt(1)));
            }

            // Total Proveedores
            String sqlProveedores = "SELECT COUNT(*) FROM dbo.tbl_suplidores";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlProveedores)) {
                if (rs.next()) lblTotalProveedores.setText(String.valueOf(rs.getInt(1)));
            }

            // Total Reservas y por estado
            String sqlReservas = "SELECT estado, COUNT(*) FROM dbo.tbl_reservas GROUP BY estado";
            int total = 0, pendientes = 0, confirmadas = 0, completadas = 0;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlReservas)) {
                while (rs.next()) {
                    String estado = rs.getString(1);
                    int count = rs.getInt(2);
                    total += count;
                    switch (estado.toLowerCase()) {
                        case "pendiente": pendientes = count; break;
                        case "confirmada": confirmadas = count; break;
                        case "completada": completadas = count; break;
                    }
                }
            }
            lblTotalReservas.setText(String.valueOf(total));
            lblReservasPendientes.setText(String.valueOf(pendientes));
            lblReservasConfirmadas.setText(String.valueOf(confirmadas));
            lblReservasCompletadas.setText(String.valueOf(completadas));

            // Total Ingresos (suma de total de reservas confirmadas y completadas)
            String sqlIngresos = "SELECT ISNULL(SUM(total), 0) FROM dbo.tbl_reservas WHERE estado IN ('confirmada', 'completada')";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlIngresos)) {
                if (rs.next()) lblTotalIngresos.setText("$" + String.format("%,.2f", rs.getDouble(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarGraficoBarras() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos Mensuales");

        // Obtener ingresos reales por mes
        Map<Integer, Double> ingresosPorMes = new HashMap<>();
        for (int i = 1; i <= 12; i++) ingresosPorMes.put(i, 0.0);

        String sql = "SELECT MONTH(fecha_evento) AS mes, ISNULL(SUM(total), 0) AS total " +
                "FROM dbo.tbl_reservas WHERE estado IN ('confirmada', 'completada') AND YEAR(fecha_evento) = YEAR(GETDATE()) " +
                "GROUP BY MONTH(fecha_evento)";

        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ingresosPorMes.put(rs.getInt("mes"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        for (int i = 1; i <= 12; i++) {
            series.getData().add(new XYChart.Data<>(meses[i-1], ingresosPorMes.get(i)));
        }

        barChartIngresos.getData().clear();
        barChartIngresos.getData().add(series);
    }

    private void cargarGraficoPastel() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        String sql = "SELECT s.categoria, COUNT(*) as total " +
                "FROM dbo.tbl_reservas r " +
                "INNER JOIN dbo.tbl_servicios s ON r.id_servicio = s.id_servicio " +
                "GROUP BY s.categoria";

        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                pieChartData.add(new PieChart.Data(rs.getString("categoria"), rs.getInt("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Sin datos", 1));
        }

        pieChartServicios.setData(pieChartData);
        pieChartServicios.setLabelsVisible(true);
    }

    private void cargarCanvasClientes() {
        Canvas canvas = new Canvas(400, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Obtener clientes por mes del año actual
        int[] clientesPorMes = new int[6];
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun"};

        String sql = "SELECT MONTH(fecha_registro) AS mes, COUNT(*) as total " +
                "FROM dbo.tbl_usuarios WHERE tipo_usuario = 'cliente' AND YEAR(fecha_registro) = YEAR(GETDATE()) " +
                "GROUP BY MONTH(fecha_registro) ORDER BY mes";

        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int mes = rs.getInt("mes");
                if (mes >= 1 && mes <= 6) {
                    clientesPorMes[mes - 1] = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int max = 1;
        for (int valor : clientesPorMes) {
            if (valor > max) max = valor;
        }
        if (max == 0) max = 1;

        int height = 150;
        int barWidth = 50;
        int startX = 25;
        int startY = 30;

        gc.setFill(Color.web("#3498db"));
        for (int i = 0; i < clientesPorMes.length; i++) {
            int barHeight = (int) ((double) clientesPorMes[i] / max * height);
            gc.fillRect(startX + i * (barWidth + 5), startY + height - barHeight, barWidth, barHeight);
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(clientesPorMes[i]), startX + i * (barWidth + 5) + 15, startY + height - barHeight - 5);
            gc.fillText(meses[i], startX + i * (barWidth + 5) + 15, startY + height + 15);
        }
        gc.setStroke(Color.GRAY);
        gc.strokeLine(startX, startY, startX, startY + height);
        gc.strokeLine(startX, startY + height, startX + 400, startY + height);

        canvasClientesContainer.getChildren().clear();
        canvasClientesContainer.getChildren().add(canvas);
    }

    private void cargarCanvasReservas() {
        Canvas canvas = new Canvas(400, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Estados de reservas
        String[] estados = {"Pendiente", "Confirmada", "Completada"};
        int[] valores = new int[3];
        Color[] colores = {Color.web("#f39c12"), Color.web("#27ae60"), Color.web("#3498db")};

        String sql = "SELECT estado, COUNT(*) FROM dbo.tbl_reservas GROUP BY estado";
        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String estado = rs.getString(1);
                int count = rs.getInt(2);
                switch (estado.toLowerCase()) {
                    case "pendiente": valores[0] = count; break;
                    case "confirmada": valores[1] = count; break;
                    case "completada": valores[2] = count; break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int max = 1;
        for (int valor : valores) {
            if (valor > max) max = valor;
        }
        if (max == 0) max = 1;

        int height = 150;
        int barWidth = 80;
        int startX = 25;
        int startY = 30;

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

        canvasReservasContainer.getChildren().clear();
        canvasReservasContainer.getChildren().add(canvas);
    }

    private void cargarUltimasReservas() {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();

        String sql = "SELECT TOP 10 " +
                "r.id_reserva, " +
                "CONCAT(c.nombre, ' ', c.apellido) AS cliente, " +
                "s.nombre_servicio, " +
                "p.nombre_empresa AS proveedor, " +
                "FORMAT(r.fecha_evento, 'yyyy-MM-dd') AS fecha_evento, " +
                "r.total, " +
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
                fila.add("$" + rs.getString("total"));

                String estado = rs.getString("estado");
                fila.add(estado);
                datos.add(fila);
            }
            tablaUltimasReservas.setItems(datos);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onActualizarClick(ActionEvent event) {
        cargarMetricas();
        cargarGraficoBarras();
        cargarGraficoPastel();
        cargarCanvasClientes();
        cargarCanvasReservas();
        cargarUltimasReservas();
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
        }
    }
}