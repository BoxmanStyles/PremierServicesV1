package com.example.premierservices.Test;

import com.example.premierservices.Utils.EmailSender;

public class TestEmail {
    public static void main(String[] args) {
        System.out.println("=== ENVIANDO CORREO DE PRUEBA ===");

        try {
            EmailSender.enviarCorreoConfirmacion(
                    "jeanmichael0025@gmail.com",
                    "Cliente Prueba",
                    "Servicio Test",
                    "15/06/2026",
                    "Av. 27 de Febrero #150, Santo Domingo",  // dirección de entrega
                    "Proveedor Test",
                    "809-555-0000",
                    "test@test.com"
            );
            System.out.println("Correo enviado correctamente!");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}