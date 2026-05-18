package com.example.premierservices.Controllers.Clientes;

import com.example.premierservices.Servicio;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaginaPrincipalSesionController {

    @FXML private Pane PanelBuscador;
    @FXML private Pane PanelBandeja;
    @FXML private ImageView imagenPerfil;
    @FXML private FlowPane flowServicios;
    @FXML private TextField txtBuscadorPrincipal;
    @FXML private Button btnBuscarPrincipal;
    @FXML private ImageView Logoimg;
    @FXML private Button BotonDeFiltros;
    @FXML private Label lblNombreCliente;
    @FXML private AnchorPane bandejaContent;

    @FXML private Pane overlay;
    @FXML private Pane panelDetalle;
    @FXML private ImageView detImagen;
    @FXML private Label detIcono;
    @FXML private Label detTipoServicio;
    @FXML private Label detTitulo;
    @FXML private Label detEmpresa;
    @FXML private Label detCalificacion;
    @FXML private Label detUbicacion;
    @FXML private Label detCosto;
    @FXML private TextArea detDescripcion;
    @FXML private VBox vboxResenas;
    @FXML private HBox hboxEstrellas;
    @FXML private TextArea txtNuevaResena;
    @FXML private Button btnPublicarResena;
    @FXML private GridPane gridAlbum;
    @FXML private VBox vboxDisponibilidad;

    // ===== PANEL DE CONTACTO =====
    @FXML private Pane panelContacto;
    @FXML private Label contactoTelefono;
    @FXML private Label contactoCorreo;
    @FXML private Button btnCopiarTelefono;
    @FXML private Button btnCopiarCorreo;
    @FXML private Button btnCerrarContacto;

    private List<Servicio> todosServicios;
    private String categoriaActiva = null;
    private int idCliente;
    private String nombreCliente;
    private String rutaFotoPerfil;
    private Servicio servicioActual;
    private int calificacionSeleccionada = 5;

    private static final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    @FXML
    public void initialize() {
        File fileLogo = new File("IMG/Logo.png");
        if (fileLogo.exists()) Logoimg.setImage(new Image(fileLogo.toURI().toString()));
        btnBuscarPrincipal.setOnAction(e -> realizarBusqueda());
        txtBuscadorPrincipal.setOnAction(e -> realizarBusqueda());
        cargarServicios();
        categoriaActiva = null;
        aplicarFiltros();
        BotonDeFiltros.setVisible(false);
        construirEstrellas();
    }

    public void setDatosCliente(int id, String nombre, String rutaFoto) {
        this.idCliente = id;
        this.nombreCliente = nombre;
        this.rutaFotoPerfil = rutaFoto;
        if (lblNombreCliente != null) lblNombreCliente.setText(nombreCliente);
        cargarFotoPerfil();
        cargarSolicitudes();

        try (Connection con = conectar();
             PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM tbl_clientes WHERE id_cliente = ?")) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                String sqlInsert = "INSERT INTO tbl_clientes (id_cliente, nombre, email, contrasena, tipo_cliente) VALUES (?, ?, ?, ?, 'personal')";
                try (PreparedStatement pst2 = con.prepareStatement(sqlInsert)) {
                    pst2.setInt(1, id);
                    pst2.setString(2, nombre);
                    pst2.setString(3, "cliente" + id + "@premierservices.do");
                    pst2.setString(4, "password");
                    pst2.executeUpdate();
                    System.out.println("Cliente #" + id + " insertado en tbl_clientes");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void abrirPanelDetalle(Servicio servicio) {
        this.servicioActual = servicio;
        System.out.println("=== ABRIENDO PANEL DETALLE ===");
        System.out.println("Servicio ID: " + servicio.getIdServicio());
        System.out.println("Suplidor ID: " + servicio.getIdSuplidor());
        System.out.println("Nombre: " + servicio.getNombreServicio());

        cargarInfoPrincipal(servicio);
        cargarResenas(servicio.getIdSuplidor());
        cargarAlbumFotos(servicio.getIdServicio());
        cargarDisponibilidad(servicio.getIdSuplidor());

        overlay.setVisible(true);
        panelDetalle.setVisible(true);
        panelDetalle.toFront();
    }

    @FXML
    public void cerrarPanelDetalle() {
        panelDetalle.setVisible(false);
        panelContacto.setVisible(false);
        overlay.setVisible(false);
        servicioActual = null;
    }

    // ===== CONTACTO =====
    @FXML
    private void handleContactar() {
        if (servicioActual == null) {
            mostrarAlerta("Error", "No hay un servicio seleccionado.");
            return;
        }

        System.out.println("=== ABRIENDO PANEL DE CONTACTO ===");
        System.out.println("Proveedor: " + servicioActual.getNombreSuplidor());
        System.out.println("ID Suplidor: " + servicioActual.getIdSuplidor());

        cargarDatosContacto(servicioActual.getIdSuplidor());

        panelContacto.setVisible(true);
        panelContacto.toFront();

        if (overlay != null) {
            overlay.setVisible(true);
        }
    }

    @FXML
    private void cerrarPanelContacto() {
        panelContacto.setVisible(false);
        if (overlay != null && !panelDetalle.isVisible()) {
            overlay.setVisible(false);
        }
    }

    @FXML
    private void copiarTelefono() {
        String telefono = contactoTelefono.getText();
        if (telefono != null && !telefono.equals("No disponible") && !telefono.equals("Error al cargar")) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(telefono);
            clipboard.setContent(content);
            mostrarAlertaRapida("📞 Teléfono copiado", "El número ha sido copiado al portapapeles.");
        } else {
            mostrarAlertaRapida("No disponible", "No hay número de teléfono registrado para este proveedor.");
        }
    }

    @FXML
    private void copiarCorreo() {
        String correo = contactoCorreo.getText();
        if (correo != null && !correo.equals("No disponible") && !correo.equals("Error al cargar")) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(correo);
            clipboard.setContent(content);
            mostrarAlertaRapida("✉️ Correo copiado", "El correo ha sido copiado al portapapeles.");
        } else {
            mostrarAlertaRapida("No disponible", "No hay correo electrónico registrado para este proveedor.");
        }
    }

    private void cargarDatosContacto(int idSuplidor) {
        String sql = "SELECT p.telefono, u.email " +
                "FROM tbl_suplidores p " +
                "INNER JOIN tbl_usuarios u ON p.id_usuario = u.id_usuario " +
                "WHERE p.id_suplidor = ?";

        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String telefono = rs.getString("telefono");
                String email = rs.getString("email");

                contactoTelefono.setText(telefono != null && !telefono.isEmpty() ? telefono : "No disponible");
                contactoCorreo.setText(email != null && !email.isEmpty() ? email : "No disponible");

                System.out.println("Datos de contacto cargados:");
                System.out.println("  Teléfono: " + contactoTelefono.getText());
                System.out.println("  Correo: " + contactoCorreo.getText());
            } else {
                contactoTelefono.setText("No disponible");
                contactoCorreo.setText("No disponible");
                System.out.println("No se encontró el proveedor con ID: " + idSuplidor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            contactoTelefono.setText("Error al cargar");
            contactoCorreo.setText("Error al cargar");
            mostrarAlerta("Error", "No se pudieron cargar los datos de contacto: " + e.getMessage());
        }
    }

    private void mostrarAlertaRapida(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-font-size: 13px;");

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(1.5), e -> alert.close())
        );
        timeline.play();
        alert.show();
    }

    private void cargarInfoPrincipal(Servicio s) {
        detTipoServicio.setText(s.getCategoria().toUpperCase());
        detTitulo.setText(s.getNombreServicio());
        detEmpresa.setText(s.getNombreSuplidor());
        detCalificacion.setText(String.format("%.1f (%d reseñas)", s.getCalificacion(), s.getTotalResenas()));
        detUbicacion.setText(s.getUbicacion());
        detCosto.setText(String.format("$%.0f", s.getPrecio()));
        detDescripcion.setText(s.getDescripcion());

        String ruta = s.getRutaImagen();
        if (ruta != null && !ruta.isEmpty()) {
            File f = new File(ruta);
            if (f.exists()) {
                detImagen.setImage(new Image(f.toURI().toString()));
                detImagen.setVisible(true);
                detIcono.setVisible(false);
                return;
            }
        }
        detImagen.setImage(null);
        detImagen.setVisible(false);
        detIcono.setVisible(true);
        detIcono.setText(obtenerIconoCategoria(s.getCategoria()));
    }

    private void cargarResenas(int idSuplidor) {
        vboxResenas.getChildren().clear();
        System.out.println("=== CARGANDO RESEÑAS PARA SUPILIDOR: " + idSuplidor + " ===");

        String sql = "SELECT r.comentario, r.puntuacion, c.nombre, r.fecha " +
                "FROM tbl_reseñas r " +
                "INNER JOIN tbl_clientes c ON r.id_cliente = c.id_cliente " +
                "WHERE r.id_suplidor = ? " +
                "ORDER BY r.fecha DESC";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                String autor = rs.getString("nombre");
                int calificacion = rs.getInt("puntuacion");
                String comentario = rs.getString("comentario");
                String fecha = rs.getString("fecha");
                System.out.println("Reseña #" + count + ": " + autor + " - " + calificacion + " estrellas - " + comentario);
                vboxResenas.getChildren().add(crearTarjetaResena(autor, calificacion, comentario, fecha));
            }
            if (count == 0) {
                System.out.println("No hay reseñas para este proveedor");
                Label vacio = new Label("Aún no hay reseñas para este proveedor. ¡Sé el primero!");
                vacio.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13px; -fx-padding:20 0 0 0;");
                vboxResenas.getChildren().add(vacio);
            }
            System.out.println("Total reseñas cargadas en UI: " + vboxResenas.getChildren().size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox crearTarjetaResena(String autor, int calificacion, String comentario, String fecha) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color:#f7f9fc; -fx-background-radius:10; -fx-padding:12 16 12 16;");
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblAutor = new Label("👤 " + autor);
        lblAutor.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:#0A1832;");
        Label lblFecha = new Label(fecha != null ? fecha : "");
        lblFecha.setStyle("-fx-font-size:11px; -fx-text-fill:#9ca3af;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(lblAutor, spacer, lblFecha);
        StringBuilder estrellas = new StringBuilder();
        for (int i = 1; i <= 5; i++) estrellas.append(i <= calificacion ? "★" : "☆");
        Label lblEstrellas = new Label(estrellas.toString());
        lblEstrellas.setStyle("-fx-text-fill:#f59e0b; -fx-font-size:15px;");
        Label lblComentario = new Label(comentario);
        lblComentario.setWrapText(true);
        lblComentario.setStyle("-fx-font-size:13px; -fx-text-fill:#374151;");
        card.getChildren().addAll(header, lblEstrellas, lblComentario);
        return card;
    }

    private void cargarAlbumFotos(int idServicio) {
        gridAlbum.getChildren().clear();
        gridAlbum.getColumnConstraints().clear();
        for (int c = 0; c < 2; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(340);
            cc.setHgrow(Priority.ALWAYS);
            gridAlbum.getColumnConstraints().add(cc);
        }
        String sql = "SELECT ruta_imagen, descripcion_foto FROM tbl_album_portafolio WHERE id_servicio = ? ORDER BY orden ASC";
        List<String[]> fotos = new ArrayList<>();
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idServicio);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                fotos.add(new String[]{rs.getString("ruta_imagen"), rs.getString("descripcion_foto")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        for (int i = 0; i < 6; i++) {
            int col = i % 2;
            int row = i / 2;
            VBox celda = crearCeldaFoto(i < fotos.size() ? fotos.get(i) : null);
            gridAlbum.add(celda, col, row);
        }
    }

    private VBox crearCeldaFoto(String[] foto) {
        VBox cell = new VBox(6);
        cell.setAlignment(Pos.CENTER);
        cell.setPrefWidth(330);
        cell.setStyle("-fx-background-color:#f0f3f8; -fx-background-radius:12; -fx-padding:8;");
        StackPane imgContainer = new StackPane();
        imgContainer.setPrefHeight(160);
        imgContainer.setStyle("-fx-background-color:#e5e7eb; -fx-background-radius:10;");
        if (foto != null && foto[0] != null && !foto[0].isEmpty()) {
            File f = new File(foto[0]);
            try {
                Image img = f.exists() ? new Image(f.toURI().toString(), 330, 160, true, true) : new Image(foto[0], 330, 160, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(330);
                iv.setFitHeight(160);
                iv.setPreserveRatio(true);
                imgContainer.getChildren().add(iv);
            } catch (Exception ex) { imgContainer.getChildren().add(fotoPlaceholder()); }
        } else {
            imgContainer.getChildren().add(fotoPlaceholder());
        }
        Label desc = new Label(foto != null && foto[1] != null ? foto[1] : "Sin descripción");
        desc.setStyle("-fx-font-size:12px; -fx-text-fill:#6b7280; -fx-wrap-text:true;");
        desc.setWrapText(true);
        desc.setMaxWidth(320);
        cell.getChildren().addAll(imgContainer, desc);
        return cell;
    }

    private Label fotoPlaceholder() {
        Label lbl = new Label("🖼");
        lbl.setStyle("-fx-font-size:36px; -fx-text-fill:#9ca3af;");
        return lbl;
    }

    private void cargarDisponibilidad(int idSuplidor) {
        vboxDisponibilidad.getChildren().clear();
        LocalDate hoy = LocalDate.now();
        LocalDate[] proximosDias = new LocalDate[7];
        for (int i = 0; i < 7; i++) proximosDias[i] = hoy.plusDays(i);
        Map<LocalDate, Boolean> disponibilidadMap = new HashMap<>();
        String sql = "SELECT fecha, disponible FROM tbl_disponibilidad WHERE id_suplidores = ? AND fecha >= ? ORDER BY fecha ASC";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            pst.setDate(2, Date.valueOf(hoy));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                boolean disponible = rs.getBoolean("disponible");
                disponibilidadMap.put(fecha, disponible);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error al cargar disponibilidad: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill:#dc2626; -fx-font-size:13px;");
            vboxDisponibilidad.getChildren().add(errorLabel);
            return;
        }
        if (disponibilidadMap.isEmpty()) {
            Label mensaje = new Label("El proveedor no ha establecido fechas de disponibilidad aún.");
            mensaje.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13px;");
            vboxDisponibilidad.getChildren().add(mensaje);
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (LocalDate fecha : proximosDias) {
            Boolean disponible = disponibilidadMap.get(fecha);
            boolean estaDisponible = disponible != null && disponible;
            HBox fila = new HBox(16);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(10, 16, 10, 16));
            fila.setStyle("-fx-background-radius:10; -fx-background-color:" + (estaDisponible ? "#ecfdf5" : "#fef2f2") + ";");
            Label lblFecha = new Label(fecha.format(formatter));
            lblFecha.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#0A1832; -fx-min-width:120;");
            Label lblEstado = new Label(estaDisponible ? "✅ Disponible" : "❌ No disponible");
            lblEstado.setStyle("-fx-font-size:13px; -fx-text-fill:" + (estaDisponible ? "#059669" : "#dc2626") + ";");
            fila.getChildren().addAll(lblFecha, lblEstado);
            vboxDisponibilidad.getChildren().add(fila);
        }
    }

    private void construirEstrellas() {
        if (hboxEstrellas == null) return;
        while (hboxEstrellas.getChildren().size() > 1) hboxEstrellas.getChildren().remove(1);
        calificacionSeleccionada = 5;
        for (int i = 1; i <= 5; i++) {
            final int valor = i;
            Label estrella = new Label("★");
            estrella.setStyle("-fx-font-size:24px; -fx-text-fill:#f59e0b; -fx-cursor:hand;");
            estrella.setOnMouseClicked(e -> {
                calificacionSeleccionada = valor;
                actualizarEstrellas(valor);
            });
            hboxEstrellas.getChildren().add(estrella);
        }
    }

    private void actualizarEstrellas(int valor) {
        var hijos = hboxEstrellas.getChildren();
        for (int i = 1; i < hijos.size(); i++) {
            Label l = (Label) hijos.get(i);
            l.setStyle("-fx-font-size:24px; -fx-cursor:hand; -fx-text-fill:" + (i <= valor ? "#f59e0b" : "#d1d5db") + ";");
        }
    }

    @FXML
    private void handlePublicarResena() {
        if (servicioActual == null) {
            mostrarAlerta("Error", "No hay un servicio seleccionado.");
            return;
        }

        String comentario = txtNuevaResena.getText().trim();
        if (comentario.isEmpty()) {
            mostrarAlerta("Campo vacío", "Por favor escribe un comentario antes de publicar.");
            return;
        }

        System.out.println("=== PUBLICANDO RESEÑA ===");
        System.out.println("ID Cliente: " + idCliente);
        System.out.println("ID Suplidor: " + servicioActual.getIdSuplidor());
        System.out.println("Calificación: " + calificacionSeleccionada);
        System.out.println("Comentario: " + comentario);

        try (Connection con = conectar()) {
            con.setAutoCommit(false);

            String sqlInsert = "INSERT INTO tbl_reseñas (id_cliente, id_suplidor, puntuacion, comentario, fecha) VALUES (?, ?, ?, ?, GETDATE())";
            try (PreparedStatement pst = con.prepareStatement(sqlInsert)) {
                pst.setInt(1, idCliente);
                pst.setInt(2, servicioActual.getIdSuplidor());
                pst.setInt(3, calificacionSeleccionada);
                pst.setString(4, comentario);
                int filas = pst.executeUpdate();
                System.out.println("Reseña insertada, filas: " + filas);
            }

            String sqlProm = "UPDATE tbl_suplidores SET calificacion_promedio = (SELECT AVG(CAST(puntuacion AS DECIMAL(3,1))) FROM tbl_reseñas WHERE id_suplidor = ?) WHERE id_suplidor = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlProm)) {
                pst.setInt(1, servicioActual.getIdSuplidor());
                pst.setInt(2, servicioActual.getIdSuplidor());
                pst.executeUpdate();
            }

            con.commit();

            txtNuevaResena.clear();
            construirEstrellas();

            System.out.println("Recargando reseñas para suplidor: " + servicioActual.getIdSuplidor());
            cargarResenas(servicioActual.getIdSuplidor());

            refrescarServicioActual();
            cargarServicios();
            aplicarFiltros();

            mostrarAlerta("Éxito", "Tu reseña ha sido publicada correctamente.");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar la reseña: " + e.getMessage());
        }
    }

    private void refrescarServicioActual() {
        String sql = "SELECT calificacion_promedio, (SELECT COUNT(*) FROM tbl_reseñas WHERE id_suplidor = ?) AS total_resenas FROM tbl_suplidores WHERE id_suplidor = ?";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, servicioActual.getIdSuplidor());
            pst.setInt(2, servicioActual.getIdSuplidor());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                double promedio = rs.getDouble("calificacion_promedio");
                int total = rs.getInt("total_resenas");
                servicioActual.setCalificacion(promedio);
                servicioActual.setTotalResenas(total);
                detCalificacion.setText(String.format("%.1f (%d reseñas)", promedio, total));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleEnviarSolicitud() {
        if (servicioActual == null) return;

        if (servicioActual.getIdServicio() == 0) {
            mostrarAlerta("Error", "El servicio no es válido. Intenta de nuevo.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva solicitud");
        dialog.setHeaderText("Solicitar servicio: " + servicioActual.getNombreServicio());

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("dd/MM/yyyy");
        datePicker.setValue(LocalDate.now().plusDays(7));
        VBox content = new VBox(10, new Label("Selecciona la fecha del evento:"), datePicker);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            LocalDate fecha = datePicker.getValue();
            if (fecha == null) {
                mostrarAlerta("Fecha requerida", "Selecciona una fecha para el evento.");
                return;
            }

            System.out.println("=== INSERTANDO RESERVA ===");
            System.out.println("ID Cliente: " + idCliente);
            System.out.println("ID Servicio: " + servicioActual.getIdServicio());
            System.out.println("ID Suplidor del servicio: " + servicioActual.getIdSuplidor());
            System.out.println("Fecha: " + fecha);

            String checkSql = "SELECT id_servicio FROM tbl_servicios WHERE id_servicio = ?";
            try (Connection con = conectar(); PreparedStatement checkSt = con.prepareStatement(checkSql)) {
                checkSt.setInt(1, servicioActual.getIdServicio());
                ResultSet rs = checkSt.executeQuery();
                if (!rs.next()) {
                    mostrarAlerta("Error", "El servicio no existe en la base de datos.");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error verificando el servicio: " + e.getMessage());
                return;
            }

            String sql = "INSERT INTO tbl_reservas (id_cliente, id_servicio, fecha_evento, estado, total) VALUES (?, ?, ?, 'pendiente', 0)";
            try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, idCliente);
                pst.setInt(2, servicioActual.getIdServicio());
                pst.setDate(3, Date.valueOf(fecha));
                int filas = pst.executeUpdate();
                System.out.println("Filas insertadas: " + filas);

                if (filas > 0) {
                    mostrarAlerta("Solicitud enviada", "Tu solicitud ha sido registrada. El proveedor te responderá pronto.");
                    cargarSolicitudes();
                } else {
                    mostrarAlerta("Error", "No se pudo enviar la solicitud.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudo enviar la solicitud: " + e.getMessage());
            }
        }
    }

    private VBox crearTarjetaServicio(Servicio servicio) {
        VBox card = new VBox(15);
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        card.setPadding(new Insets(0));

        DropShadow shadowBase = new DropShadow(5, Color.rgb(0, 0, 0, 0.1));
        card.setEffect(shadowBase);

        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
            DropShadow shadowHover = new DropShadow(20, Color.rgb(0, 0, 0, 0.2));
            card.setEffect(shadowHover);
            card.setCursor(javafx.scene.Cursor.HAND);
        });

        card.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            card.setEffect(shadowBase);
        });

        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(200);
        imagePane.setStyle("-fx-background-color: linear-gradient(to right, #003566, #669bbc);");
        String rutaImg = servicio.getRutaImagen();
        if (rutaImg != null && !rutaImg.isEmpty()) {
            File imgFile = new File(rutaImg);
            if (imgFile.exists()) {
                ImageView imgView = new ImageView(imgFile.toURI().toString());
                imgView.setFitHeight(200);
                imgView.setFitWidth(350);
                imgView.setPreserveRatio(true);
                imagePane.getChildren().add(imgView);
            } else { imagePane.getChildren().add(makeIconLabel(servicio)); }
        } else { imagePane.getChildren().add(makeIconLabel(servicio)); }

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        Label categoria = new Label(servicio.getCategoria().toUpperCase());
        categoria.setStyle("-fx-text-fill: #667eea; -fx-font-size: 12; -fx-font-weight: bold;");
        Label nombreServicio = new Label(servicio.getNombreServicio());
        nombreServicio.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombreServicio.setStyle("-fx-text-fill: #2c3e50;");
        Label nombreProveedor = new Label(servicio.getNombreSuplidor());
        nombreProveedor.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");
        VBox leftHeader = new VBox(5, categoria, nombreServicio, nombreProveedor);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox rating = new HBox(5);
        rating.setAlignment(Pos.CENTER_RIGHT);
        Label ratingText = new Label(String.format("⭐ %.1f (%d)", servicio.getCalificacion(), servicio.getTotalResenas()));
        ratingText.setStyle("-fx-text-fill: #555; -fx-font-size: 13;");
        rating.getChildren().add(ratingText);
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(leftHeader, spacer, rating);
        HBox ubicacion = new HBox(5, new Label("📍"), styledLabel(servicio.getUbicacion(), "-fx-text-fill: #7f8c8d; -fx-font-size: 13;"));
        ubicacion.setAlignment(Pos.CENTER_LEFT);
        Label descripcion = new Label(servicio.getDescripcion());
        descripcion.setWrapText(true);
        descripcion.setMaxWidth(310);
        descripcion.setMaxHeight(45);
        descripcion.setStyle("-fx-text-fill: #555; -fx-font-size: 14;");
        Label precio = new Label(String.format("$%.0f", servicio.getPrecio()));
        precio.setFont(Font.font("System", FontWeight.BOLD, 20));
        precio.setStyle("-fx-text-fill: #27ae60;");
        Label desde = new Label("desde");
        desde.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13;");
        VBox priceBox = new VBox(2, precio, desde);
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnVer = new Button("Ver portafolio");
        btnVer.setStyle("-fx-background-color: #003566; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnVer.setOnAction(e -> abrirPanelDetalle(servicio));
        footer.getChildren().add(btnVer);
        content.getChildren().addAll(header, ubicacion, descripcion, new Separator(), priceBox, footer);
        card.getChildren().addAll(imagePane, content);
        card.setOnMouseClicked(e -> abrirPanelDetalle(servicio));
        return card;
    }

    private Label makeIconLabel(Servicio s) { Label l = new Label(obtenerIconoCategoria(s.getCategoria())); l.setFont(Font.font(48)); return l; }
    private Label styledLabel(String text, String style) { Label l = new Label(text); l.setStyle(style); return l; }

    private String obtenerIconoCategoria(String cat) {
        if (cat == null) return "⭐";
        return switch (cat.toLowerCase()) {
            case "fotografía", "fotografia" -> "📷"; case "videografía", "videografia", "video" -> "🎬";
            case "catering", "banquetes" -> "🍽️"; case "música", "musica" -> "🎵";
            case "decoración", "decoracion", "flores" -> "🌸"; case "audio" -> "🎧";
            case "animación", "animacion", "entretenimiento" -> "🎭"; case "organización", "organizacion" -> "📋";
            case "repostería", "reposteria" -> "🎂"; case "mobiliario" -> "🪑";
            default -> "⭐";
        };
    }

    private void cargarServicios() {
        todosServicios = new ArrayList<>();
        String sql = "SELECT s.id_servicio, s.id_suplidor, p.nombre_empresa, s.nombre_servicio, " +
                "s.categoria, p.ubicacion, COALESCE(p.calificacion_promedio,0) AS calificacion_promedio, " +
                "COALESCE((SELECT COUNT(*) FROM tbl_reseñas r WHERE r.id_suplidor = p.id_suplidor),0) AS total_resenas, " +
                "s.precio, s.descripcion, s.ruta_imagen " +
                "FROM tbl_servicios s INNER JOIN tbl_suplidores p ON s.id_suplidor = p.id_suplidor WHERE s.estado = 'activo'";
        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                todosServicios.add(new Servicio(rs.getInt("id_servicio"), rs.getInt("id_suplidor"),
                        rs.getString("nombre_empresa"), rs.getString("nombre_servicio"), rs.getString("categoria"),
                        rs.getString("ubicacion"), rs.getDouble("calificacion_promedio"), rs.getInt("total_resenas"),
                        rs.getDouble("precio"), rs.getString("descripcion"), "rookie", true, rs.getString("ruta_imagen")));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error al cargar servicios", e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarServicios(List<Servicio> servicios) {
        flowServicios.getChildren().clear();
        if (servicios == null || servicios.isEmpty()) {
            VBox noResults = new VBox(10);
            noResults.setAlignment(Pos.CENTER);
            noResults.setPadding(new Insets(60));
            Label lbl = new Label("No hay servicios disponibles");
            lbl.setFont(Font.font("System", FontWeight.BOLD, 24));
            lbl.setStyle("-fx-text-fill: #7f8c8d;");
            noResults.getChildren().add(lbl);
            flowServicios.getChildren().add(noResults);
            return;
        }
        for (Servicio s : servicios) {
            flowServicios.getChildren().add(crearTarjetaServicio(s));
        }
    }

    private void aplicarFiltros() {
        if (todosServicios == null) return;
        String texto = normalizarTexto(txtBuscadorPrincipal.getText());
        List<Servicio> filtrados = todosServicios.stream().filter(s -> {
            boolean cat = categoriaActiva == null || normalizarTexto(s.getCategoria()).contains(normalizarTexto(categoriaActiva));
            boolean txt = texto.isEmpty() || normalizarTexto(s.getNombreServicio()).contains(texto) ||
                    normalizarTexto(s.getNombreSuplidor()).contains(texto) || normalizarTexto(s.getDescripcion()).contains(texto);
            return cat && txt;
        }).collect(Collectors.toList());
        mostrarServicios(filtrados);
    }

    private void realizarBusqueda() { aplicarFiltros(); }

    private void cargarSolicitudes() {
        if (bandejaContent == null) return;
        bandejaContent.getChildren().clear();
        ListView<String> listView = new ListView<>();
        listView.setStyle("-fx-background-color: transparent; -fx-font-size: 13px;");
        String sql = "SELECT r.id_reserva, s.nombre_servicio, r.fecha_evento, r.estado " +
                "FROM tbl_reservas r INNER JOIN tbl_servicios s ON r.id_servicio = s.id_servicio " +
                "WHERE r.id_cliente = ? ORDER BY r.fecha_evento DESC";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idCliente);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String estado = rs.getString("estado");
                String icono = switch (estado.toLowerCase()) {
                    case "pendiente" -> "⏳"; case "confirmada" -> "✅";
                    case "completado" -> "🏆"; case "cancelado" -> "❌";
                    default -> "📋";
                };
                listView.getItems().add(String.format("%s %s - %s (%s)", icono, rs.getString("nombre_servicio"), rs.getString("fecha_evento"), estado));
            }
            if (listView.getItems().isEmpty()) listView.getItems().add("📭 No tienes solicitudes aún.");
        } catch (SQLException e) {
            e.printStackTrace();
            listView.getItems().add("❌ Error al cargar las solicitudes.");
        }
        bandejaContent.getChildren().add(listView);
        AnchorPane.setTopAnchor(listView, 10.0); AnchorPane.setLeftAnchor(listView, 10.0);
        AnchorPane.setRightAnchor(listView, 10.0); AnchorPane.setBottomAnchor(listView, 10.0);
    }

    @FXML protected void Buscador(ActionEvent e) { PanelBuscador.setVisible(true); txtBuscadorPrincipal.requestFocus(); }
    @FXML protected void SalirBuscar(ActionEvent e) { PanelBuscador.setVisible(false); txtBuscadorPrincipal.clear(); categoriaActiva = null; aplicarFiltros(); BotonDeFiltros.setVisible(false); }
    @FXML protected void BandejaAbrir(ActionEvent e) { PanelBandeja.setVisible(true); cargarSolicitudes(); }
    @FXML public void SalirBandeja(ActionEvent e) { PanelBandeja.setVisible(false); }
    @FXML protected void filtrarPorCategoria(ActionEvent event) { categoriaActiva = ((Button) event.getSource()).getText(); aplicarFiltros(); BotonDeFiltros.setVisible(true); }
    @FXML public void AbirYCerrarFiltros(ActionEvent event) { categoriaActiva = null; txtBuscadorPrincipal.clear(); aplicarFiltros(); BotonDeFiltros.setVisible(false); }

    @FXML private void cerrarSesion(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Cerrar sesión"); a.setHeaderText("¿Deseas cerrar sesión?"); a.setContentText("Serás redirigido a la pantalla de inicio.");
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/LoginGeneralV2.fxml"));
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root)); stage.setTitle("Iniciar Sesión");
                stage.centerOnScreen(); stage.show();
            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String n = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").toLowerCase();
    }

    private void cargarFotoPerfil() {
        if (rutaFotoPerfil != null && !rutaFotoPerfil.isEmpty()) {
            File file = new File(rutaFotoPerfil);
            if (file.exists()) { imagenPerfil.setImage(new Image(file.toURI().toString())); return; }
        }
        File def = new File("IMG/Perfil 1 sin fondo.png");
        if (def.exists()) imagenPerfil.setImage(new Image(def.toURI().toString()));
    }

    private Connection conectar() {
        try {
            return DriverManager.getConnection("jdbc:sqlserver://26.228.126.202:1433;databaseName=PremierServicesV1;encrypt=true;trustServerCertificate=true", "wilenny", "1234");
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }
}