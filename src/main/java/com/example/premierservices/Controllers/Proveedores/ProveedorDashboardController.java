package com.example.premierservices.Controllers.Proveedores;

import com.example.premierservices.Controllers.GlobalController.EditarPortafolioController;
import com.example.premierservices.Models.SolicitudReserva;
import com.example.premierservices.Servicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProveedorDashboardController {

    @FXML private Label labelNombreProveedor;
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

    @FXML
    public void initialize() {
        configurarComboCategoria();
        configurarTablasSolicitudes();
    }

    public void setIdSuplidor(int id) {
        this.idSuplidor = id;
        System.out.println("=== PROVEEDOR DASHBOARD ===");
        System.out.println("ID Suplidor recibido: " + idSuplidor);
        cargarNombreEmpresa();
        cargarLogo();
        cargarServiciosReales();
        cargarSolicitudesReales();
    }

    private void cargarLogo() {
        try {
            File f = new File("IMG/Logo.png");
            if (f.exists()) { Logoimg.setImage(new Image(f.toURI().toString())); return; }
            URL url = getClass().getResource("/IMG/Logo.png");
            if (url != null) Logoimg.setImage(new Image(url.toString()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarNombreEmpresa() {
        String sql = "SELECT nombre_empresa FROM tbl_suplidores WHERE id_suplidor = ?";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();
            nombreEmpresa = rs.next() ? rs.getString("nombre_empresa") : "Proveedor #" + idSuplidor;
            labelNombreProveedor.setText(nombreEmpresa);
        } catch (SQLException e) { e.printStackTrace(); }
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
            while (rs.next()) {
                listaServicios.add(new Servicio(
                        rs.getInt("id_servicio"), idSuplidor, nombreEmpresa,
                        rs.getString("nombre_servicio"), rs.getString("categoria"),
                        rs.getString("ubicacion"), 0, 0, rs.getDouble("precio"),
                        rs.getString("descripcion"), "rookie", true, rs.getString("ruta_imagen")));
            }
            actualizarLabelPortafolio();
            renderizarTarjetas(listaServicios);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void renderizarTarjetas(List<Servicio> servicios) {
        flowPaneServicios.getChildren().clear();
        for (Servicio s : servicios) flowPaneServicios.getChildren().add(crearTarjeta(s));
        actualizarLabelPortafolio();
    }

    private VBox crearTarjeta(Servicio servicio) {
        VBox card = new VBox(0);
        card.setPrefWidth(240); card.setMaxWidth(260);
        card.setStyle("-fx-background-color:white; -fx-background-radius:14;");
        DropShadow sombra = new DropShadow(10, Color.web("rgba(0,0,0,0.1)"));
        sombra.setOffsetY(3);
        card.setEffect(sombra);

        StackPane bannerPane = new StackPane();
        bannerPane.setPrefHeight(140);
        String iconoCategoria = switch (servicio.getCategoria()) {
            case "Fotografía" -> "📷"; case "Catering" -> "🍽️";
            case "Música" -> "🎵"; case "Decoración" -> "🌸"; default -> "⭐";
        };
        String rutaImg = servicio.getRutaImagen();
        if (rutaImg != null && !rutaImg.isEmpty()) {
            File f = new File(rutaImg);
            if (f.exists()) {
                ImageView iv = new ImageView(new Image(f.toURI().toString(), 240, 140, true, true));
                iv.setFitWidth(240); iv.setFitHeight(140);
                bannerPane.getChildren().add(iv);
            } else {
                Label bannerLabel = new Label(iconoCategoria);
                bannerLabel.setStyle("-fx-font-size:52px;");
                bannerPane.getChildren().add(bannerLabel);
            }
        } else {
            Label bannerLabel = new Label(iconoCategoria);
            bannerLabel.setStyle("-fx-font-size:52px;");
            bannerPane.getChildren().add(bannerLabel);
        }
        String colorFondo = switch (servicio.getCategoria()) {
            case "Fotografía" -> "#dbeafe"; case "Catering" -> "#fef9c3";
            case "Música" -> "#ede9fe"; case "Decoración" -> "#fce7f3"; default -> "#f0f3f8";
        };
        bannerPane.setStyle("-fx-background-color:" + colorFondo + "; -fx-background-radius:14 14 0 0;");

        VBox info = new VBox(4);
        info.setPadding(new Insets(12, 14, 10, 14));
        Label lblCat = new Label(servicio.getCategoria());
        lblCat.setStyle("-fx-font-size:11px; -fx-text-fill:#4A7FA9; -fx-font-weight:bold;");
        Label lblNom = new Label(servicio.getNombreServicio());
        lblNom.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#0A1832;");
        lblNom.setWrapText(true);
        Label lblPrecio = new Label(String.format("$%.0f", servicio.getPrecio()));
        lblPrecio.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#27ae60;");
        info.getChildren().addAll(lblCat, lblNom, lblPrecio);

        HBox botones = new HBox(6);
        botones.setPadding(new Insets(6, 14, 12, 14));
        botones.setAlignment(Pos.CENTER);

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle("-fx-background-color:#4A7FA9; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
        btnEditar.setOnAction(e -> abrirEditorBasico(servicio));

        Button btnDetalle = new Button("🗂 Detalle");
        btnDetalle.setStyle("-fx-background-color:#0A1832; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
        btnDetalle.setOnAction(e -> abrirEditorDetallado(servicio));

        Button btnEliminar = new Button("🗑");
        btnEliminar.setStyle("-fx-background-color:#fef2f2; -fx-text-fill:#dc2626; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 8 5 8;");
        btnEliminar.setOnAction(e -> eliminarServicio(servicio));

        botones.getChildren().addAll(btnEditar, btnDetalle, btnEliminar);
        card.getChildren().addAll(bannerPane, info, botones);
        return card;
    }

    private void abrirEditorBasico(Servicio servicio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditarPortafolio.fxml"));
            Parent root = loader.load();
            EditarPortafolioController ctrl = loader.getController();
            ctrl.setServicio(servicio, idSuplidor);
            Stage stage = new Stage();
            stage.setTitle("Editar Portafolio — " + servicio.getNombreServicio());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarServiciosReales();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarDialogoSimulado("Editor básico", "Abriendo editor básico para:\n" + servicio.getNombreServicio());
        }
    }

    private void abrirEditorDetallado(Servicio servicio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditarPortafolioDetallado.fxml"));
            Parent root = loader.load();
            EditarPortafolioDetalladoController ctrl = loader.getController();
            ctrl.setServicio(servicio, idSuplidor);
            Stage stage = new Stage();
            stage.setTitle("Información Detallada — " + servicio.getNombreServicio());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarServiciosReales();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarDialogoSimulado("Error", "No se pudo abrir el editor detallado: " + e.getMessage());
        }
    }

    @FXML
    private void handleAgregarPortafolio() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Portafolio");
        dialog.setHeaderText("Ingresa la información básica del nuevo servicio");
        dialog.getDialogPane().setStyle("-fx-background-color:white; -fx-font-size:13px;");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));

        TextField tfNombre = new TextField(); tfNombre.setPromptText("Nombre del servicio");
        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.getItems().addAll("Fotografía","Videografía","Animación","Diseño","Catering",
                "Música","Decoración","Audio","Banquetes","Flores","Mobiliario","Entretenimiento","Repostería","Organización");
        cbCat.setPromptText("Categoría");
        TextField tfPrecio = new TextField(); tfPrecio.setPromptText("Precio base (ej: 15000)");
        TextArea taDesc = new TextArea(); taDesc.setPromptText("Descripción del servicio");
        taDesc.setPrefRowCount(3); taDesc.setWrapText(true);

        grid.add(new Label("Nombre:"), 0, 0); grid.add(tfNombre, 1, 0);
        grid.add(new Label("Categoría:"), 0, 1); grid.add(cbCat, 1, 1);
        grid.add(new Label("Precio:"), 0, 2); grid.add(tfPrecio, 1, 2);
        grid.add(new Label("Descripción:"), 0, 3); grid.add(taDesc, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String nombre = tfNombre.getText().trim();
            String cat = cbCat.getValue();
            String precioStr = tfPrecio.getText().trim();
            String desc = taDesc.getText().trim();
            if (nombre.isEmpty() || cat == null || precioStr.isEmpty()) {
                mostrarDialogoSimulado("Error", "Completa nombre, categoría y precio.");
                return;
            }
            try {
                double precio = Double.parseDouble(precioStr);
                String sql = "INSERT INTO tbl_servicios (id_suplidor, nombre_servicio, categoria, precio, descripcion, estado) " +
                        "VALUES (?, ?, ?, ?, ?, 'activo')";
                try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setInt(1, idSuplidor);
                    pst.setString(2, nombre); pst.setString(3, cat);
                    pst.setDouble(4, precio); pst.setString(5, desc);
                    pst.executeUpdate();
                    cargarServiciosReales();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    mostrarDialogoSimulado("Error SQL", ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                mostrarDialogoSimulado("Error", "El precio debe ser un número válido.");
            }
        }
    }

    private void eliminarServicio(Servicio servicio) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar servicio");
        confirm.setHeaderText("¿Eliminar \"" + servicio.getNombreServicio() + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        estilizarAlerta(confirm);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "UPDATE tbl_servicios SET estado='inactivo' WHERE id_servicio=?";
            try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, servicio.getIdServicio());
                pst.executeUpdate();
                cargarServiciosReales();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void configurarComboCategoria() {
        if (comboCategoriaFiltro != null) {
            comboCategoriaFiltro.setItems(FXCollections.observableArrayList(
                    "Todas","Fotografía","Videografía","Catering","Música","Decoración",
                    "Audio","Banquetes","Flores","Mobiliario","Entretenimiento","Repostería","Organización"));
            comboCategoriaFiltro.getSelectionModel().selectFirst();
        }
    }

    @FXML private void handleSearch() { aplicarFiltros(); }
    @FXML private void handleFiltroCategoria() { aplicarFiltros(); }

    private void aplicarFiltros() {
        String texto = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String cat = comboCategoriaFiltro != null ? comboCategoriaFiltro.getValue() : "Todas";
        List<Servicio> filtrados = listaServicios.stream().filter(s -> {
            boolean matchCat = "Todas".equals(cat) || cat == null || s.getCategoria().equals(cat);
            boolean matchTxt = texto.isEmpty() ||
                    s.getNombreServicio().toLowerCase().contains(texto) ||
                    s.getCategoria().toLowerCase().contains(texto);
            return matchCat && matchTxt;
        }).collect(Collectors.toList());
        renderizarTarjetas(filtrados);
    }

    private void actualizarLabelPortafolio() {
        if (labelTotalServicios != null) {
            labelTotalServicios.setText(listaServicios.size() + " servicio" + (listaServicios.size() != 1 ? "s" : "") + " publicados");
        }
    }

    // ========== SOLICITUDES CORREGIDAS ==========
    private void cargarSolicitudesReales() {
        todasSolicitudes.clear();

        String sql = "SELECT r.id_reserva, u.nombre, s.nombre_servicio, " +
                "r.fecha_evento, r.estado " +
                "FROM tbl_reservas r " +
                "INNER JOIN tbl_usuarios u ON r.id_cliente = u.id_usuario " +
                "INNER JOIN tbl_servicios s ON r.id_servicio = s.id_servicio " +
                "WHERE s.id_suplidor = ? " +
                "ORDER BY r.id_reserva DESC";

        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();

            System.out.println("=== SOLICITUDES ENCONTRADAS para proveedor " + idSuplidor + " ===");

            while (rs.next()) {
                int idReserva = rs.getInt("id_reserva");
                String nombreCliente = rs.getString("nombre");
                String nombreServicio = rs.getString("nombre_servicio");
                String fechaEvento = rs.getString("fecha_evento");
                String estadoStr = rs.getString("estado");

                System.out.println("Reserva #" + idReserva +
                        " - Cliente: " + nombreCliente +
                        " - Servicio: " + nombreServicio +
                        " - Estado: " + estadoStr);

                SolicitudReserva.Estado estado;
                try {
                    estado = SolicitudReserva.Estado.valueOf(estadoStr.toUpperCase());
                } catch (Exception e) {
                    if (estadoStr.equalsIgnoreCase("confirmada")) {
                        estado = SolicitudReserva.Estado.CONFIRMADO;
                    } else if (estadoStr.equalsIgnoreCase("completada")) {
                        estado = SolicitudReserva.Estado.COMPLETADO;
                    } else if (estadoStr.equalsIgnoreCase("cancelada")) {
                        estado = SolicitudReserva.Estado.CANCELADO;
                    } else {
                        estado = SolicitudReserva.Estado.PENDIENTE;
                    }
                }

                todasSolicitudes.add(new SolicitudReserva(
                        idReserva,
                        nombreCliente,
                        nombreServicio,
                        fechaEvento,
                        estado));
            }

            System.out.println("Total solicitudes cargadas: " + todasSolicitudes.size());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        actualizarStats();
        actualizarLabelSolicitudes();
    }

    private void configurarTablasSolicitudes() {
        if (colIdReserva != null) colIdReserva.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        if (colCliente != null) colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        if (colServicio != null) colServicio.setCellValueFactory(new PropertyValueFactory<>("servicio"));
        if (colFechaEvento != null) colFechaEvento.setCellValueFactory(new PropertyValueFactory<>("fechaEvento"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        if (colEstado != null) colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(SolicitudReserva.Estado estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); return; }
                Label badge = new Label(estadoTexto(estado));
                badge.setStyle("-fx-background-color:" + estadoColor(estado) + "; -fx-text-fill:" +
                        estadoColorTexto(estado) + "; -fx-background-radius:20; -fx-padding:3 10 3 10; " +
                        "-fx-font-size:11px; -fx-font-weight:bold;");
                setGraphic(badge); setText(null);
            }
        });

        if (colAccion != null) colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                SolicitudReserva s = getTableView().getItems().get(getIndex());
                boolean pendiente = s.getEstado() == SolicitudReserva.Estado.PENDIENTE;
                btn.setText(pendiente ? "✔ Confirmar" : "🔍 Ver detalle");
                btn.setStyle("-fx-background-color:" + (pendiente ? "#059669" : COLOR_AZUL) +
                        "; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
                btn.setOnAction(e -> handleAccionSolicitud(s));
                setGraphic(btn); setText(null);
            }
        });

        if (tablaSolicitudes != null) tablaSolicitudes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        if (comboEstadoFiltro != null) {
            comboEstadoFiltro.setItems(FXCollections.observableArrayList("Todos","PENDIENTE","CONFIRMADO","CANCELADO","COMPLETADO"));
            comboEstadoFiltro.getSelectionModel().selectFirst();
        }
        solicitudesFiltradas = new FilteredList<>(todasSolicitudes, p -> true);
        if (tablaSolicitudes != null) tablaSolicitudes.setItems(solicitudesFiltradas);
    }

    @FXML private void handleFiltroEstado() {
        String filtro = comboEstadoFiltro.getValue();
        solicitudesFiltradas.setPredicate(s -> filtro == null || filtro.equals("Todos") || s.getEstado().name().equals(filtro));
        actualizarLabelSolicitudes();
    }

    @FXML private void handleRefreshSolicitudes() { cargarSolicitudesReales(); }

    private void handleAccionSolicitud(SolicitudReserva solicitud) {
        if (solicitud.getEstado() == SolicitudReserva.Estado.PENDIENTE) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar solicitud");
            confirm.setHeaderText("¿Confirmar la reserva #" + solicitud.getIdReserva() + "?");
            confirm.setContentText("Cliente: " + solicitud.getCliente() + "\nServicio: " + solicitud.getServicio() +
                    "\nFecha: " + solicitud.getFechaEvento());
            estilizarAlerta(confirm);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try (Connection con = conectar();
                     PreparedStatement pst = con.prepareStatement("UPDATE tbl_reservas SET estado='confirmada' WHERE id_reserva=?")) {
                    pst.setInt(1, solicitud.getIdReserva());
                    pst.executeUpdate();
                    cargarSolicitudesReales();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        } else {
            mostrarDialogoSimulado("Detalle de Reserva",
                    "Cliente: " + solicitud.getCliente() + "\nServicio: " + solicitud.getServicio() +
                            "\nFecha: " + solicitud.getFechaEvento() + "\nEstado: " + estadoTexto(solicitud.getEstado()));
        }
    }

    @FXML private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión"); alert.setHeaderText("¿Deseas cerrar sesión?");
        alert.setContentText("Serás redirigido a la pantalla de inicio.");
        estilizarAlerta(alert);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/LoginGeneralV2.fxml"));
                Stage stage = (Stage) labelNombreProveedor.getScene().getWindow();
                stage.setScene(new Scene(root)); stage.setTitle("Iniciar Sesión");
                stage.centerOnScreen(); stage.show();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void actualizarStats() {
        long p = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.PENDIENTE).count();
        long c = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.CONFIRMADO).count();
        long cp = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.COMPLETADO).count();
        long ca = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.CANCELADO).count();
        if (statPendiente != null) statPendiente.setText(String.valueOf(p));
        if (statConfirmado != null) statConfirmado.setText(String.valueOf(c));
        if (statCompletado != null) statCompletado.setText(String.valueOf(cp));
        if (statCancelado != null) statCancelado.setText(String.valueOf(ca));
    }

    private void actualizarLabelSolicitudes() {
        if (labelTotalSolicitudes != null) {
            labelTotalSolicitudes.setText(solicitudesFiltradas.size() +
                    (solicitudesFiltradas.size() == 1 ? " solicitud" : " solicitudes"));
        }
    }

    private String estadoTexto(SolicitudReserva.Estado e) {
        return switch (e) {
            case PENDIENTE -> "⏳ Pendiente"; case CONFIRMADO -> "✅ Confirmado";
            case CANCELADO -> "❌ Cancelado"; case COMPLETADO -> "🏆 Completado";
        };
    }
    private String estadoColor(SolicitudReserva.Estado e) {
        return switch (e) {
            case PENDIENTE -> "#fff7ed"; case CONFIRMADO -> "#ecfdf5";
            case CANCELADO -> "#fef2f2"; case COMPLETADO -> "#eff6ff";
        };
    }
    private String estadoColorTexto(SolicitudReserva.Estado e) {
        return switch (e) {
            case PENDIENTE -> "#d97706"; case CONFIRMADO -> "#059669";
            case CANCELADO -> "#dc2626"; case COMPLETADO -> "#2563eb";
        };
    }

    private void estilizarAlerta(Alert alert) {
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-font-size:13px;");
    }

    private void mostrarDialogoSimulado(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(titulo); a.setContentText(mensaje);
        estilizarAlerta(a); a.showAndWait();
    }

    private Connection conectar() {
        try {
            return DriverManager.getConnection(
                    "jdbc:sqlserver://26.228.126.202:1433;databaseName=PremierServicesV1;encrypt=true;trustServerCertificate=true",
                    "wilenny", "1234");
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }
}