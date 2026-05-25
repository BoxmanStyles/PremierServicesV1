package com.example.premierservices.Controllers.Proveedores;

import com.example.premierservices.Controllers.GlobalController.EditarPortafolioController;
import com.example.premierservices.Models.SolicitudReserva;
import com.example.premierservices.Servicio;
import com.example.premierservices.Utils.EmailSender;
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
import java.util.Optional;
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

    // ========== SOLICITUDES ==========
    private void cargarSolicitudesReales() {
        todasSolicitudes.clear();

        String sql = "SELECT r.id_reserva, c.nombre, c.apellido, s.nombre_servicio, " +
                "r.fecha_evento, r.estado, r.direccion_entrega " +
                "FROM tbl_reservas r " +
                "INNER JOIN tbl_clientes c ON r.id_cliente = c.id_cliente " +
                "INNER JOIN tbl_servicios s ON r.id_servicio = s.id_servicio " +
                "WHERE s.id_suplidor = ? " +
                "ORDER BY r.id_reserva DESC";

        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();

            System.out.println("=== SOLICITUDES ENCONTRADAS para proveedor " + idSuplidor + " ===");

            while (rs.next()) {
                int idReserva = rs.getInt("id_reserva");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String nombreCompleto = (nombre != null ? nombre : "Cliente") + (apellido != null ? " " + apellido : "");
                String nombreServicio = rs.getString("nombre_servicio");
                String fechaEvento = rs.getString("fecha_evento");
                String estadoStr = rs.getString("estado");
                String direccion = rs.getString("direccion_entrega");

                SolicitudReserva.Estado estado;
                try {
                    estado = SolicitudReserva.Estado.valueOf(estadoStr.toUpperCase());
                } catch (Exception e) {
                    if (estadoStr.equalsIgnoreCase("aceptada")) {
                        estado = SolicitudReserva.Estado.ACEPTADA;
                    } else if (estadoStr.equalsIgnoreCase("confirmada") || estadoStr.equalsIgnoreCase("confirado")) {
                        estado = SolicitudReserva.Estado.CONFIRMADO;
                    } else if (estadoStr.equalsIgnoreCase("completada")) {
                        estado = SolicitudReserva.Estado.COMPLETADO;
                    } else if (estadoStr.equalsIgnoreCase("cancelada")) {
                        estado = SolicitudReserva.Estado.CANCELADO;
                    } else {
                        estado = SolicitudReserva.Estado.PENDIENTE;
                    }
                }

                todasSolicitudes.add(new SolicitudReserva(idReserva, nombreCompleto, nombreServicio, fechaEvento, direccion, estado));
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

        if (colAccion != null) {
            colAccion.setCellFactory(col -> new TableCell<>() {
                private final Button btnAceptar  = new Button("✓ Aceptar");
                private final Button btnConfirmar = new Button("✔ Confirmar");
                private final Button btnCancelar = new Button("✗ Cancelar");
                private final Button btnCompletar = new Button("🏆 Completar");
                private final Button btnDetalle = new Button("👁 Ver más");

                {
                    btnAceptar.setStyle("-fx-background-color:#007cff; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
                    btnConfirmar.setStyle("-fx-background-color:#059669; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
                    btnCancelar.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
                    btnCompletar.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
                    btnDetalle.setStyle("-fx-background-color:#4A7FA9; -fx-text-fill:white; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10 5 10;");
                }

                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    SolicitudReserva s = getTableView().getItems().get(getIndex());
                    boolean pendiente  = s.getEstado() == SolicitudReserva.Estado.PENDIENTE;
                    boolean aceptada   = s.getEstado() == SolicitudReserva.Estado.ACEPTADA;
                    boolean confirmado = s.getEstado() == SolicitudReserva.Estado.CONFIRMADO;
                    boolean completado = s.getEstado() == SolicitudReserva.Estado.COMPLETADO;

                    btnAceptar.setVisible(pendiente);
                    btnConfirmar.setVisible(aceptada);
                    btnCancelar.setVisible(pendiente || aceptada);
                    btnCompletar.setVisible(confirmado && !completado);

                    HBox buttons = new HBox(5, btnAceptar, btnConfirmar, btnCancelar, btnCompletar, btnDetalle);
                    btnAceptar.setOnAction(e -> aceptarReserva(s));
                    btnConfirmar.setOnAction(e -> confirmarReserva(s));
                    btnCancelar.setOnAction(e -> cancelarReserva(s));
                    btnCompletar.setOnAction(e -> completarReserva(s));
                    btnDetalle.setOnAction(e -> mostrarDetalleReserva(s));
                    setGraphic(buttons); setText(null);
                }
            });
        }

        if (tablaSolicitudes != null) tablaSolicitudes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        if (comboEstadoFiltro != null) {
            comboEstadoFiltro.setItems(FXCollections.observableArrayList("Todos","PENDIENTE","ACEPTADA","CONFIRMADO","CANCELADO","COMPLETADO"));
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

    // ========== MÉTODOS PARA RESERVAS ==========

    private void aceptarReserva(SolicitudReserva solicitud) {
        String direccion = solicitud.getDireccion() == null || solicitud.getDireccion().isBlank()
                ? "(sin dirección especificada)" : solicitud.getDireccion();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Aceptar solicitud");
        confirm.setHeaderText("¿Aceptar realmente esta reserva?");
        confirm.setContentText(
                "Reserva #" + solicitud.getIdReserva() +
                "\n\n👤 Cliente: " + solicitud.getCliente() +
                "\n📦 Servicio: " + solicitud.getServicio() +
                "\n📅 Fecha del evento: " + solicitud.getFechaEvento() +
                "\n📍 Dirección de entrega: " + direccion +
                "\n\nSe enviará un correo al cliente notificando la aceptación.");
        estilizarAlerta(confirm);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection con = conectar();
                 PreparedStatement pst = con.prepareStatement("UPDATE tbl_reservas SET estado='aceptada' WHERE id_reserva=?")) {
                pst.setInt(1, solicitud.getIdReserva());
                pst.executeUpdate();

                enviarNotificacionesAceptacion(solicitud);

                cargarSolicitudesReales();
                mostrarDialogoSimulado("Éxito", "Solicitud aceptada.\nSe ha notificado al cliente.\n\nAhora puedes confirmar el pedido cuando estés listo.");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarDialogoSimulado("Error", "No se pudo aceptar la solicitud: " + e.getMessage());
            }
        }
    }

    private void confirmarReserva(SolicitudReserva solicitud) {
        String direccion = solicitud.getDireccion() == null || solicitud.getDireccion().isBlank()
                ? "(sin dirección especificada)" : solicitud.getDireccion();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar reserva");
        confirm.setHeaderText("¿Confirmar definitivamente la reserva #" + solicitud.getIdReserva() + "?");
        confirm.setContentText(
                "👤 Cliente: " + solicitud.getCliente() +
                "\n📦 Servicio: " + solicitud.getServicio() +
                "\n📅 Fecha del evento: " + solicitud.getFechaEvento() +
                "\n📍 Dirección de entrega: " + direccion +
                "\n\nSe enviará un correo final al cliente confirmando el pedido.");
        estilizarAlerta(confirm);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection con = conectar();
                 PreparedStatement pst = con.prepareStatement("UPDATE tbl_reservas SET estado='confirmada' WHERE id_reserva=?")) {
                pst.setInt(1, solicitud.getIdReserva());
                pst.executeUpdate();

                enviarNotificacionesConfirmacion(solicitud);

                cargarSolicitudesReales();
                mostrarDialogoSimulado("Éxito", "Reserva confirmada correctamente.\nSe ha notificado al cliente.");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarDialogoSimulado("Error", "No se pudo confirmar la reserva: " + e.getMessage());
            }
        }
    }

    private void cancelarReserva(SolicitudReserva solicitud) {
        String direccion = solicitud.getDireccion() == null || solicitud.getDireccion().isBlank()
                ? "(sin dirección especificada)" : solicitud.getDireccion();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar reserva");
        confirm.setHeaderText("¿Cancelar realmente la reserva #" + solicitud.getIdReserva() + "?");
        confirm.setContentText(
                "👤 Cliente: " + solicitud.getCliente() +
                "\n📦 Servicio: " + solicitud.getServicio() +
                "\n📅 Fecha del evento: " + solicitud.getFechaEvento() +
                "\n📍 Dirección de entrega: " + direccion +
                "\n\nSe enviará un correo al cliente notificando la cancelación.");
        estilizarAlerta(confirm);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection con = conectar();
                 PreparedStatement pst = con.prepareStatement("UPDATE tbl_reservas SET estado='cancelada' WHERE id_reserva=?")) {
                pst.setInt(1, solicitud.getIdReserva());
                pst.executeUpdate();

                enviarNotificacionesCancelacion(solicitud);

                cargarSolicitudesReales();
                mostrarDialogoSimulado("Éxito", "Reserva cancelada correctamente.\nSe ha notificado al cliente.");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarDialogoSimulado("Error", "No se pudo cancelar la reserva: " + e.getMessage());
            }
        }
    }

    private void completarReserva(SolicitudReserva solicitud) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Completar reserva");
        confirm.setHeaderText("¿Marcar la reserva #" + solicitud.getIdReserva() + " como completada?");
        confirm.setContentText("Cliente: " + solicitud.getCliente() + "\nServicio: " + solicitud.getServicio() +
                "\nFecha: " + solicitud.getFechaEvento() + "\n\nEl evento se marcará como completado.");
        estilizarAlerta(confirm);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection con = conectar();
                 PreparedStatement pst = con.prepareStatement("UPDATE tbl_reservas SET estado='completada' WHERE id_reserva=?")) {
                pst.setInt(1, solicitud.getIdReserva());
                pst.executeUpdate();

                cargarSolicitudesReales();
                mostrarDialogoSimulado("Éxito", "Reserva marcada como completada.\n¡Gracias por tu servicio!");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarDialogoSimulado("Error", "No se pudo completar la reserva: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Detalle: muestra info del cliente (no privada) + dirección + fecha
    // ─────────────────────────────────────────────────────────────────────
    private void mostrarDetalleReserva(SolicitudReserva solicitud) {
        // Cargar info pública del cliente desde la BD
        String emailCliente = "(no disponible)";
        String telefonoCliente = "(no disponible)";

        String sql = "SELECT c.email, c.telefono FROM tbl_reservas r " +
                "INNER JOIN tbl_clientes c ON r.id_cliente = c.id_cliente " +
                "WHERE r.id_reserva = ?";

        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, solicitud.getIdReserva());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                emailCliente = rs.getString("email") != null ? rs.getString("email") : emailCliente;
                telefonoCliente = rs.getString("telefono") != null ? rs.getString("telefono") : telefonoCliente;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String direccion = solicitud.getDireccion() == null || solicitud.getDireccion().isBlank()
                ? "(sin dirección especificada)" : solicitud.getDireccion();

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Detalle de Reserva #" + solicitud.getIdReserva());
        info.setHeaderText("Información del pedido");
        info.setContentText(
                "👤 INFORMACIÓN DEL CLIENTE\n" +
                "Nombre: " + solicitud.getCliente() + "\n" +
                "Email: " + emailCliente + "\n" +
                "Teléfono: " + telefonoCliente + "\n" +
                "\n📦 INFORMACIÓN DEL PEDIDO\n" +
                "Servicio: " + solicitud.getServicio() + "\n" +
                "Fecha del evento: " + solicitud.getFechaEvento() + "\n" +
                "Dirección de entrega: " + direccion + "\n" +
                "Estado actual: " + estadoTexto(solicitud.getEstado())
        );
        estilizarAlerta(info);
        info.showAndWait();
    }

    // ========== NOTIFICACIONES ==========

    private void crearNotificacionCliente(int idUsuario, String titulo, String contenido, String tipo, String urlAccion) {
        String sql = "INSERT INTO tbl_notificaciones (id_usuario, tipo, titulo, contenido, leida, url_accion, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, 0, ?, GETDATE())";

        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idUsuario);
            pst.setString(2, tipo);
            pst.setString(3, titulo);
            pst.setString(4, contenido);
            pst.setString(5, urlAccion);
            pst.executeUpdate();
            System.out.println("Notificación creada para usuario ID: " + idUsuario);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Datos de cliente+proveedor+servicio que necesitan los correos */
    private static class DatosReserva {
        int idUsuario = -1;
        String nombreCliente = "", emailCliente = "";
        String nombreServicio = "", fechaEvento = "", direccion = "";
        String nombreProveedor = "", telefonoProveedor = "No disponible", emailProveedor = "No disponible";
    }

    private DatosReserva cargarDatosReserva(int idReserva) {
        DatosReserva d = new DatosReserva();
        try (Connection con = conectar()) {
            String sql = "SELECT u.id_usuario, u.nombre AS nombre_cliente, c.email AS email_cliente, " +
                    "s.nombre_servicio, r.fecha_evento, r.direccion_entrega, " +
                    "p.nombre_empresa, p.telefono AS tel_prov, up.email AS email_prov " +
                    "FROM tbl_reservas r " +
                    "INNER JOIN tbl_clientes c ON r.id_cliente = c.id_cliente " +
                    "INNER JOIN tbl_usuarios u ON c.id_usuario = u.id_usuario " +
                    "INNER JOIN tbl_servicios s ON r.id_servicio = s.id_servicio " +
                    "INNER JOIN tbl_suplidores p ON s.id_suplidor = p.id_suplidor " +
                    "INNER JOIN tbl_usuarios up ON p.id_usuario = up.id_usuario " +
                    "WHERE r.id_reserva = ?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, idReserva);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    d.idUsuario         = rs.getInt("id_usuario");
                    d.nombreCliente     = nz(rs.getString("nombre_cliente"));
                    d.emailCliente      = nz(rs.getString("email_cliente"));
                    d.nombreServicio    = nz(rs.getString("nombre_servicio"));
                    d.fechaEvento       = nz(rs.getString("fecha_evento"));
                    d.direccion         = rs.getString("direccion_entrega") != null && !rs.getString("direccion_entrega").isBlank()
                                          ? rs.getString("direccion_entrega") : "(sin dirección especificada)";
                    d.nombreProveedor   = nz(rs.getString("nombre_empresa"));
                    d.telefonoProveedor = rs.getString("tel_prov") != null ? rs.getString("tel_prov") : "No disponible";
                    d.emailProveedor    = rs.getString("email_prov") != null ? rs.getString("email_prov") : "No disponible";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return d;
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private void enviarNotificacionesAceptacion(SolicitudReserva solicitud) {
        DatosReserva d = cargarDatosReserva(solicitud.getIdReserva());

        if (d.idUsuario != -1) {
            String titulo = "✅ Solicitud aceptada";
            String contenido = "Tu solicitud para \"" + d.nombreServicio + "\" fue aceptada. Será entregada en " +
                    d.direccion + " el " + d.fechaEvento + ".";
            crearNotificacionCliente(d.idUsuario, titulo, contenido, "reserva_aceptada", "/reservas/" + solicitud.getIdReserva());
        }
        if (!d.emailCliente.isEmpty()) {
            try {
                EmailSender.enviarCorreoAceptacion(
                        d.emailCliente, d.nombreCliente, d.nombreServicio, d.fechaEvento,
                        d.direccion, d.nombreProveedor, d.telefonoProveedor, d.emailProveedor);
            } catch (Exception e) {
                System.err.println("Error correo aceptación: " + e.getMessage());
            }
        }
    }

    private void enviarNotificacionesConfirmacion(SolicitudReserva solicitud) {
        DatosReserva d = cargarDatosReserva(solicitud.getIdReserva());

        if (d.idUsuario != -1) {
            String titulo = "🎉 Reserva confirmada";
            String contenido = "Tu pedido de \"" + d.nombreServicio + "\" fue confirmado. Se llevará a " +
                    d.direccion + " el " + d.fechaEvento + ".";
            crearNotificacionCliente(d.idUsuario, titulo, contenido, "reserva_confirmada", "/reservas/" + solicitud.getIdReserva());
        }
        if (!d.emailCliente.isEmpty()) {
            try {
                EmailSender.enviarCorreoConfirmacion(
                        d.emailCliente, d.nombreCliente, d.nombreServicio, d.fechaEvento,
                        d.direccion, d.nombreProveedor, d.telefonoProveedor, d.emailProveedor);
            } catch (Exception e) {
                System.err.println("Error correo confirmación: " + e.getMessage());
            }
        }
    }

    private void enviarNotificacionesCancelacion(SolicitudReserva solicitud) {
        try (Connection con = conectar()) {
            String sqlCliente = "SELECT id_cliente FROM tbl_reservas WHERE id_reserva = ?";
            int idCliente = -1;
            try (PreparedStatement pst = con.prepareStatement(sqlCliente)) {
                pst.setInt(1, solicitud.getIdReserva());
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    idCliente = rs.getInt("id_cliente");
                }
            }

            String sqlDatosCliente = "SELECT u.id_usuario, u.nombre, c.email " +
                    "FROM tbl_usuarios u INNER JOIN tbl_clientes c ON u.id_usuario = c.id_usuario " +
                    "WHERE c.id_cliente = ?";
            int idUsuario = -1;
            String nombreCliente = "";
            String emailCliente = "";
            try (PreparedStatement pst = con.prepareStatement(sqlDatosCliente)) {
                pst.setInt(1, idCliente);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    idUsuario = rs.getInt("id_usuario");
                    nombreCliente = rs.getString("nombre");
                    emailCliente = rs.getString("email");
                }
            }

            String sqlServicio = "SELECT s.nombre_servicio, r.fecha_evento " +
                    "FROM tbl_servicios s INNER JOIN tbl_reservas r ON r.id_servicio = s.id_servicio " +
                    "WHERE r.id_reserva = ?";
            String nombreServicio = "";
            String fechaEvento = "";
            try (PreparedStatement pst = con.prepareStatement(sqlServicio)) {
                pst.setInt(1, solicitud.getIdReserva());
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    nombreServicio = rs.getString("nombre_servicio");
                    fechaEvento = rs.getString("fecha_evento");
                }
            }

            if (idUsuario != -1) {
                String titulo = "❌ Reserva Cancelada";
                String contenido = "Lamentamos informarte que tu reserva para \"" + nombreServicio +
                        "\" (Fecha: " + fechaEvento + ") ha sido cancelada.";
                crearNotificacionCliente(idUsuario, titulo, contenido, "reserva_cancelada", "/reservas/" + solicitud.getIdReserva());
            }

            if (emailCliente != null && !emailCliente.isEmpty()) {
                try {
                    EmailSender.enviarCorreoCancelacion(emailCliente, nombreCliente, nombreServicio, fechaEvento);
                    System.out.println("✅ Correo de cancelación enviado a: " + emailCliente);
                } catch (Exception e) {
                    System.err.println("❌ Error al enviar correo de cancelación: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
        long a = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.ACEPTADA).count();
        long c = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.CONFIRMADO).count();
        long cp = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.COMPLETADO).count();
        long ca = todasSolicitudes.stream().filter(s -> s.getEstado() == SolicitudReserva.Estado.CANCELADO).count();
        // Pendiente + Aceptada se cuentan juntos en el contador "Pendiente" del dashboard
        if (statPendiente != null) statPendiente.setText(String.valueOf(p + a));
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
            case PENDIENTE -> "⏳ Pendiente";
            case ACEPTADA  -> "✅ Aceptada";
            case CONFIRMADO -> "🎉 Confirmado";
            case CANCELADO -> "❌ Cancelado";
            case COMPLETADO -> "🏆 Completado";
        };
    }
    private String estadoColor(SolicitudReserva.Estado e) {
        return switch (e) {
            case PENDIENTE -> "#fff7ed";
            case ACEPTADA  -> "#dbeafe";
            case CONFIRMADO -> "#ecfdf5";
            case CANCELADO -> "#fef2f2";
            case COMPLETADO -> "#eff6ff";
        };
    }
    private String estadoColorTexto(SolicitudReserva.Estado e) {
        return switch (e) {
            case PENDIENTE -> "#d97706";
            case ACEPTADA  -> "#007cff";
            case CONFIRMADO -> "#059669";
            case CANCELADO -> "#dc2626";
            case COMPLETADO -> "#2563eb";
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