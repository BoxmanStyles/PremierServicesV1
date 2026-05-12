package com.example.premierservices.Controllers.GlobalController;

import com.example.premierservices.Servicio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class controllerPaginaPrincipal {

    @FXML private Pane PanelBuscador;
    @FXML private Pane PanelBandeja;
    @FXML private ImageView imagenPerfil;
    @FXML private FlowPane flowServicios;
    @FXML private TextField txtBuscadorPrincipal;
    @FXML private Button btnBuscarPrincipal;
    @FXML private ImageView Logoimg;
    @FXML private Button BotonDeFiltros;

    private List<Servicio> todosServicios;
    private String categoriaActiva = null;

    // Método faltante que se llama desde el botón "Account"
    @FXML
    private void AbrirLoginPaquinaPrincipal(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginGeneralV2.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar Sesión");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la pantalla de inicio de sesión");
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized.toLowerCase();
    }

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
    protected void Buscador(ActionEvent actionEvent) {
        PanelBuscador.setVisible(true);
        txtBuscadorPrincipal.requestFocus();
    }

    @FXML
    protected void SalirBuscar(ActionEvent actionEvent) {
        PanelBuscador.setVisible(false);
        txtBuscadorPrincipal.clear();
        categoriaActiva = null;
        aplicarFiltros();
        BotonDeFiltros.setVisible(false);
    }

    @FXML
    protected void BandejaAbrir(ActionEvent actionEvent) {
        PanelBandeja.setVisible(true);
    }

    @FXML
    public void SalirBandeja(ActionEvent actionEvent) {
        PanelBandeja.setVisible(false);
    }

    @FXML
    protected void filtrarPorCategoria(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String categoria = btn.getText();
        categoriaActiva = categoria;
        aplicarFiltros();
        BotonDeFiltros.setVisible(true);
    }

    @FXML
    public void AbirYCerrarFiltros(ActionEvent event) {
        categoriaActiva = null;
        txtBuscadorPrincipal.clear();
        aplicarFiltros();
        BotonDeFiltros.setVisible(false);
    }

    private void aplicarFiltros() {
        String textoBusquedaNormalizado = normalizarTexto(txtBuscadorPrincipal.getText());
        List<Servicio> filtrados = new ArrayList<>(todosServicios);
        if (categoriaActiva != null && !categoriaActiva.isEmpty()) {
            String catActivaNormalizada = normalizarTexto(categoriaActiva);
            filtrados = filtrados.stream()
                    .filter(s -> normalizarTexto(s.getCategoria()).equals(catActivaNormalizada))
                    .collect(Collectors.toList());
        }
        if (!textoBusquedaNormalizado.isEmpty()) {
            filtrados = filtrados.stream()
                    .filter(s -> normalizarTexto(s.getNombreSuplidor()).contains(textoBusquedaNormalizado) ||
                            normalizarTexto(s.getNombreServicio()).contains(textoBusquedaNormalizado) ||
                            normalizarTexto(s.getCategoria()).contains(textoBusquedaNormalizado) ||
                            normalizarTexto(s.getDescripcion()).contains(textoBusquedaNormalizado))
                    .collect(Collectors.toList());
        }
        mostrarServicios(filtrados);
    }

    private void realizarBusqueda() { aplicarFiltros(); }

    private void cargarServicios() {
        todosServicios = new ArrayList<>();
        String sql = "SELECT s.id_servicio, s.id_suplidor, p.nombre_empresa, " +
                "s.nombre_servicio, s.categoria, p.ubicacion, " +
                "p.calificacion_promedio, s.descripcion, s.precio, " +
                "p.plan_id, s.ruta_imagen " +
                "FROM dbo.tbl_servicios s " +
                "INNER JOIN dbo.tbl_suplidores p ON s.id_suplidor = p.id_suplidor " +
                "WHERE s.estado = 'activo'";
        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String plan = switch (rs.getInt("plan_id")) {
                    case 3 -> "prime";
                    case 2 -> "elite";
                    default -> "rookie";
                };
                String rutaImagen = rs.getString("ruta_imagen");
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
                        true,
                        rutaImagen
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
        for (Servicio s : servicios) {
            flowServicios.getChildren().add(crearTarjetaServicio(s));
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
        String rutaImg = servicio.getRutaImagen();
        if (rutaImg != null && !rutaImg.isEmpty()) {
            File imgFile = new File(rutaImg);
            if (imgFile.exists()) {
                ImageView imgView = new ImageView(imgFile.toURI().toString());
                imgView.setFitHeight(200);
                imgView.setFitWidth(350);
                imgView.setPreserveRatio(true);
                imagePane.getChildren().add(imgView);
            } else {
                Label icon = new Label(servicio.getIcono());
                icon.setFont(Font.font(48));
                imagePane.getChildren().add(icon);
            }
        } else {
            Label icon = new Label(servicio.getIcono());
            icon.setFont(Font.font(48));
            imagePane.getChildren().add(icon);
        }

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        VBox leftHeader = new VBox(5);
        Label categoria = new Label(servicio.getCategoria().toUpperCase());
        categoria.setStyle("-fx-text-fill: #667eea; -fx-font-size: 12; -fx-font-weight: bold;");
        Label nombreServicio = new Label(servicio.getNombreServicio());
        nombreServicio.setFont(Font.font("System", FontWeight.BOLD, 18));
        nombreServicio.setStyle("-fx-text-fill: #2c3e50;");
        Label nombreProveedor = new Label(servicio.getNombreSuplidor());
        nombreProveedor.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");
        leftHeader.getChildren().addAll(categoria, nombreServicio, nombreProveedor);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rating = new HBox(5);
        rating.setAlignment(Pos.CENTER_RIGHT);
        Label star = new Label("⭐");
        Label ratingText = new Label(String.format("%.1f (%d)", servicio.getCalificacion(), servicio.getTotalResenas()));
        ratingText.setStyle("-fx-text-fill: #555; -fx-font-size: 13;");
        rating.getChildren().addAll(star, ratingText);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
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

        VBox priceBox = new VBox(2);
        Label precio = new Label(String.format("$%.0f", servicio.getPrecio()));
        precio.setFont(Font.font("System", FontWeight.BOLD, 20));
        precio.setStyle("-fx-text-fill: #27ae60;");
        Label desde = new Label("desde");
        desde.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13;");
        priceBox.getChildren().addAll(precio, desde);

        HBox priceRow = new HBox();
        priceRow.setAlignment(Pos.CENTER_LEFT);
        priceRow.getChildren().add(priceBox);
        VBox.setMargin(priceRow, new Insets(0, 0, 10, 0));

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnContactar = new Button("Contactar");
        btnContactar.setStyle("-fx-background-color: #003566; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 10 20; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");
        // Redirigir al login
        btnContactar.setOnAction(e -> abrirLogin());
        footer.getChildren().add(btnContactar);

        content.getChildren().addAll(header, ubicacion, descripcion, separator, priceRow, footer);
        card.getChildren().addAll(imagePane, content);
        card.setOnMouseClicked(e -> abrirLogin());
        return card;
    }

    private void abrirLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginGeneralV2.fxml"));
            Stage stage = (Stage) flowServicios.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar Sesión");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la pantalla de inicio de sesión");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        // Cargar imagen de perfil por defecto (ícono global)
        try {
            File filePerfil = new File("C:/Users/jeanm/IdeaProjects/PremierServicesV1/IMG/Perfil 1 sin fondo.png");
            if (filePerfil.exists()) {
                imagenPerfil.setImage(new Image(filePerfil.toURI().toString()));
            } else {
                System.err.println("No se encontró imagen de perfil por defecto");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cargar logo
        File fileLogo = new File("C:/Users/jeanm/IdeaProjects/PremierServicesV1/IMG/Logo.png");
        if (fileLogo.exists()) {
            Logoimg.setImage(new Image(fileLogo.toURI().toString()));
        }

        btnBuscarPrincipal.setOnAction(e -> realizarBusqueda());
        txtBuscadorPrincipal.setOnAction(e -> realizarBusqueda());

        cargarServicios();
        categoriaActiva = null;
        aplicarFiltros();
        BotonDeFiltros.setVisible(false);
    }
}