package com.example.premierservices;


public class Servicio {
    private int idServicio;
    private int idSuplidor;
    private String nombreSuplidor;
    private String nombreServicio;
    private String categoria;
    private String ubicacion;
    private double calificacion;
    private int totalResenas;
    private double precio;
    private String descripcion;private String planSuscripcion;
    private boolean disponible;
    private String telefono;

    public Servicio() {
    }

    // Constructor
    public Servicio(int idServicio, int idSuplidor, String nombreSuplidor, 
                   String nombreServicio, String categoria, String ubicacion,
                   double calificacion, int totalResenas, double precio, 
                   String descripcion, String planSuscripcion, boolean disponible) {
        this.idServicio = idServicio;
        this.idSuplidor = idSuplidor;
        this.nombreSuplidor = nombreSuplidor;
        this.nombreServicio = nombreServicio;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.calificacion = calificacion;
        this.totalResenas = totalResenas;
        this.precio = precio;
        this.descripcion = descripcion;
        this.planSuscripcion = planSuscripcion;
        this.disponible = disponible;
    }

    // Getters y Setters
    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public int getIdSuplidor() {
        return idSuplidor;
    }

    public void setIdSuplidor(int idSuplidor) {
        this.idSuplidor = idSuplidor;
    }

    public String getNombreSuplidor() {
        return nombreSuplidor;
    }

    public void setNombreSuplidor(String nombreSuplidor) {
        this.nombreSuplidor = nombreSuplidor;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public int getTotalResenas() {
        return totalResenas;
    }

    public void setTotalResenas(int totalResenas) {
        this.totalResenas = totalResenas;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPlanSuscripcion() {
        return planSuscripcion;
    }

    public void setPlanSuscripcion(String planSuscripcion) {
        this.planSuscripcion = planSuscripcion;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // Método helper para obtener el icono según categoría
    public String getIcono() {
        switch (categoria.toLowerCase()) {
            case "fotografia":
            case "fotografía":
                return "📸";
            case "catering":
                return "🍽️";
            case "decoracion":
            case "decoración":
                return "🎨";
            case "musica":
            case "música":
                return "🎵";
            case "videografia":
            case "videografía":
                return "🎥";
            case "coordinacion":
            case "coordinación":
                return "📋";
            default:
                return "✨";
        }
    }

    @Override
    public String toString() {
        return "Servicio{" +
                "idServicio=" + idServicio +
                ", nombreSuplidor='" + nombreSuplidor + '\'' +
                ", nombreServicio='" + nombreServicio + '\'' +
                ", categoria='" + categoria + '\'' +
                ", precio=" + precio +
                '}';
    }
}
