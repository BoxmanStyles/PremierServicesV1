package com.example.premierservices.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AdminEditorPortafolioController {

    // Elementos del formulario
    @FXML private ComboBox<String> cmbProveedor;
    @FXML private Label lblProveedorInfo;
    @FXML private TextField txtIdPortafolio;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cmbServicio;
    @FXML private TextField txtUrlImagen;
    @FXML private Spinner<Integer> spinOrden;
    @FXML private CheckBox chkDestacado;
    @FXML private Label lblMensaje;

    // Botones
    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnLimpiar;

    // Lista de proyectos
    @FXML private VBox vboxProyectos;
    @FXML private Label lblTotalProyectos;

    @FXML
    public void initialize() {
        // Configurar el Spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        spinOrden.setValueFactory(valueFactory);

        // Configurar opciones del ComboBox de proveedores (datos de ejemplo)
        cmbProveedor.getItems().addAll("Proveedor 1", "Proveedor 2", "Proveedor 3");

        // Configurar opciones del ComboBox de servicios (datos de ejemplo)
        cmbServicio.getItems().addAll("Fotografía", "Videografía", "Animación", "Diseño Gráfico");

        // Configurar mensaje inicial
        lblMensaje.setText("");
        lblTotalProyectos.setText("0 proyectos");

        System.out.println("AdminEditorPortafolioController inicializado correctamente");
    }

    @FXML
    void onProveedorSeleccionado(ActionEvent event) {
        System.out.println("Proveedor seleccionado: " + cmbProveedor.getValue());
    }

    @FXML
    void onRecargarProveedores(ActionEvent event) {
        System.out.println("Recargar proveedores");
    }

    @FXML
    void onSubirImagenClick(ActionEvent event) {
        System.out.println("Subir imagen");
    }

    @FXML
    void onAgregarClick(ActionEvent event) {
        System.out.println("Agregar proyecto");
    }

    @FXML
    void onActualizarClick(ActionEvent event) {
        System.out.println("Actualizar proyecto");
    }

    @FXML
    void onLimpiarClick(ActionEvent event) {
        System.out.println("Limpiar formulario");
        limpiarFormulario();
    }

    @FXML
    void onVolverClick(ActionEvent event) {
        System.out.println("Volver al panel");
    }

    private void limpiarFormulario() {
        txtIdPortafolio.clear();
        txtTitulo.clear();
        txtDescripcion.clear();
        txtUrlImagen.clear();
        cmbServicio.setValue(null);
        spinOrden.getValueFactory().setValue(0);
        chkDestacado.setSelected(false);
        lblMensaje.setText("");
    }
}