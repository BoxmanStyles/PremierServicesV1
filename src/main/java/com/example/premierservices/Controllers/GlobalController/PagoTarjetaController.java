package com.example.premierservices.Controllers.GlobalController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PagoTarjetaController {

    // ─── Vista de la tarjeta (display visual) ─────────────────────────────
    @FXML private Label lblPlanInfo;
    @FXML private Label lblCardNumero;
    @FXML private Label lblCardNombre;
    @FXML private Label lblCardExpira;

    // ─── Inputs del formulario ───────────────────────────────────────────
    @FXML private TextField txtNumero;
    @FXML private TextField txtNombre;
    @FXML private TextField txtExpira;
    @FXML private TextField txtCvv;

    // ─── Estado ──────────────────────────────────────────────────────────
    private boolean pagoCompletado = false;

    @FXML
    public void initialize() {
        // Sincronizar el display de la tarjeta con lo que el usuario escribe

        // Número de tarjeta → formatear como "1234 5678 9012 3456"
        txtNumero.textProperty().addListener((obs, oldV, newV) -> {
            // Quitar todo lo que no sea dígito, máximo 16
            String soloDigitos = newV.replaceAll("[^0-9]", "");
            if (soloDigitos.length() > 16) soloDigitos = soloDigitos.substring(0, 16);

            // Reformatear con espacios cada 4 dígitos
            StringBuilder formateado = new StringBuilder();
            for (int i = 0; i < soloDigitos.length(); i++) {
                if (i > 0 && i % 4 == 0) formateado.append(" ");
                formateado.append(soloDigitos.charAt(i));
            }

            // Actualizar el campo si cambió (sin disparar bucle infinito)
            if (!formateado.toString().equals(newV)) {
                txtNumero.setText(formateado.toString());
                return;
            }

            // Actualizar el display de la tarjeta
            String mostrar = formateado.toString();
            while (mostrar.replaceAll(" ", "").length() < 16) {
                if (mostrar.replaceAll(" ", "").length() % 4 == 0 && mostrar.length() > 0
                        && !mostrar.endsWith(" ")) {
                    mostrar += " ";
                }
                mostrar += "•";
            }
            lblCardNumero.setText(mostrar);
        });

        // Nombre del titular → mayúsculas en la tarjeta
        txtNombre.textProperty().addListener((obs, oldV, newV) -> {
            String n = newV == null || newV.isBlank() ? "NOMBRE APELLIDO" : newV.toUpperCase();
            if (n.length() > 24) n = n.substring(0, 24);
            lblCardNombre.setText(n);
        });

        // Fecha de expiración → formatear como MM/YY
        txtExpira.textProperty().addListener((obs, oldV, newV) -> {
            String soloDigitos = newV.replaceAll("[^0-9]", "");
            if (soloDigitos.length() > 4) soloDigitos = soloDigitos.substring(0, 4);

            String formateado = soloDigitos.length() > 2
                    ? soloDigitos.substring(0, 2) + "/" + soloDigitos.substring(2)
                    : soloDigitos;

            if (!formateado.equals(newV)) {
                txtExpira.setText(formateado);
                return;
            }
            lblCardExpira.setText(formateado.isBlank() ? "MM/YY" : formateado);
        });

        // CVV solo dígitos, máximo 4
        txtCvv.textProperty().addListener((obs, oldV, newV) -> {
            String soloDigitos = newV.replaceAll("[^0-9]", "");
            if (soloDigitos.length() > 4) soloDigitos = soloDigitos.substring(0, 4);
            if (!soloDigitos.equals(newV)) txtCvv.setText(soloDigitos);
        });
    }

    /** Llamado por SeleccionPlanController para inyectar el plan elegido */
    public void setPlanInfo(String plan, double monto) {
        if (lblPlanInfo != null) {
            lblPlanInfo.setText(String.format("Plan %s — USD %.2f / mes", plan, monto));
        }
    }

    /** Indica si el usuario completó el "pago" (prototipo visual) */
    public boolean isPagoCompletado() {
        return pagoCompletado;
    }

    @FXML
    void onPagar(ActionEvent event) {
        // Validación visual mínima — solo verificar que los campos no estén vacíos
        if (txtNumero.getText().replaceAll(" ", "").length() < 13 ||
            txtNombre.getText().isBlank() ||
            txtExpira.getText().length() < 5 ||
            txtCvv.getText().length() < 3) {

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Datos incompletos");
            alert.setHeaderText("Completa todos los campos de la tarjeta");
            alert.setContentText("Verifica que el número, nombre, fecha y CVV estén correctos.");
            alert.showAndWait();
            return;
        }

        pagoCompletado = true;
        cerrarVentana(event);
    }

    @FXML
    void onCancelar(ActionEvent event) {
        pagoCompletado = false;
        cerrarVentana(event);
    }

    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
