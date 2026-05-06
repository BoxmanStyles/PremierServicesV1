package com.example.premierservices.Controllers;

import com.example.premierservices.Servicio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class controllerPaginaPrincipal {

    // Elementos de la pantalla
    @FXML private Pane PanelBuscador;
    @FXML private Pane PanelBandeja;
    @FXML private ImageView imagenPerfil;
    @FXML private FlowPane flowServicios;
    @FXML private TextField txtBuscadorPrincipal;
    @FXML private Button btnBuscarPrincipal;

    private List<Servicio> todosServicios;

    // Conexión a BD
    private Connection conectar() {
        try {
            String url = "jdbc:sqlserver://26.228.126.202:1433;" +
                    "databaseName=PremierServicesV1;" +
                    "encrypt=true;trustServerCertificate=true";
            return DriverManager.getConnection(url, "wilenny", "1234");
        } catch (Exception e) {
            mostrarAlerta("Error de conexión", e.getMessage());
            return null;
        }
    }

    // Métodos de la interfaz (mostrar/ocultar paneles)
    @FXML
    protected void Buscador(ActionEvent actionEvent) {
        PanelBuscador.setVisible(true);
        txtBuscadorPrincipal.requestFocus(); // Enfocar el campo al abrir
    }

    @FXML
    protected void SalirBuscar(ActionEvent actionEvent) {
        PanelBuscador.setVisible(false);
        txtBuscadorPrincipal.clear();        // Limpiar texto
        mostrarServicios(todosServicios);    // Restaurar todos los servicios
    }

    @FXML
    protected void BandejaAbrir(ActionEvent actionEvent) {
        PanelBandeja.setVisible(true);
    }

    @FXML
    public void SalirBandeja(ActionEvent actionEvent) {
        PanelBandeja.setVisible(false);
    }

    // Inicialización
    @FXML
    public void initialize() {
        // Cargar imagen de perfil desde ruta absoluta
        File file = new File("C:/Users/jeanm/IdeaProjects/PremierServicesV1/IMG/Perfil 1 sin fondo.png");
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            imagenPerfil.setImage(image);
        } else {
            System.err.println("Imagen no encontrada: " + file.getAbsolutePath());
        }

        // Configurar búsqueda
        btnBuscarPrincipal.setOnAction(e -> realizarBusqueda());
        txtBuscadorPrincipal.setOnAction(e -> realizarBusqueda());

        cargarServicios();
        mostrarServicios(todosServicios);
    }

    // Método de filtrado
    private void realizarBusqueda() {
        String texto = txtBuscadorPrincipal.getText().trim().toLowerCase();
        if (texto.isEmpty()) {
            mostrarServicios(todosServicios);
            return;
        }

        List<Servicio> filtrados = todosServicios.stream()
                .filter(s -> s.getNombreSuplidor().toLowerCase().contains(texto) ||
                        s.getNombreServicio().toLowerCase().contains(texto) ||
                        s.getCategoria().toLowerCase().contains(texto) ||
                        s.getDescripcion().toLowerCase().contains(texto))
                .collect(Collectors.toList());

        mostrarServicios(filtrados);
    }

    private void cargarServicios() {
        todosServicios = new ArrayList<>();

        String sql = "SELECT s.id_servicio, s.id_suplidor, p.nombre_empresa, " +
                "s.nombre_servicio, s.categoria, p.ubicacion, " +
                "p.calificacion_promedio, s.descripcion, s.precio, " +
                "p.plan_id " +
                "FROM dbo.tbl_servicios s " +
                "INNER JOIN dbo.tbl_suplidores p ON s.id_suplidor = p.id_suplidor " +
                "WHERE s.estado = 'activo'";

        try (Connection con = conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String plan;
                switch (rs.getInt("plan_id")) {
                    case 3  -> plan = "prime";
                    case 2  -> plan = "elite";
                    default -> plan = "rookie";
                }

                Servicio servicio = new Servicio(
                        rs.getInt("id_servicio"),
                        rs.getInt("id_suplidor"),
                        rs.getString("nombre_empresa"),
                        rs.getString("nombre_servicio"),
                        rs.getString("categoria"),
                        rs.getString("ubicacion"),
                        rs.getDouble("calificacion_promedio"),
                        0,
                        rs.getDouble("precio"),
                        rs.getString("descripcion"),
                        plan,
                        true
                );
                todosServicios.add(servicio);
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al cargar servicios", e.getMessage());
        }
    }

    private void mostrarServicios(List<Servicio> servicios) {
        flowServicios.getChildren().clear();

        if (servicios == null || servicios.isEmpty()) {
            VBox noResults = new VBox(10);
            noResults.setAlignment(Pos.CENTER);
            noResults.setPadding(new Insets(60));
            Label lblEmpty = new Label("No hay servicios disponibles");
            lblEmpty.setFont(Font.font("System", FontWeight.BOLD, 24));
            lblEmpty.setStyle("-fx-text-fill: #7f8c8d;");
            noResults.getChildren().add(lblEmpty);
            flowServicios.getChildren().add(noResults);
            return;
        }

        for (Servicio servicio : servicios) {
            flowServicios.getChildren().add(crearTarjetaServicio(servicio));
        }
    }

    // Métodos de creación de tarjetas (sin cambios)
    private VBox crearTarjetaServicio(Servicio servicio) {
        // ... (el código es idéntico al que ya tenías, no se modifica)
        // Solo se copia para completitud, pero puedes dejarlo exactamente igual
        VBox card = new VBox(15);
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setPadding(new Insets(0));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"));

        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(200);
        imagePane.setStyle("-fx-background-color: linear-gradient(to right, #003566, #669bbc);");

        Label icon = new Label(servicio.getIcono());
        icon.setFont(Font.font(48));
        imagePane.getChildren().add(icon);

        if (!servicio.getPlanSuscripcion().equalsIgnoreCase("rookie")) {
            Label badge = new Label();
            if (servicio.getPlanSuscripcion().equalsIgnoreCase("prime")) {
                badge.setText("PRIME");
                badge.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                        "-fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");
            } else {
                badge.setText("ELITE");
                badge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");
            }
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(15));
            imagePane.getChildren().add(badge);
        }

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox leftHeader = new VBox(5);
        Label categoria = new Label(servicio.getCategoria().toUpperCase());
        categoria.setStyle("-fx-text-fill: #667eea; -fx-font-size: 12; -fx-font-weight: bold;");

        Label nombre = new Label(servicio.getNombreSuplidor());
        nombre.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombre.setStyle("-fx-text-fill: #2c3e50;");

        leftHeader.getChildren().addAll(categoria, nombre);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rating = new HBox(5);
        rating.setAlignment(Pos.CENTER_RIGHT);
        Label star = new Label("⭐");
        Label ratingText = new Label(String.format("%.1f (%d)", servicio.getCalificacion(), servicio.getTotalResenas()));
        ratingText.setStyle("-fx-text-fill: #555; -fx-font-size: 13;");
        rating.getChildren().addAll(star, ratingText);

        header.getChildren().addAll(leftHeader, spacer, rating);

        HBox ubicacion = new HBox(5);
        ubicacion.setAlignment(Pos.CENTER_LEFT);
        Label iconUbicacion = new Label("📍");
        Label textUbicacion = new Label(servicio.getUbicacion());
        textUbicacion.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13;");
        ubicacion.getChildren().addAll(iconUbicacion, textUbicacion);

        Label descripcion = new Label(servicio.getDescripcion());
        descripcion.setWrapText(true);
        descripcion.setMaxWidth(310);
        descripcion.setMaxHeight(45);
        descripcion.setStyle("-fx-text-fill: #555; -fx-font-size: 14;");

        Separator separator = new Separator();

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(2);
        Label precio = new Label(String.format("$%.0f", servicio.getPrecio()));
        precio.setFont(Font.font("System", FontWeight.BOLD, 20));
        precio.setStyle("-fx-text-fill: #27ae60;");
        Label desde = new Label("desde");
        desde.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13;");
        priceBox.getChildren().addAll(precio, desde);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button btnContactar = new Button("Contactar");
        btnContactar.setStyle("-fx-background-color: #003566; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 10 20; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");
        btnContactar.setOnAction(e -> contactarSuplidor(servicio));

        footer.getChildren().addAll(priceBox, spacer2, btnContactar);
        content.getChildren().addAll(header, ubicacion, descripcion, separator, footer);
        card.getChildren().addAll(imagePane, content);
        card.setOnMouseClicked(e -> mostrarDetalleServicio(servicio));

        return card;
    }

    private void mostrarDetalleServicio(Servicio servicio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(servicio.getNombreSuplidor());
        alert.setHeaderText(servicio.getNombreServicio());

        String contenido = String.format(
                "Categoria: %s\nUbicacion: %s\nCalificacion: %.1f/5.0 (%d reseñas)\n" +
                        "Precio desde: $%.2f\nPlan: %s\n\nDescripcion:\n%s",
                servicio.getCategoria(), servicio.getUbicacion(),
                servicio.getCalificacion(), servicio.getTotalResenas(),
                servicio.getPrecio(), servicio.getPlanSuscripcion().toUpperCase(),
                servicio.getDescripcion()
        );

        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void contactarSuplidor(Servicio servicio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contactar Suplidor");
        alert.setHeaderText("Iniciar una conversacion con: " + servicio.getNombreSuplidor());
        alert.setContentText("En la version completa:\n- Chat en tiempo real\n" +
                "- Preguntas guiadas segun el servicio\n" +
                "- Generacion automatica de cotizacion\n" +
                "- Fecha en el calendario del proveedor");
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}