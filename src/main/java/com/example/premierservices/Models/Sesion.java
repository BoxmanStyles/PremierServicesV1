package com.example.premierservices.Models;

public class Sesion {
    private static int idUsuario;
    private static int idCliente;
    private static int idSuplidor;
    private static String nombre;
    private static String tipo; // "cliente", "suplidor", "admin"

    public static int getIdUsuario() { return idUsuario; }
    public static void setIdUsuario(int id) { idUsuario = id; }
    public static int getIdCliente() { return idCliente; }
    public static void setIdCliente(int id) { idCliente = id; }
    public static int getIdSuplidor() { return idSuplidor; }
    public static void setIdSuplidor(int id) { idSuplidor = id; }
    public static String getNombre() { return nombre; }
    public static void setNombre(String n) { nombre = n; }
    public static String getTipo() { return tipo; }
    public static void setTipo(String t) { tipo = t; }
    public static void limpiar() {
        idUsuario = 0; idCliente = 0; idSuplidor = 0; nombre = null; tipo = null;
    }
}