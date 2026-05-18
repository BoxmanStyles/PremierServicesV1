package com.example.premierservices.Utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";  // Cambiado a 465 para SSL
    private static final String EMAIL_FROM = "jeanmichael0025@gmail.com";
    private static final String EMAIL_PASSWORD = "nqqo cswo eoyv ayad";

    public static void enviarCorreoConfirmacion(String emailCliente, String nombreCliente,
                                                String nombreServicio, String fechaEvento,
                                                String nombreProveedor, String telefonoProveedor,
                                                String emailProveedor) {
        String asunto = "Tu reserva ha sido confirmada - Premier Services";

        String cuerpo = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                        "<div style='background-color: #0A1832; padding: 20px; text-align: center;'>" +
                        "<h2 style='color: white;'>Premier Services</h2>" +
                        "</div>" +
                        "<div style='background-color: white; padding: 20px;'>" +
                        "<h3>¡Hola %s!</h3>" +
                        "<p>Tu reserva para el servicio <strong>%s</strong> ha sido <strong style='color: #27ae60;'>CONFIRMADA</strong>.</p>" +
                        "<p><strong> Fecha del evento:</strong> %s</p>" +
                        "<p>Ponga se en contacto con el proveedor través de:</p>" +
                        "<ul>" +
                        "<li><strong>📞 Numero de Teléfono:</strong> %s</li>" +
                        "<li><strong>📧 Correo electrónico:</strong> %s</li>" +
                        "</ul>" +
                        "<p><strong>Proveedor:</strong> %s</p>" +
                        "<p>Gracias por confiar en <strong>Premier Services</strong>.</p>" +
                        "<p>¡Disfruta de tu evento!</p>" +
                        "</div>" +
                        "<div style='background-color: #0A1832; padding: 10px; text-align: center;'>" +
                        "<p style='color: white; font-size: 12px;'>© 2026 Premier Services - Todos los derechos reservados</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                nombreCliente, nombreServicio, fechaEvento, telefonoProveedor, emailProveedor, nombreProveedor
        );

        enviarCorreo(emailCliente, asunto, cuerpo);
    }

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

        // session.setDebug(true); // Descomenta para ver logs detallados

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

    public static void enviarCorreoCancelacion(String emailCliente, String nombreCliente,
                                               String nombreServicio, String fechaEvento) {
        String asunto = "❌ Tu reserva ha sido cancelada - Premier Services";

        String cuerpo = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                        "<div style='background-color: #0A1832; padding: 20px; text-align: center;'>" +
                        "<h2 style='color: white;'>Premier Services</h2>" +
                        "</div>" +
                        "<div style='background-color: white; padding: 20px;'>" +
                        "<h3>¡Hola %s!</h3>" +
                        "<p>Lamentamos informarte que tu reserva para el servicio <strong>%s</strong> ha sido <strong style='color: #dc2626;'>CANCELADA</strong>.</p>" +
                        "<p><strong> Fecha del evento:</strong> %s</p>" +
                        "<p>Si tienes alguna duda o deseas reagendar, por favor contacta al proveedor directamente a través de la plataforma.</p>" +
                        "<p>Sentimos los inconvenientes que esto pueda causarte.</p>" +
                        "<p>Quedamos atentos para ayudarte con nuevas reservas.</p>" +
                        "</div>" +
                        "<div style='background-color: #0A1832; padding: 10px; text-align: center;'>" +
                        "<p style='color: white; font-size: 12px;'>© 2026 Premier Services - Todos los derechos reservados</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                nombreCliente, nombreServicio, fechaEvento
        );

        enviarCorreo(emailCliente, asunto, cuerpo);
    }

}