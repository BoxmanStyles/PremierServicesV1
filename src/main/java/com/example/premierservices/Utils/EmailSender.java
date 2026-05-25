package com.example.premierservices.Utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final String EMAIL_FROM = "jeanmichael0025@gmail.com";
    private static final String EMAIL_PASSWORD = "nqqo cswo eoyv ayad";

    // ─────────────────────────────────────────────────────────────────────────
    //  ACEPTACIÓN — Primer paso: el proveedor aceptó la solicitud
    // ─────────────────────────────────────────────────────────────────────────
    public static void enviarCorreoAceptacion(String emailCliente, String nombreCliente,
                                              String nombreServicio, String fechaEvento,
                                              String direccion, String nombreProveedor,
                                              String telefonoProveedor, String emailProveedor) {
        String asunto = "✅ Tu solicitud ha sido aceptada - Premier Services";

        String cuerpo = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; margin:0; padding:0;'>" +
                "<div style='max-width: 620px; margin: 0 auto; background-color: #f5f5f5;'>" +
                "<div style='background: linear-gradient(to right, #0A1832, #4A7FA9); padding: 25px; text-align: center;'>" +
                "<h2 style='color: white; margin: 0; letter-spacing: 1px;'>Premier Services</h2>" +
                "<p style='color: #cfe0f0; margin: 5px 0 0 0; font-size: 13px;'>Tu plataforma de servicios para eventos</p>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px;'>" +
                "<h3 style='color: #0A1832;'>¡Hola %s!</h3>" +
                "<p style='font-size: 15px; color: #333;'>Tu solicitud para el servicio <strong>%s</strong> ha sido <strong style='color: #007cff;'>ACEPTADA</strong> por el proveedor.</p>" +
                "<div style='background-color: #f0f7ff; border-left: 4px solid #007cff; padding: 15px 18px; border-radius: 6px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #0A1832; font-size: 14px; line-height: 1.6;'>" +
                "El proveedor <strong>%s</strong> preparará tu pedido para ser entregado en " +
                "<strong>%s</strong> el <strong>%s</strong>." +
                "</p>" +
                "</div>" +
                "<p style='font-size: 14px; color: #555;'>Aún falta que el proveedor confirme la disponibilidad final. Te llegará otro correo cuando esté confirmado.</p>" +
                "<h4 style='color: #0A1832; margin-top: 25px;'>📞 Contacto del proveedor</h4>" +
                "<ul style='font-size: 14px; color: #444;'>" +
                "<li><strong>Teléfono:</strong> %s</li>" +
                "<li><strong>Correo:</strong> %s</li>" +
                "</ul>" +
                "<p style='font-size: 14px; color: #555; margin-top: 25px;'>Gracias por confiar en <strong>Premier Services</strong>.</p>" +
                "</div>" +
                "<div style='background-color: #0A1832; padding: 12px; text-align: center;'>" +
                "<p style='color: white; font-size: 12px; margin: 0;'>© 2026 Premier Services — Todos los derechos reservados</p>" +
                "</div>" +
                "</div>" +
                "</body></html>",
                nombreCliente, nombreServicio, nombreProveedor, direccion, fechaEvento, telefonoProveedor, emailProveedor
        );

        enviarCorreo(emailCliente, asunto, cuerpo);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CONFIRMACIÓN — Segundo paso: el pedido fue confirmado en firme
    // ─────────────────────────────────────────────────────────────────────────
    public static void enviarCorreoConfirmacion(String emailCliente, String nombreCliente,
                                                String nombreServicio, String fechaEvento,
                                                String direccion, String nombreProveedor,
                                                String telefonoProveedor, String emailProveedor) {
        String asunto = "🎉 Tu reserva ha sido confirmada - Premier Services";

        String cuerpo = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; margin:0; padding:0;'>" +
                "<div style='max-width: 620px; margin: 0 auto; background-color: #f5f5f5;'>" +
                "<div style='background: linear-gradient(to right, #0A1832, #4A7FA9); padding: 25px; text-align: center;'>" +
                "<h2 style='color: white; margin: 0; letter-spacing: 1px;'>Premier Services</h2>" +
                "<p style='color: #cfe0f0; margin: 5px 0 0 0; font-size: 13px;'>Tu plataforma de servicios para eventos</p>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px;'>" +
                "<h3 style='color: #0A1832;'>¡Hola %s!</h3>" +
                "<p style='font-size: 15px; color: #333;'>Tu reserva para el servicio <strong>%s</strong> ha sido <strong style='color: #27ae60;'>CONFIRMADA</strong> en firme.</p>" +
                "<div style='background-color: #ecfdf5; border-left: 4px solid #27ae60; padding: 15px 18px; border-radius: 6px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #0A1832; font-size: 14px; line-height: 1.6;'>" +
                "Tu pedido del servicio <strong>%s</strong> será llevado a <strong>%s</strong> el <strong>%s</strong>. " +
                "El proveedor <strong>%s</strong> ya tiene todo preparado para tu evento." +
                "</p>" +
                "</div>" +
                "<h4 style='color: #0A1832; margin-top: 25px;'>📞 Contacto del proveedor</h4>" +
                "<ul style='font-size: 14px; color: #444;'>" +
                "<li><strong>Teléfono:</strong> %s</li>" +
                "<li><strong>Correo:</strong> %s</li>" +
                "</ul>" +
                "<p style='font-size: 14px; color: #555; margin-top: 25px;'>Si tienes alguna duda contacta directamente al proveedor por los medios anteriores.</p>" +
                "<p style='font-size: 14px; color: #555;'>¡Disfruta de tu evento!</p>" +
                "</div>" +
                "<div style='background-color: #0A1832; padding: 12px; text-align: center;'>" +
                "<p style='color: white; font-size: 12px; margin: 0;'>© 2026 Premier Services — Todos los derechos reservados</p>" +
                "</div>" +
                "</div>" +
                "</body></html>",
                nombreCliente, nombreServicio, nombreServicio, direccion, fechaEvento, nombreProveedor, telefonoProveedor, emailProveedor
        );

        enviarCorreo(emailCliente, asunto, cuerpo);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CANCELACIÓN
    // ─────────────────────────────────────────────────────────────────────────
    public static void enviarCorreoCancelacion(String emailCliente, String nombreCliente,
                                               String nombreServicio, String fechaEvento) {
        String asunto = "❌ Tu reserva ha sido cancelada - Premier Services";

        String cuerpo = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; margin:0; padding:0;'>" +
                "<div style='max-width: 620px; margin: 0 auto; background-color: #f5f5f5;'>" +
                "<div style='background: linear-gradient(to right, #0A1832, #4A7FA9); padding: 25px; text-align: center;'>" +
                "<h2 style='color: white; margin: 0; letter-spacing: 1px;'>Premier Services</h2>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px;'>" +
                "<h3 style='color: #0A1832;'>¡Hola %s!</h3>" +
                "<p style='font-size: 15px; color: #333;'>Lamentamos informarte que tu reserva para el servicio <strong>%s</strong> ha sido <strong style='color: #dc2626;'>CANCELADA</strong>.</p>" +
                "<div style='background-color: #fef2f2; border-left: 4px solid #dc2626; padding: 15px 18px; border-radius: 6px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #0A1832; font-size: 14px;'>Fecha original del evento: <strong>%s</strong></p>" +
                "</div>" +
                "<p style='font-size: 14px; color: #555;'>Puedes intentar reservar otro proveedor desde la plataforma. Sentimos los inconvenientes.</p>" +
                "</div>" +
                "<div style='background-color: #0A1832; padding: 12px; text-align: center;'>" +
                "<p style='color: white; font-size: 12px; margin: 0;'>© 2026 Premier Services — Todos los derechos reservados</p>" +
                "</div>" +
                "</div>" +
                "</body></html>",
                nombreCliente, nombreServicio, fechaEvento
        );

        enviarCorreo(emailCliente, asunto, cuerpo);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Envío SMTP real
    // ─────────────────────────────────────────────────────────────────────────
    private static void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setContent(cuerpoHtml, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Correo enviado a: " + destinatario);

        } catch (MessagingException e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
