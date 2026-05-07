package com.example.premierservices.Controllers;

import com.example.premierservices.Servicio;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;

public class EditarPortafolioController {

    @FXML private TextField txtIdProveedor;
    @FXML private TextField txtNombreServicio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbUbicacion;
    @FXML private TextField txtPrecio;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtRutaImagen;
    @FXML private ImageView imgVistaPrevia;
    @FXML private Label lblMensaje;
    @FXML private Button btnEliminar;

    private Servicio servicioEditando;
    private Runnable onGuardado;

    public void setServicioEditando(Servicio servicio) {
        this.servicioEditando = servicio;
        if (servicio != null) {
            txtIdProveedor.setText(String.valueOf(servicio.getIdSuplidor()));
            txtNombreServicio.setText(servicio.getNombreServicio());
            cmbCategoria.setValue(servicio.getCategoria());
            cmbUbicacion.setValue(servicio.getUbicacion());
            txtPrecio.setText(String.valueOf(servicio.getPrecio()));
            txtDescripcion.setText(servicio.getDescripcion());
            if (servicio.getRutaImagen() != null && !servicio.getRutaImagen().isEmpty()) {
                txtRutaImagen.setText(servicio.getRutaImagen());
                imgVistaPrevia.setImage(new Image(new File(servicio.getRutaImagen()).toURI().toString()));
            } else {
                txtRutaImagen.clear();
                imgVistaPrevia.setImage(null);
            }
            btnEliminar.setVisible(true);
        } else {
            txtIdProveedor.clear();
            txtNombreServicio.clear();
            cmbCategoria.setValue(null);
            cmbUbicacion.setValue(null);
            txtPrecio.clear();
            txtDescripcion.clear();
            txtRutaImagen.clear();
            imgVistaPrevia.setImage(null);
            lblMensaje.setText("");
            btnEliminar.setVisible(false);
        }
    }

    public void setOnGuardado(Runnable callback) {
        this.onGuardado = callback;
    }

    @FXML
    public void initialize() {
        cmbCategoria.getItems().addAll(
                "Fotografía", "Organización", "Catering", "Decoración",
                "Audio", "Música", "Banquetes", "Flores", "Video",
                "Mobiliario", "Entretenimiento", "Repostería"
        );
        cmbUbicacion.getItems().addAll(
                "Santo Domingo", "Santiago", "La Vega", "Puerto Plata",
                "San Pedro de Macorís", "La Romana", "Higüey"
        );
        btnEliminar.setVisible(false);
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            txtRutaImagen.setText(file.getAbsolutePath());
            imgVistaPrevia.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void limpiar() {
        if (servicioEditando != null) {
            // Recargar datos originales (deshacer cambios)
            setServicioEditando(servicioEditando);
        } else {
            // Limpiar campos de nuevo servicio
            txtIdProveedor.clear();
            txtNombreServicio.clear();
            cmbCategoria.setValue(null);
            cmbUbicacion.setValue(null);
            txtPrecio.clear();
            txtDescripcion.clear();
            txtRutaImagen.clear();
            imgVistaPrevia.setImage(null);
            lblMensaje.setText("");
        }
    }

    @FXML
    private void guardar() {
        if (!validarCampos()) return;

        int idSuplidor = Integer.parseInt(txtIdProveedor.getText().trim());
        String nombre = txtNombreServicio.getText().trim();
        String categoria = cmbCategoria.getValue();
        String ubicacion = cmbUbicacion.getValue();
        double precio = Double.parseDouble(txtPrecio.getText().trim());
        String descripcion = txtDescripcion.getText().trim();
        String rutaImagen = txtRutaImagen.getText().trim();

        try (Connection con = conectar()) {
            if (servicioEditando == null) {
                String sql = "INSERT INTO dbo.tbl_servicios (id_suplidor, nombre_servicio, categoria, descripcion, precio, estado, ruta_imagen) VALUES (?, ?, ?, ?, ?, 'activo', ?)";
                try (PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setInt(1, idSuplidor);
                    pst.setString(2, nombre);
                    pst.setString(3, categoria);
                    pst.setString(4, descripcion);
                    pst.setDouble(5, precio);
                    pst.setString(6, rutaImagen);
                    pst.executeUpdate();
                    mostrarMensaje("Servicio creado correctamente", true);
                }
            } else {
                String sql = "UPDATE dbo.tbl_servicios SET id_suplidor=?, nombre_servicio=?, categoria=?, descripcion=?, precio=?, ruta_imagen=? WHERE id_servicio=?";
                try (PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setInt(1, idSuplidor);
                    pst.setString(2, nombre);
                    pst.setString(3, categoria);
                    pst.setString(4, descripcion);
                    pst.setDouble(5, precio);
                    pst.setString(6, rutaImagen);
                    pst.setInt(7, servicioEditando.getIdServicio());
                    pst.executeUpdate();
                    mostrarMensaje("Servicio actualizado correctamente", true);
                }
            }
            if (onGuardado != null) onGuardado.run();
            cerrarVentana();
        } catch (SQLException e) {
            mostrarMensaje("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (servicioEditando == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar este servicio?");
        alert.setContentText("Servicio: " + servicioEditando.getNombreServicio());
        if (alert.showAndWait().get() == ButtonType.OK) {
            try (Connection con = conectar(); PreparedStatement pst = con.prepareStatement("UPDATE dbo.tbl_servicios SET estado = 'inactivo' WHERE id_servicio = ?")) {
                pst.setInt(1, servicioEditando.getIdServicio());
                pst.executeUpdate();
                if (onGuardado != null) onGuardado.run();
                cerrarVentana();
            } catch (SQLException e) {
                mostrarMensaje("Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private boolean validarCampos() {
        if (txtIdProveedor.getText().trim().isEmpty()) { mostrarMensaje("ID del proveedor requerido"); return false; }
        if (txtNombreServicio.getText().trim().isEmpty()) { mostrarMensaje("Nombre del servicio requerido"); return false; }
        if (cmbCategoria.getValue() == null) { mostrarMensaje("Seleccione categoría"); return false; }
        if (cmbUbicacion.getValue() == null) { mostrarMensaje("Seleccione ubicación"); return false; }
        if (txtPrecio.getText().trim().isEmpty()) { mostrarMensaje("Precio requerido"); return false; }
        try { Double.parseDouble(txtPrecio.getText().trim()); } catch (NumberFormatException e) { mostrarMensaje("Precio inválido"); return false; }
        return true;
    }

    private void mostrarMensaje(String msg) { mostrarMensaje(msg, false); }
    private void mostrarMensaje(String msg, boolean ok) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(ok ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void cerrarVentana() {
        ((Stage) txtIdProveedor.getScene().getWindow()).close();
    }

    private Connection conectar() {
        try {
            return DriverManager.getConnection("jdbc:sqlserver://26.228.126.202:1433;databaseName=PremierServicesV1;encrypt=true;trustServerCertificate=true", "wilenny", "1234");
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }
}