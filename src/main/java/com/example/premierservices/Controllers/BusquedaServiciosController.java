package com.example.premierservices.Controllers;

import com.example.premierservices.Servicio;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BusquedaServiciosController {

    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbUbicacion;
    @FXML private ComboBox<String> cmbPrecio;
    @FXML private ComboBox<String> cmbCalificacion;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbOrden;
    @FXML private Label lblResultados;
    @FXML private FlowPane flowServicios;

    private List<Servicio> todosServicios;
    private List<Servicio> serviciosFiltrados;

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

    @FXML
    public void initialize() {
        inicializarComboBoxes();
        cargarServicios();
        mostrarServicios(todosServicios);
    }

    private void inicializarComboBoxes() {
        cmbCategoria.getItems().addAll(
                "Todas las categorías",
                "Organización",
                "Catering",
                "Fotografía",
                "Decoración",
                "Audio",
                "Música",
                "Banquetes",
                "Flores",
                "Video",
                "Mobiliario",
                "Entretenimiento",
                "Repostería"
        );
        cmbCategoria.setValue("Todas las categorías");

        cmbUbicacion.getItems().addAll(
                "Todas las ubicaciones",
                "Santo Domingo",
                "Santiago",
                "La Vega",
                "Puerto Plata",
                "San Pedro de Macorís"
        );
        cmbUbicacion.setValue("Todas las ubicaciones");

        cmbPrecio.getItems().addAll(
                "Cualquier precio",
                "Menos de $500",
                "$500 - $1,500",
                "Más de $1,500"
        );
        cmbPrecio.setValue("Cualquier precio");

        cmbCalificacion.getItems().addAll(
                "Cualquier calificación",
                "⭐⭐⭐⭐⭐ 5 estrellas",
                "⭐⭐⭐⭐ 4+ estrellas",
                "⭐⭐⭐ 3+ estrellas"
        );
        cmbCalificacion.setValue("Cualquier calificación");

        cmbOrden.getItems().addAll(
                "Relevancia",
                "Mayor calificación",
                "Precio: menor a mayor",
                "Precio: mayor a menor"
        );
        cmbOrden.setValue("Relevancia");
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

        serviciosFiltrados = new ArrayList<>(todosServicios);
    }

    @FXML
    protected void buscarServicios() {
        aplicarFiltros();
    }

    @FXML
    protected void aplicarFiltros() {
        serviciosFiltrados = new ArrayList<>(todosServicios);

        String textoBusqueda = txtBusqueda.getText().toLowerCase().trim();
        if (!textoBusqueda.isEmpty()) {
            serviciosFiltrados = serviciosFiltrados.stream()
                    .filter(s -> s.getNombreSuplidor().toLowerCase().contains(textoBusqueda) ||
                            s.getNombreServicio().toLowerCase().contains(textoBusqueda) ||
                            s.getCategoria().toLowerCase().contains(textoBusqueda) ||
                            s.getDescripcion().toLowerCase().contains(textoBusqueda))
                    .collect(Collectors.toList());
        }

        String categoria = cmbCategoria.getValue();
        if (categoria != null && !categoria.equals("Todas las categorías")) {
            serviciosFiltrados = serviciosFiltrados.stream()
                    .filter(s -> s.getCategoria().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
        }

        String ubicacion = cmbUbicacion.getValue();
        if (ubicacion != null && !ubicacion.equals("Todas las ubicaciones")) {
            serviciosFiltrados = serviciosFiltrados.stream()
                    .filter(s -> s.getUbicacion().equalsIgnoreCase(ubicacion))
                    .collect(Collectors.toList());
        }

        String precioRango = cmbPrecio.getValue();
        if (precioRango != null && !precioRango.equals("Cualquier precio")) {
            switch (precioRango) {
                case "Menos de $500" -> serviciosFiltrados = serviciosFiltrados.stream()
                        .filter(s -> s.getPrecio() < 500).collect(Collectors.toList());
                case "$500 - $1,500" -> serviciosFiltrados = serviciosFiltrados.stream()
                        .filter(s -> s.getPrecio() >= 500 && s.getPrecio() <= 1500).collect(Collectors.toList());
                case "Más de $1,500" -> serviciosFiltrados = serviciosFiltrados.stream()
                        .filter(s -> s.getPrecio() > 1500).collect(Collectors.toList());
            }
        }

        String calificacion = cmbCalificacion.getValue();
        if (calificacion != null && !calificacion.equals("Cualquier calificación")) {
            double minCalificacion = 0;
            if (calificacion.contains("5 estrellas")) minCalificacion = 5.0;
            else if (calificacion.contains("4+")) minCalificacion = 4.0;
            else if (calificacion.contains("3+")) minCalificacion = 3.0;

            final double minCal = minCalificacion;
            serviciosFiltrados = serviciosFiltrados.stream()
                    .filter(s -> s.getCalificacion() >= minCal)
                    .collect(Collectors.toList());
        }

        ordenarResultados();
    }

    @FXML
    protected void ordenarResultados() {
        String orden = cmbOrden.getValue();

        if (orden != null) {
            switch (orden) {
                case "Mayor calificación" -> serviciosFiltrados.sort(
                        Comparator.comparingDouble(Servicio::getCalificacion).reversed());
                case "Precio: menor a mayor" -> serviciosFiltrados.sort(
                        Comparator.comparingDouble(Servicio::getPrecio));
                case "Precio: mayor a menor" -> serviciosFiltrados.sort(
                        Comparator.comparingDouble(Servicio::getPrecio).reversed());
                default -> serviciosFiltrados.sort((s1, s2) -> {
                    int p1 = getPlanOrder(s1.getPlanSuscripcion());
                    int p2 = getPlanOrder(s2.getPlanSuscripcion());
                    if (p1 != p2) return p2 - p1;
                    return Double.compare(s2.getCalificacion(), s1.getCalificacion());
                });
            }
        }

        mostrarServicios(serviciosFiltrados);
    }

    private int getPlanOrder(String plan) {
        return switch (plan.toLowerCase()) {
            case "prime"  -> 3;
            case "elite"  -> 2;
            case "rookie" -> 1;
            default       -> 0;
        };
    }

    private void mostrarServicios(List<Servicio> servicios) {
        flowServicios.getChildren().clear();
        lblResultados.setText("Mostrando " + servicios.size() + " resultados");

        if (servicios.isEmpty()) {
            VBox noResults = new VBox(10);
            noResults.setAlignment(Pos.CENTER);
            noResults.setPadding(new Insets(60));

            Label lblEmpty = new Label("No se encontraron resultados");
            lblEmpty.setFont(Font.font("System", FontWeight.BOLD, 24));
            lblEmpty.setStyle("-fx-text-fill: #7f8c8d;");

            Label lblTip = new Label("Intenta ajustar tus filtros de búsqueda");
            lblTip.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");

            noResults.getChildren().addAll(lblEmpty, lblTip);
            flowServicios.getChildren().add(noResults);
            return;
        }

        for (Servicio servicio : servicios) {
            flowServicios.getChildren().add(crearTarjetaServicio(servicio));
        }
    }

    private VBox crearTarjetaServicio(Servicio servicio) {
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