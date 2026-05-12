package com.example.premierservices.Controllers.GlobalController;

import com.example.premierservices.Servicio;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;

public class EditarPortafolioController {

    @FXML private VBox proveedorIdBox;   // contenedor del campo ID (para ocultarlo)
    @FXML private TextField txtIdProveedor;
    @FXML private TextField txtNombreServicio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbUbicacion;
    @FXML private TextField txtPrecio;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtRutaImagen;
    @FXML private Label lblMensaje;

    private Servicio servicioEditando;
    private Runnable onGuardado;
    private boolean modoProveedor = false;
    private int idSuplidorFijo = -1;

    /**
     * Configura el editor para ser usado por un proveedor.
     * Oculta el campo del ID del proveedor y guarda el ID para usarlo al guardar.
     */
    public void setModoProveedor(int idSuplidor) {
        this.modoProveedor = true;
        this.idSuplidorFijo = idSuplidor;
        if (proveedorIdBox != null) {
            proveedorIdBox.setVisible(false);
            proveedorIdBox.setManaged(false);
        }
    }

    public void setServicioEditando(Servicio servicio) {
        this.servicioEditando = servicio;
        if (servicio != null && servicio.getIdServicio() != 0) {
            // Edición de servicio existente
            txtIdProveedor.setText(String.valueOf(servicio.getIdSuplidor()));
            txtNombreServicio.setText(servicio.getNombreServicio());
            cmbCategoria.setValue(servicio.getCategoria());
            cmbUbicacion.setValue(servicio.getUbicacion());
            txtPrecio.setText(String.valueOf(servicio.getPrecio()));
            txtDescripcion.setText(servicio.getDescripcion());
            if (servicio.getRutaImagen() != null && !servicio.getRutaImagen().isEmpty()) {
                txtRutaImagen.setText(servicio.getRutaImagen());
            } else {
                txtRutaImagen.clear();
            }
        } else {
            // Nuevo servicio: limpiamos campos y, si es modo proveedor, no mostramos el ID
            txtNombreServicio.clear();
            cmbCategoria.setValue(null);
            cmbUbicacion.setValue(null);
            txtPrecio.clear();
            txtDescripcion.clear();
            txtRutaImagen.clear();
            if (!modoProveedor) {
                txtIdProveedor.clear();
            }
        }
        lblMensaje.setText("");
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
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            txtRutaImagen.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void guardar() {
        if (!validarCampos()) return;

        int idSuplidor;
        if (modoProveedor) {
            idSuplidor = idSuplidorFijo;
        } else {
            try {
                idSuplidor = Integer.parseInt(txtIdProveedor.getText().trim());
            } catch (NumberFormatException e) {
                mostrarMensaje("ID de proveedor inválido");
                return;
            }
        }

        String nombre = txtNombreServicio.getText().trim();
        String categoria = cmbCategoria.getValue();
        String ubicacion = cmbUbicacion.getValue();
        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            mostrarMensaje("Precio inválido");
            return;
        }
        String descripcion = txtDescripcion.getText().trim();
        String rutaImagen = txtRutaImagen.getText().trim();

        try (Connection con = conectar()) {
            if (servicioEditando == null || servicioEditando.getIdServicio() == 0) {
                String sql = "INSERT INTO tbl_servicios (id_suplidor, nombre_servicio, categoria, descripcion, precio, estado, ruta_imagen) VALUES (?, ?, ?, ?, ?, 'activo', ?)";
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
                String sql = "UPDATE tbl_servicios SET id_suplidor=?, nombre_servicio=?, categoria=?, descripcion=?, precio=?, ruta_imagen=? WHERE id_servicio=?";
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
    private void cancelar() {
        cerrarVentana();
    }

    private boolean validarCampos() {
        if (!modoProveedor && txtIdProveedor.getText().trim().isEmpty()) {
            mostrarMensaje("ID del proveedor requerido");
            return false;
        }
        if (txtNombreServicio.getText().trim().isEmpty()) {
            mostrarMensaje("Nombre del servicio requerido");
            return false;
        }
        if (cmbCategoria.getValue() == null) {
            mostrarMensaje("Seleccione una categoría");
            return false;
        }
        if (cmbUbicacion.getValue() == null) {
            mostrarMensaje("Seleccione una ubicación");
            return false;
        }
        if (txtPrecio.getText().trim().isEmpty()) {
            mostrarMensaje("Ingrese el precio");
            return false;
        }
        return true;
    }

    private void mostrarMensaje(String msg) {
        mostrarMensaje(msg, false);
    }

    private void mostrarMensaje(String msg, boolean ok) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(ok ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtIdProveedor.getScene().getWindow();
        stage.close();
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