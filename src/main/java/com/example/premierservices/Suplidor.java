package com.example.premierservices;

public class Suplidor {

    private int id_suplidor;
    private int id_usuario;
    private String nombre_empresa;
    private String telefono;
    private String ubicacion;
    private int plan_id;
    private double calificacion_promedio;

    public Suplidor(int id_suplidor, int id_usuario, String nombre_empresa,
                    String telefono, String ubicacion, int plan_id,
                    double calificacion_promedio) {
        this.id_suplidor = id_suplidor;
        this.id_usuario = id_usuario;
        this.nombre_empresa = nombre_empresa;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.plan_id = plan_id;
        this.calificacion_promedio = calificacion_promedio;
    }

    public int getId_suplidor() { return id_suplidor; }
    public int getId_usuario() { return id_usuario; }
    public String getNombre_empresa() { return nombre_empresa; }
    public String getTelefono() { return telefono; }
    public String getUbicacion() { return ubicacion; }
    public int getPlan_id() { return plan_id; }
    public double getCalificacion_promedio() { return calificacion_promedio; }
}