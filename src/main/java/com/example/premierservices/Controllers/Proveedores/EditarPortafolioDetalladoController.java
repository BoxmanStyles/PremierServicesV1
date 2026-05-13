package com.example.premierservices.Controllers.Proveedores;

import com.example.premierservices.Servicio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EditarPortafolioDetalladoController {

    @FXML private Label lblNombreServicio;
    @FXML private ImageView prevImagenPrincipal;
    @FXML private Label lblPlaceholderImagen;
    @FXML private TextField tfRutaImagenPrincipal;
    @FXML private ComboBox<String> comboTipoServicio;
    @FXML private TextField tfCosto;
    @FXML private GridPane gridAlbumEditor;
    @FXML private CheckBox chkLunes, chkMartes, chkMiercoles, chkJueves, chkViernes, chkSabado, chkDomingo;
    @FXML private Label lblMensaje;

    private Servicio servicio;
    private int idSuplidor;

    private final TextField[] tfAlbumRutas = new TextField[6];
    private final TextField[] tfAlbumDescs = new TextField[6];
    private final ImageView[] ivAlbumPrev = new ImageView[6];

    private static final String[] CATEGORIAS = {
            "Fotografía","Videografía","Animación","Diseño","Catering","Música","Decoración",
            "Audio","Banquetes","Flores","Mobiliario","Entretenimiento","Repostería","Organización"
    };

    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    @FXML
    public void initialize() {
        comboTipoServicio.setItems(FXCollections.observableArrayList(CATEGORIAS));
        construirGridAlbum();
    }

    public void setServicio(Servicio servicio, int idSuplidor) {
        this.servicio = servicio;
        this.idSuplidor = idSuplidor;
        System.out.println("Editor detallado - Servicio ID: " + servicio.getIdServicio() + ", Suplidor ID: " + idSuplidor);

        lblNombreServicio.setText(servicio.getNombreServicio());
        comboTipoServicio.setValue(servicio.getCategoria());
        tfCosto.setText(String.format("%.0f", servicio.getPrecio()));

        String ruta = servicio.getRutaImagen();
        if (ruta != null && !ruta.isEmpty()) {
            tfRutaImagenPrincipal.setText(ruta);
            mostrarPreviewPrincipal(ruta);
        }

        cargarAlbumDesdeBD();
        cargarDisponibilidadDesdeBD();
    }

    private void construirGridAlbum() {
        gridAlbumEditor.getChildren().clear();
        gridAlbumEditor.getColumnConstraints().clear();

        ColumnConstraints c0 = new ColumnConstraints(80);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(180);
        ColumnConstraints c3 = new ColumnConstraints(90);
        gridAlbumEditor.getColumnConstraints().addAll(c0, c1, c2, c3);

        gridAlbumEditor.add(lblH("Vista"), 0, 0);
        gridAlbumEditor.add(lblH("Ruta / URL"), 1, 0);
        gridAlbumEditor.add(lblH("Descripción"), 2, 0);
        gridAlbumEditor.add(new Label(""), 3, 0);

        for (int i = 0; i < 6; i++) {
            StackPane sp = new StackPane();
            sp.setPrefSize(75, 55);
            sp.setStyle("-fx-background-color:#e5e7eb; -fx-background-radius:8;");
            ImageView iv = new ImageView();
            iv.setFitWidth(75);
            iv.setFitHeight(55);
            iv.setPreserveRatio(true);
            Label ph = new Label("🖼");
            ph.setStyle("-fx-font-size:20px; -fx-text-fill:#9ca3af;");
            sp.getChildren().addAll(ph, iv);
            ivAlbumPrev[i] = iv;

            TextField tfR = new TextField();
            tfR.setPromptText("Ruta o URL de la foto " + (i + 1));
            tfR.setStyle("-fx-background-radius:7; -fx-border-radius:7; -fx-border-color:#e5e7eb; -fx-padding:5 10 5 10; -fx-font-size:12px;");
            GridPane.setMargin(tfR, new Insets(0, 4, 0, 4));
            tfAlbumRutas[i] = tfR;

            TextField tfD = new TextField();
            tfD.setPromptText("Descripción breve");
            tfD.setStyle("-fx-background-radius:7; -fx-border-radius:7; -fx-border-color:#e5e7eb; -fx-padding:5 10 5 10; -fx-font-size:12px;");
            tfAlbumDescs[i] = tfD;

            final int idx = i;
            Button btnExp = new Button("📁");
            btnExp.setTooltip(new Tooltip("Seleccionar archivo"));
            btnExp.setStyle("-fx-background-color:#e8f4fd; -fx-text-fill:#4A7FA9; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:12px; -fx-padding:5 8 5 8;");
            btnExp.setOnAction(e -> explorarArchivoAlbum(idx));

            Button btnPrev = new Button("👁");
            btnPrev.setTooltip(new Tooltip("Vista previa"));
            btnPrev.setStyle("-fx-background-color:#f3f4f6; -fx-text-fill:#374151; -fx-background-radius:7; -fx-cursor:hand; -fx-font-size:12px; -fx-padding:5 8 5 8;");
            btnPrev.setOnAction(e -> mostrarPreviewAlbum(idx));

            HBox btnBox = new HBox(4, btnExp, btnPrev);
            gridAlbumEditor.add(sp, 0, i+1);
            gridAlbumEditor.add(tfR, 1, i+1);
            gridAlbumEditor.add(tfD, 2, i+1);
            gridAlbumEditor.add(btnBox, 3, i+1);
            GridPane.setMargin(sp, new Insets(4));
            GridPane.setMargin(tfD, new Insets(0,4,0,4));
            GridPane.setMargin(btnBox, new Insets(4));
        }
    }

    private Label lblH(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#6b7280; -fx-padding:0 0 4 0;");
        return l;
    }

    private void cargarAlbumDesdeBD() {
        String sql = "SELECT ruta_imagen, descripcion_foto, orden FROM tbl_album_portafolio WHERE id_servicio = ? ORDER BY orden ASC";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, servicio.getIdServicio());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int orden = rs.getInt("orden") - 1;
                if (orden >= 0 && orden < 6) {
                    tfAlbumRutas[orden].setText(rs.getString("ruta_imagen") != null ? rs.getString("ruta_imagen") : "");
                    tfAlbumDescs[orden].setText(rs.getString("descripcion_foto") != null ? rs.getString("descripcion_foto") : "");
                    if (!tfAlbumRutas[orden].getText().isEmpty()) mostrarPreviewAlbum(orden);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarDisponibilidadDesdeBD() {
        // Resetear checkboxes
        CheckBox[] checkboxes = {chkLunes, chkMartes, chkMiercoles, chkJueves, chkViernes, chkSabado, chkDomingo};
        for (CheckBox cb : checkboxes) {
            if (cb != null) cb.setSelected(false);
        }

        String sql = "SELECT dia_semana FROM tbl_disponibilidad WHERE id_suplidores = ?";
        try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idSuplidor);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String dia = rs.getString("dia_semana");
                switch (dia.toLowerCase()) {
                    case "lunes": if (chkLunes != null) chkLunes.setSelected(true); break;
                    case "martes": if (chkMartes != null) chkMartes.setSelected(true); break;
                    case "miércoles": if (chkMiercoles != null) chkMiercoles.setSelected(true); break;
                    case "jueves": if (chkJueves != null) chkJueves.setSelected(true); break;
                    case "viernes": if (chkViernes != null) chkViernes.setSelected(true); break;
                    case "sábado": if (chkSabado != null) chkSabado.setSelected(true); break;
                    case "domingo": if (chkDomingo != null) chkDomingo.setSelected(true); break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSeleccionarImagenPrincipal() {
        File f = elegirArchivo();
        if (f != null) {
            tfRutaImagenPrincipal.setText(f.getAbsolutePath());
            mostrarPreviewPrincipal(f.getAbsolutePath());
        }
    }

    @FXML
    private void handlePreviewImagenPrincipal() {
        mostrarPreviewPrincipal(tfRutaImagenPrincipal.getText().trim());
    }

    private void mostrarPreviewPrincipal(String ruta) {
        if (ruta == null || ruta.isEmpty()) return;
        try {
            File f = new File(ruta);
            Image img = f.exists() ? new Image(f.toURI().toString(), 220, 140, true, true) : new Image(ruta, 220, 140, true, true);
            prevImagenPrincipal.setImage(img);
            lblPlaceholderImagen.setVisible(false);
        } catch (Exception ex) {
            lblMensaje.setText("⚠ No se pudo cargar la imagen.");
        }
    }

    private void explorarArchivoAlbum(int idx) {
        File f = elegirArchivo();
        if (f != null) {
            tfAlbumRutas[idx].setText(f.getAbsolutePath());
            mostrarPreviewAlbum(idx);
        }
    }

    private void mostrarPreviewAlbum(int idx) {
        String ruta = tfAlbumRutas[idx].getText().trim();
        if (ruta.isEmpty()) return;
        try {
            File f = new File(ruta);
            Image img = f.exists() ? new Image(f.toURI().toString(), 75, 55, true, true) : new Image(ruta, 75, 55, true, true);
            ivAlbumPrev[idx].setImage(img);
            ((StackPane) ivAlbumPrev[idx].getParent()).getChildren().get(0).setVisible(false);
        } catch (Exception ex) {
            // ignorar
        }
    }

    private File elegirArchivo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar imagen");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png","*.jpg","*.jpeg","*.gif","*.webp"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        return fc.showOpenDialog(lblNombreServicio.getScene().getWindow());
    }

    private void guardarDisponibilidad(Connection con) throws SQLException {
        // Eliminar disponibilidad existente para este suplidor
        try (PreparedStatement del = con.prepareStatement("DELETE FROM tbl_disponibilidad WHERE id_suplidores = ?")) {
            del.setInt(1, idSuplidor);
            del.executeUpdate();
        }

        // Insertar nuevos días seleccionados
        List<String> dias = obtenerDiasSeleccionados();
        if (!dias.isEmpty()) {
            String sqlDis = "INSERT INTO tbl_disponibilidad (id_suplidores, dia_semana, fecha, disponible) VALUES (?, ?, GETDATE(), 1)";
            try (PreparedStatement ins = con.prepareStatement(sqlDis)) {
                for (String dia : dias) {
                    ins.setInt(1, idSuplidor);
                    ins.setString(2, dia);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        }
    }

    private List<String> obtenerDiasSeleccionados() {
        List<String> dias = new ArrayList<>();
        if (chkLunes != null && chkLunes.isSelected()) dias.add("Lunes");
        if (chkMartes != null && chkMartes.isSelected()) dias.add("Martes");
        if (chkMiercoles != null && chkMiercoles.isSelected()) dias.add("Miércoles");
        if (chkJueves != null && chkJueves.isSelected()) dias.add("Jueves");
        if (chkViernes != null && chkViernes.isSelected()) dias.add("Viernes");
        if (chkSabado != null && chkSabado.isSelected()) dias.add("Sábado");
        if (chkDomingo != null && chkDomingo.isSelected()) dias.add("Domingo");
        return dias;
    }

    @FXML
    private void handleGuardar() {
        if (!validar()) return;

        try (Connection con = conectar()) {
            con.setAutoCommit(false);

            // 1. Actualizar servicio
            String sqlServ = "UPDATE tbl_servicios SET ruta_imagen=?, categoria=?, precio=? WHERE id_servicio=?";
            try (PreparedStatement pst = con.prepareStatement(sqlServ)) {
                pst.setString(1, tfRutaImagenPrincipal.getText().trim());
                pst.setString(2, comboTipoServicio.getValue());
                pst.setDouble(3, Double.parseDouble(tfCosto.getText().trim()));
                pst.setInt(4, servicio.getIdServicio());
                pst.executeUpdate();
            }

            // 2. Álbum: borrar y reinsertar
            try (PreparedStatement del = con.prepareStatement("DELETE FROM tbl_album_portafolio WHERE id_servicio=?")) {
                del.setInt(1, servicio.getIdServicio());
                del.executeUpdate();
            }

            String sqlAlbum = "INSERT INTO tbl_album_portafolio (id_servicio, ruta_imagen, descripcion_foto, orden) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ins = con.prepareStatement(sqlAlbum)) {
                for (int i = 0; i < 6; i++) {
                    String ruta = tfAlbumRutas[i].getText().trim();
                    if (!ruta.isEmpty()) {
                        ins.setInt(1, servicio.getIdServicio());
                        ins.setString(2, ruta);
                        ins.setString(3, tfAlbumDescs[i].getText().trim());
                        ins.setInt(4, i + 1);
                        ins.addBatch();
                    }
                }
                ins.executeBatch();
            }

            // 3. Guardar disponibilidad
            guardarDisponibilidad(con);

            con.commit();
            lblMensaje.setText("✅ Cambios guardados correctamente.");
            lblMensaje.setStyle("-fx-text-fill:#059669; -fx-font-size:12px;");

        } catch (Exception ex) {
            ex.printStackTrace();
            lblMensaje.setText("❌ Error al guardar: " + ex.getMessage());
            lblMensaje.setStyle("-fx-text-fill:#dc2626; -fx-font-size:12px;");
        }
    }

    private boolean validar() {
        if (comboTipoServicio.getValue() == null) {
            lblMensaje.setText("⚠ Selecciona un tipo de servicio.");
            lblMensaje.setStyle("-fx-text-fill:#d97706; -fx-font-size:12px;");
            return false;
        }
        if (tfCosto.getText().trim().isEmpty()) {
            lblMensaje.setText("⚠ Ingresa el costo del servicio.");
            lblMensaje.setStyle("-fx-text-fill:#d97706; -fx-font-size:12px;");
            return false;
        }
        try {
            Double.parseDouble(tfCosto.getText().trim());
        } catch (NumberFormatException e) {
            lblMensaje.setText("⚠ El costo debe ser un número válido.");
            lblMensaje.setStyle("-fx-text-fill:#d97706; -fx-font-size:12px;");
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancelar() {
        ((Stage) lblNombreServicio.getScene().getWindow()).close();
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:sqlserver://26.228.126.202:1433;databaseName=PremierServicesV1;encrypt=true;trustServerCertificate=true",
                "wilenny", "1234");
    }
}