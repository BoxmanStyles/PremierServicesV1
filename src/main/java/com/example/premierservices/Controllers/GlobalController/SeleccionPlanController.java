package com.example.premierservices.Controllers.GlobalController;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.SwingUtilities;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SeleccionPlanController {

    @FXML private Button btnAtras;
    @FXML private Button btnRookie;
    @FXML private Button btnElite;
    @FXML private Button btnPrime;
    @FXML private Button btnSiguiente;
    @FXML private Label labelSeleccionado;

    private String planSeleccionado = null;
    private int idSuplidor;

    private static final int PLAN_ROOKIE_ID = 1;
    private static final int PLAN_ELITE_ID = 2;
    private static final int PLAN_PRIME_ID = 3;

    @FXML
    public void initialize() {
        System.out.println("SeleccionPlanController inicializado");
    }

    public void setIdSuplidor(int id) {
        this.idSuplidor = id;
        System.out.println("ID Suplidor recibido: " + idSuplidor);
    }

    @FXML
    void irARegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegistrateProveedor.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Proveedor - Premier Services");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver al registro: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onRookieClicked(MouseEvent event) {
        planSeleccionado = "Rookie";
        labelSeleccionado.setText("Plan seleccionado: Rookie (Gratis)");
        actualizarBotonSiguiente();
    }

    @FXML
    void onEliteClicked(MouseEvent event) {
        planSeleccionado = "Elite";
        labelSeleccionado.setText("Plan seleccionado: Elite - USD 29/mes");
        actualizarBotonSiguiente();
    }

    @FXML
    void onPrimeClicked(MouseEvent event) {
        planSeleccionado = "Prime";
        labelSeleccionado.setText("Plan seleccionado: Prime - USD 79/mes");
        actualizarBotonSiguiente();
    }

    @FXML
    void seleccionarRookie(ActionEvent event) {
        planSeleccionado = "Rookie";
        labelSeleccionado.setText("Plan seleccionado: Rookie (Gratis)");
        actualizarBotonSiguiente();
    }

    @FXML
    void seleccionarElite(ActionEvent event) {
        planSeleccionado = "Elite";
        labelSeleccionado.setText("Plan seleccionado: Elite - USD 29/mes");
        actualizarBotonSiguiente();
    }

    @FXML
    void seleccionarPrime(ActionEvent event) {
        planSeleccionado = "Prime";
        labelSeleccionado.setText("Plan seleccionado: Prime - USD 79/mes");
        actualizarBotonSiguiente();
    }

    private void actualizarBotonSiguiente() {
        btnSiguiente.setStyle("-fx-background-color:#4A7FA9; -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:15px; -fx-background-radius:12; -fx-padding:13 60 13 60;");
    }

    @FXML
    void guardarSeleccion(ActionEvent event) {
        if (planSeleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione un plan para continuar.", Alert.AlertType.WARNING);
            return;
        }

        System.out.println("Plan seleccionado: " + planSeleccionado);
        System.out.println("ID Suplidor: " + idSuplidor);

        boolean guardadoExitoso = guardarPlanEnBaseDatos(planSeleccionado);

        if (guardadoExitoso) {
            if (planSeleccionado.equals("Elite") || planSeleccionado.equals("Prime")) {
                int numeroFactura = crearFacturaEnBD(planSeleccionado);
                if (numeroFactura > 0) {
                    generarYMostrarFactura(idSuplidor, numeroFactura, event);
                } else {
                    mostrarAlerta("Error", "No se pudo generar la factura.", Alert.AlertType.ERROR);
                    irALogin(event);
                }
            } else {
                mostrarAlerta("Éxito", "Plan " + planSeleccionado + " activado correctamente.", Alert.AlertType.INFORMATION);
                irALogin(event);
            }
        } else {
            mostrarAlerta("Error", "No se pudo guardar el plan seleccionado.", Alert.AlertType.ERROR);
        }
    }

    private boolean guardarPlanEnBaseDatos(String plan) {
        int planId = switch (plan) {
            case "Rookie" -> PLAN_ROOKIE_ID;
            case "Elite" -> PLAN_ELITE_ID;
            case "Prime" -> PLAN_PRIME_ID;
            default -> PLAN_ROOKIE_ID;
        };

        String sql = "UPDATE tbl_suplidores SET plan_id = ? WHERE id_suplidor = ?";

        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, planId);
            pst.setInt(2, idSuplidor);
            int filas = pst.executeUpdate();
            System.out.println("Plan guardado en BD, filas actualizadas: " + filas);
            return filas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int crearFacturaEnBD(String plan) {
        double monto = plan.equals("Elite") ? 29.00 : 79.00;
        String comprobante = "FACT-PLAN-" + idSuplidor + "-" + System.currentTimeMillis();

        int numeroFactura = 1;
        String sqlMax = "SELECT ISNULL(MAX(numero_factura), 0) + 1 AS next_num FROM dbo.tbl_factura";
        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlMax)) {
            if (rs.next()) {
                numeroFactura = rs.getInt("next_num");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "INSERT INTO dbo.tbl_factura (numero_factura, comprobante, fecha, id_suplidor, subtotal, total, estado, descripcion_factura) " +
                "VALUES (?, ?, GETDATE(), ?, ?, ?, 'pagada', ?)";

        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, numeroFactura);
            pst.setString(2, comprobante);
            pst.setInt(3, idSuplidor);
            pst.setDouble(4, monto);
            pst.setDouble(5, monto);
            pst.setString(6, "Factura por suscripción al plan " + plan + " - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            pst.executeUpdate();
            System.out.println("Factura #" + numeroFactura + " creada en BD para plan " + plan);
            return numeroFactura;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Genera la factura PDF y la muestra en el visor de JasperReports (Swing)
     * El usuario puede imprimir, guardar o exportar desde el visor
     */
    private void generarYMostrarFactura(int idSuplidor, int numeroFactura, ActionEvent event) {
        try {
            String reportPath = "src/main/resources/ReportePlanV2.jrxml";
            File reportFile = new File(reportPath);

            if (!reportFile.exists()) {
                System.err.println("No se encuentra el archivo del reporte: " + reportPath);
                mostrarAlerta("Error", "No se encuentra el archivo del reporte.", Alert.AlertType.ERROR);
                irALogin(event);
                return;
            }

            // Compilar el reporte desde el archivo
            JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);

            // Parámetros
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("id_suplidor", idSuplidor);

            // Conexión
            Connection con = conectar();

            // Llenar reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, con);

            // Crear carpeta Facturas si no existe
            String facturasDir = "Facturas";
            File dir = new File(facturasDir);
            if (!dir.exists()) {
                dir.mkdir();
            }

            // Guardar PDF en carpeta
            String pdfPath = facturasDir + "/Factura_Proveedor_" + idSuplidor + "_Nro_" + numeroFactura + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);
            System.out.println("Factura PDF guardada en: " + pdfPath);

            con.close();

            // Mostrar el visor de JasperReports (Swing) en un hilo separado
            // El visor tiene botones para: Guardar, Imprimir, Exportar, Zoom, etc.
            SwingUtilities.invokeLater(() -> {
                JasperViewer viewer = new JasperViewer(jasperPrint, false);
                viewer.setTitle("Factura - Plan " + planSeleccionado);
                viewer.setVisible(true);
            });

            // Preguntar si quiere ir al login o quedarse viendo la factura
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Factura generada");
            alert.setHeaderText("La factura se generó correctamente");
            alert.setContentText("¿Desea continuar al inicio de sesión?\n\nPuede cerrar la ventana de la factura cuando termine de imprimir o guardar.");

            ButtonType btnSi = new ButtonType("Sí, ir al login");
            ButtonType btnNo = new ButtonType("Ver factura primero", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnSi, btnNo);

            Optional<ButtonType> resultado = alert.showAndWait();
            if (resultado.isPresent() && resultado.get() == btnSi) {
                irALogin(event);
            }
            // Si elige "Ver factura primero", la ventana de la factura ya está abierta
            // y se queda en la pantalla actual

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al generar factura: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo generar la factura: " + e.getMessage(), Alert.AlertType.ERROR);
            irALogin(event);
        }
    }

    private void irALogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginGeneralV2.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar Sesión - Premier Services");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error al cargar LoginGeneralV2.fxml: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo cargar la pantalla de inicio de sesión.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private Connection conectar() {
        try {
            String url = "jdbc:sqlserver://26.228.126.202:1433;" +
                    "databaseName=PremierServicesV1;" +
                    "encrypt=true;trustServerCertificate=true";
            return DriverManager.getConnection(url, "wilenny", "1234");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}