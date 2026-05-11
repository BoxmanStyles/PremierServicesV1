package com.example.premierservices.Controllers;

import com.example.premierservices.Models.SolicitudReserva;
import com.example.premierservices.Servicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProveedorDashboardController {

    @FXML private Label labelNombreProveedor;
    @FXML private Label labelStatus;
    @FXML private FlowPane flowPaneServicios;
    @FXML private Label labelTotalServicios;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboCategoriaFiltro;
    @FXML private TableView<SolicitudReserva> tablaSolicitudes;
    @FXML private TableColumn<SolicitudReserva, Integer> colIdReserva;
    @FXML private TableColumn<SolicitudReserva, String> colCliente;
    @FXML private TableColumn<SolicitudReserva, String> colServicio;
    @FXML private TableColumn<SolicitudReserva, String> colFechaEvento;
    @FXML private TableColumn<SolicitudReserva, SolicitudReserva.Estado> colEstado;
    @FXML private TableColumn<SolicitudReserva, Void> colAccion;
    @FXML private Label labelTotalSolicitudes;
    @FXML private Label statPendiente;
    @FXML private Label statConfirmado;
    @FXML private Label statCompletado;
    @FXML private Label statCancelado;
    @FXML private ComboBox<String> comboEstadoFiltro;
    @FXML private ImageView Logoimg;

    private int idSuplidor;
    private String nombreEmpresa;
    private final List<Servicio> listaServicios = new ArrayList<>();
    private final ObservableList<SolicitudReserva> todasSolicitudes = FXCollections.observableArrayList();
    private FilteredList<SolicitudReserva> solicitudesFiltradas;

    private static final String COLOR_AZUL = "#007cff";
    private static final String COLOR_ROJO = "#e74c3c";

    public void setIdSuplidor(int id) {
        this.idSuplidor = id;
        cargarNombreEmpresa();
        cargarLogo();
        cargarServiciosReales();
        cargarSolicitudesReales();
    }

    private void cargarLogo() {
        try {
            File fileLogo = new File("IMG/Logo.png");
            if (fileLogo.exists()) {
                Logoimg.setImage(new Image(fileLogo.toURI().toString()));
            } else {
                URL url = getClass().getResource("/IMG/Logo.png");
                if (url != null) {
                    Logoimg.setImage(new Image(url.toString()));
                } else {
                    System.err.println("Logo no encontrado en ninguna ruta.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarNombreEmpresa() {
        String sql = "SELECT nombre_empresa FROM tbl_suplidores WHERE id_suplidor = ?";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                nombreEmpresa = rs.getString("nombre_empresa");
                labelNombreProveedor.setText(nombreEmpresa);
            } else {
                labelNombreProveedor.setText("Proveedor #" + idSuplidor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            labelNombreProveedor.setText("Error al cargar nombre");
        }
    }

    private void cargarServiciosReales() {
        listaServicios.clear();
        String sql = "SELECT s.id_servicio, s.nombre_servicio, s.categoria, s.precio, " +
                "p.ubicacion, s.descripcion, s.ruta_imagen " +
                "FROM tbl_servicios s " +
                "INNER JOIN tbl_suplidores p ON s.id_suplidor = p.id_suplidor " +
                "WHERE s.id_suplidor = ? AND s.estado = 'activo'";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                Servicio servicio = new Servicio(
                        rs.getInt("id_servicio"),
                        idSuplidor,
                        nombreEmpresa,
                        rs.getString("nombre_servicio"),
                        rs.getString("categoria"),
                        rs.getString("ubicacion"),
                        0, 0,
                        rs.getDouble("precio"),
                        rs.getString("descripcion"),
                        "rookie",
                        true,
                        rs.getString("ruta_imagen")
                );
                listaServicios.add(servicio);
            }
            System.out.println("Cargados " + count + " servicios para proveedor " + idSuplidor);
            actualizarLabelPortafolio();
            renderizarTarjetas(listaServicios);
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus("Error al cargar servicios: " + e.getMessage());
        }
    }

    private void renderizarTarjetas(List<Servicio> servicios) {
        flowPaneServicios.getChildren().clear();
        for (Servicio s : servicios) {
            flowPaneServicios.getChildren().add(crearTarjeta(s));
        }
        actualizarLabelPortafolio();
    }

    private VBox crearTarjeta(Servicio servicio) {
        VBox card = new VBox(0);
        card.setPrefWidth(240);
        card.setMaxWidth(260);
        card.setStyle("-fx-background-color:white; -fx-background-radius:14;");
        DropShadow sombra = new DropShadow(10, Color.web("rgba(0,0,0,0.1)"));
        sombra.setOffsetY(3);
        card.setEffect(sombra);

        StackPane bannerPane = new StackPane();
        bannerPane.setPrefHeight(140);
        String iconoCategoria = switch (servicio.getCategoria()) {
            case "Fotografía" -> "📷";
            case "Catering" -> "🍽️";
            case "Música" -> "🎵";
            case "Decoración" -> "🌸";
            default -> "⭐";
        };
        Label bannerLabel = new Label(iconoCategoria);
        bannerLabel.setStyle("-fx-font-size:52px;");
        bannerPane.getChildren().add(bannerLabel);
        String colorFondo = switch (servicio.getCategoria()) {
            case "Fotografía" -> "#dbeafe";
            case "Catering" -> "#fef9c3";
            case "Música" -> "#ede9fe";
            case "Decoración" -> "#fce7f3";
            default -> "#f0f3f8";
        };
        bannerPane.setStyle("-fx-background-color:" + colorFondo + "; -fx-background-radius:14 14 0 0;");

        Label badgeCategoria = new Label(servicio.getCategoria());
        badgeCategoria.setStyle("-fx-background-color:rgba(255,255,255,0.85); -fx-text-fill:#0A1832; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:20; -fx-padding:3 10 3 10;");
        StackPane.setAlignment(badgeCategoria, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeCategoria, new Insets(10, 10, 0, 0));
        bannerPane.getChildren().add(badgeCategoria);

        VBox contenido = new VBox(6);
        contenido.setPadding(new Insets(14, 16, 16, 16));

        Label lblNombre = new Label(servicio.getNombreServicio());
        lblNombre.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#0A1832;");
        lblNombre.setWrapText(true);

        Label lblUbicacion = new Label("📍 " + servicio.getUbicacion());
        lblUbicacion.setStyle("-fx-font-size:11px; -fx-text-fill:#6b7280;");

        Label lblPrecio = new Label(String.format("Desde: $%.2f", servicio.getPrecio()));
        lblPrecio.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_AZUL + ";");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#e5e7eb;");
        VBox.setMargin(sep, new Insets(4, 0, 4, 0));

        HBox botonesBox = new HBox(8);
        botonesBox.setAlignment(Pos.CENTER);

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle("-fx-background-color:" + COLOR_AZUL + "; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:6 14 6 14;");
        btnEditar.setOnAction(e -> abrirEditorPortafolio(servicio));
        aplicarHover(btnEditar, COLOR_AZUL, "#0056cc");

        Button btnEliminar = new Button("🗑");
        btnEliminar.setStyle("-fx-background-color:" + COLOR_ROJO + "; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:6 10 6 10;");
        btnEliminar.setOnAction(e -> handleEliminarServicio(servicio));
        aplicarHover(btnEliminar, COLOR_ROJO, "#c0392b");

        HBox.setHgrow(btnEditar, Priority.ALWAYS);
        btnEditar.setMaxWidth(Double.MAX_VALUE);
        botonesBox.getChildren().addAll(btnEditar, btnEliminar);

        contenido.getChildren().addAll(lblNombre, lblUbicacion, lblPrecio, sep, botonesBox);

        card.setOnMouseEntered(e -> {
            sombra.setRadius(18);
            sombra.setOffsetY(6);
            card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-translate-y:-2;");
        });
        card.setOnMouseExited(e -> {
            sombra.setRadius(10);
            sombra.setOffsetY(3);
            card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-translate-y:0;");
        });

        card.getChildren().addAll(bannerPane, contenido);
        return card;
    }

    private void abrirEditorPortafolio(Servicio servicio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditarPortafolio.fxml"));
            Parent root = loader.load();
            EditarPortafolioController editorController = loader.getController();

            // Configurar modo proveedor con el ID actual
            editorController.setModoProveedor(idSuplidor);

            if (servicio != null && servicio.getIdServicio() != 0) {
                editorController.setServicioEditando(servicio);
            } else {
                editorController.setServicioEditando(null);
            }

            editorController.setOnGuardado(() -> {
                cargarServiciosReales();
                setStatus("Lista de servicios actualizada.");
            });

            Stage stage = new Stage();
            stage.setTitle(servicio == null ? "Nuevo Portafolio" : "Editar Portafolio");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            setStatus("Error al abrir el editor: " + e.getMessage());
        }
    }

    @FXML
    private void handleAgregarPortafolio() {
        abrirEditorPortafolio(null);
    }

    private void handleEliminarServicio(Servicio servicio) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar el servicio?");
        confirm.setContentText("«" + servicio.getNombreServicio() + "» será eliminado permanentemente.");
        estilizarAlerta(confirm);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "UPDATE tbl_servicios SET estado = 'inactivo' WHERE id_servicio = ? AND id_suplidor = ?";
            try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, servicio.getIdServicio());
                pst.setInt(2, idSuplidor);
                int affected = pst.executeUpdate();
                if (affected > 0) {
                    cargarServiciosReales();
                    setStatus("Servicio eliminado correctamente.");
                } else {
                    setStatus("No se pudo eliminar (no autorizado).");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                setStatus("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML private void handleSearch() { renderizarTarjetas(aplicarFiltroPortafolio()); }
    @FXML private void handleFiltroCategoria() { renderizarTarjetas(aplicarFiltroPortafolio()); }

    private List<Servicio> aplicarFiltroPortafolio() {
        String texto = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String categoria = comboCategoriaFiltro.getValue();
        return listaServicios.stream()
                .filter(s -> texto.isEmpty() || s.getNombreServicio().toLowerCase().contains(texto) || s.getCategoria().toLowerCase().contains(texto) || s.getUbicacion().toLowerCase().contains(texto))
                .filter(s -> categoria == null || categoria.equals("Todas") || s.getCategoria().equals(categoria))
                .collect(Collectors.toList());
    }

    private void configurarComboCategoria() {
        comboCategoriaFiltro.setItems(FXCollections.observableArrayList("Todas", "Fotografía", "Catering", "Música", "Decoración", "Iluminación", "Transporte", "Otro"));
    }

    private void actualizarLabelPortafolio() {
        labelTotalServicios.setText(listaServicios.size() + (listaServicios.size() == 1 ? " servicio publicado" : " servicios publicados"));
    }

    // ========== SOLICITUDES (sin cambios, funcionan) ==========
    private void cargarSolicitudesReales() {
        todasSolicitudes.clear();
        String sql = "SELECT r.id_reserva, c.nombre AS cliente, s.nombre_servicio AS servicio, r.fecha_evento, r.estado " +
                "FROM tbl_reservas r " +
                "INNER JOIN tbl_servicios s ON r.id_servicio = s.id_servicio " +
                "INNER JOIN tbl_clientes cl ON r.id_cliente = cl.id_cliente " +
                "INNER JOIN tbl_usuarios c ON cl.id_usuario = c.id_usuario " +
                "WHERE s.id_suplidor = ? ORDER BY r.fecha_evento DESC";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                SolicitudReserva sr = new SolicitudReserva(
                        rs.getInt("id_reserva"),
                        rs.getString("cliente"),
                        rs.getString("servicio"),
                        rs.getString("fecha_evento"),
                        convertirEstado(rs.getString("estado"))
                );
                todasSolicitudes.add(sr);
            }
            actualizarStats();
            actualizarLabelSolicitudes();
            tablaSolicitudes.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            setStatus("Error al cargar solicitudes: " + e.getMessage());
        }
    }

    private SolicitudReserva.Estado convertirEstado(String estadoBD) {
        if (estadoBD == null) return SolicitudReserva.Estado.PENDIENTE;
        switch (estadoBD.toLowerCase()) {
            case "confirmado": return SolicitudReserva.Estado.CONFIRMADO;
            case "cancelado": return SolicitudReserva.Estado.CANCELADO;
            case "completado": return SolicitudReserva.Estado.COMPLETADO;
            default: return SolicitudReserva.Estado.PENDIENTE;
        }
    }

    private void configurarTablasSolicitudes() {
        colIdReserva.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colServicio.setCellValueFactory(new PropertyValueFactory<>("servicio"));
        colFechaEvento.setCellValueFactory(new PropertyValueFactory<>("fechaEvento"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(SolicitudReserva.Estado estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(estadoTexto(estado));
                badge.setStyle("-fx-background-color:" + estadoColor(estado) + "; -fx-text-fill:" + estadoColorTexto(estado) + "; -fx-background-radius:20; -fx-padding:3 10 3 10; -fx-font-size:11px; -fx-font-weight:bold;");
                setGraphic(badge);
                setText(null);
            }
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                SolicitudReserva s = getTableView().getItems().get(getIndex());
                boolean pendiente = s.getEstado() == SolicitudReserva.Estado.PENDIENTE;
                btn.setText(pendiente ? "✔ Confirmar" : "🔍 Ver detalle");
                btn.setStyle("-fx-background-color:" + (pendiente ? "#059669" : COLOR_AZUL) + "; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
                btn.setOnAction(e -> handleAccionSolicitud(s));
                setGraphic(btn);
                setText(null);
            }
        });
        tablaSolicitudes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        comboEstadoFiltro.setItems(FXCollections.observableArrayList("Todos", "PENDIENTE", "CONFIRMADO", "CANCELADO", "COMPLETADO"));
        comboEstadoFiltro.getSelectionModel().selectFirst();
        solicitudesFiltradas = new FilteredList<>(todasSolicitudes, p -> true);
        tablaSolicitudes.setItems(solicitudesFiltradas);
    }

    @FXML private void handleFiltroEstado() {
        String filtro = comboEstadoFiltro.getValue();
        solicitudesFiltradas.setPredicate(s -> filtro == null || filtro.equals("Todos") || s.getEstado().name().equals(filtro));
        actualizarLabelSolicitudes();
    }

    @FXML private void handleRefreshSolicitudes() {
        cargarSolicitudesReales();
        setStatus("Solicitudes actualizadas.");
    }

    private void handleAccionSolicitud(SolicitudReserva solicitud) {
        if (solicitud.getEstado() == SolicitudReserva.Estado.PENDIENTE) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar solicitud");
            confirm.setHeaderText("¿Confirmar la reserva #" + solicitud.getIdReserva() + "?");
            confirm.setContentText("Cliente: " + solicitud.getCliente() + "\nServicio: " + solicitud.getServicio() + "\nFecha: " + solicitud.getFechaEvento());
            estilizarAlerta(confirm);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                String sql = "UPDATE tbl_reservas SET estado = 'confirmado' WHERE id_reserva = ?";
                try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setInt(1, solicitud.getIdReserva());
                    pst.executeUpdate();
                    cargarSolicitudesReales();
                    setStatus("Solicitud confirmada.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    setStatus("Error al confirmar: " + e.getMessage());
                }
            }
        } else {
            mostrarDialogoSimulado("Detalle de Reserva", "Cliente: " + solicitud.getCliente() +
                    "\nServicio: " + solicitud.getServicio() +
                    "\nFecha: " + solicitud.getFechaEvento() +
                    "\nEstado: " + estadoTexto(solicitud.getEstado()));
        }
    }

    // ========== GENERAL ==========
    @FXML private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText("¿Deseas cerrar sesión?");
        alert.setContentText("Serás redirigido a la pantalla de inicio.");
        estilizarAlerta(alert);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/LoginGeneralV2.fxml"));
                Stage stage = (Stage) labelNombreProveedor.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Iniciar Sesión");
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void mostrarDialogoSimulado(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        estilizarAlerta(alert);
        alert.showAndWait();
    }

    private void estilizarAlerta(Alert alert) {
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-font-size:13px;");
    }

    private void aplicarHover(Button btn, String colorNormal, String colorHover) {
        String baseStyle = btn.getStyle();
        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle.replace(colorNormal, colorHover)));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    private void setStatus(String msg) {
        if (labelStatus != null) labelStatus.setText("✦ " + msg);
        System.out.println("[INFO] " + msg);
    }

    private void actualizarStats() {
        long pendiente  = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.PENDIENTE).count();
        long confirmado = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.CONFIRMADO).count();
        long completado = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.COMPLETADO).count();
        long cancelado  = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.CANCELADO).count();
        statPendiente.setText(String.valueOf(pendiente));
        statConfirmado.setText(String.valueOf(confirmado));
        statCompletado.setText(String.valueOf(completado));
        statCancelado.setText(String.valueOf(cancelado));
    }

    private void actualizarLabelSolicitudes() {
        labelTotalSolicitudes.setText(solicitudesFiltradas.size() + (solicitudesFiltradas.size() == 1 ? " solicitud" : " solicitudes"));
    }

    private String estadoTexto(SolicitudReserva.Estado e) {
        switch (e) {
            case PENDIENTE: return "⏳ Pendiente";
            case CONFIRMADO: return "✅ Confirmado";
            case CANCELADO: return "❌ Cancelado";
            case COMPLETADO: return "🏆 Completado";
            default: return "";
        }
    }

    private String estadoColor(SolicitudReserva.Estado e) {
        switch (e) {
            case PENDIENTE: return "#fff7ed";
            case CONFIRMADO: return "#ecfdf5";
            case CANCELADO: return "#fef2f2";
            case COMPLETADO: return "#eff6ff";
            default: return "#f0f0f0";
        }
    }

    private String estadoColorTexto(SolicitudReserva.Estado e) {
        switch (e) {
            case PENDIENTE: return "#d97706";
            case CONFIRMADO: return "#059669";
            case CANCELADO: return "#dc2626";
            case COMPLETADO: return "#2563eb";
            default: return "#000";
        }
    }

    @FXML
    public void initialize() {
        configurarComboCategoria();
        configurarTablasSolicitudes();
        // La carga real de datos se hará en setIdSuplidor()
    }

    private Connection conectar() {
        try {
            String url = "jdbc:sqlserver://26.228.126.202:1433;databaseName=PremierServicesV1;encrypt=true;trustServerCertificate=true";
            return DriverManager.getConnection(url, "wilenny", "1234");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}