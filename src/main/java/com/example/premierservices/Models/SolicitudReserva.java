package com.example.premierservices.Models;

import javafx.beans.property.*;

public class SolicitudReserva {

    public enum Estado { PENDIENTE, ACEPTADA, CONFIRMADO, CANCELADO, COMPLETADO }

    private final IntegerProperty    idReserva    = new SimpleIntegerProperty();
    private final StringProperty     cliente      = new SimpleStringProperty();
    private final StringProperty     servicio     = new SimpleStringProperty();
    private final StringProperty     fechaEvento  = new SimpleStringProperty();
    private final StringProperty     direccion    = new SimpleStringProperty();
    private final ObjectProperty<Estado> estado   = new SimpleObjectProperty<>();

    public SolicitudReserva(int id, String cliente, String servicio, String fechaEvento, Estado estado) {
        this(id, cliente, servicio, fechaEvento, "", estado);
    }

    public SolicitudReserva(int id, String cliente, String servicio, String fechaEvento, String direccion, Estado estado) {
        this.idReserva.set(id);
        this.cliente.set(cliente);
        this.servicio.set(servicio);
        this.fechaEvento.set(fechaEvento);
        this.direccion.set(direccion != null ? direccion : "");
        this.estado.set(estado);
    }

    public IntegerProperty idReservaProperty()   { return idReserva; }
    public StringProperty  clienteProperty()     { return cliente; }
    public StringProperty  servicioProperty()    { return servicio; }
    public StringProperty  fechaEventoProperty() { return fechaEvento; }
    public StringProperty  direccionProperty()   { return direccion; }
    public ObjectProperty<Estado> estadoProperty() { return estado; }

    public int    getIdReserva()   { return idReserva.get(); }
    public String getCliente()     { return cliente.get(); }
    public String getServicio()    { return servicio.get(); }
    public String getFechaEvento() { return fechaEvento.get(); }
    public String getDireccion()   { return direccion.get(); }
    public Estado getEstado()      { return estado.get(); }
    public void   setEstado(Estado e) { this.estado.set(e); }
    public void   setDireccion(String d) { this.direccion.set(d != null ? d : ""); }
}
