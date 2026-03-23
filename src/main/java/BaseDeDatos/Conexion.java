package BaseDeDatos;

import javax.swing.*;
import java.sql.*;

public class Conexion {

    Connection connection = null;

    String usuario = "wilenny";
    String contrase = "1234";
    String db = "PremierServicesV1";
    String server = "26.228.126.202";
    String puerto = "1433";

    String cadena = "jdbc:sqlserver://" + server + ":" + puerto +
            ";databaseName=" + db +
            ";encrypt=true;trustServerCertificate=true";

    public Connection establecerConexion() {

        try {
            connection = DriverManager.getConnection(cadena, usuario, contrase);
            JOptionPane.showMessageDialog(null, "Se conectó correctamente a la BD");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error en la conexión: " + e.toString());
        }
        return connection;
    }

    // INSERTAR SUPLIDOR
    public void insertarSuplidor(int idUsuario,
                                 String nombreEmpresa,
                                 String telefono,
                                 String ubicacion,
                                 int planId,
                                 double calificacion) {

        String sql = "INSERT INTO suplidores (id_usuario, nombre_empresa, telefono, ubicacion, plan_id, calificacion_promedio) VALUES (?,?,?,?,?,?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, idUsuario);
            pstmt.setString(2, nombreEmpresa);
            pstmt.setString(3, telefono);
            pstmt.setString(4, ubicacion);
            pstmt.setInt(5, planId);
            pstmt.setDouble(6, calificacion);

            int filas = pstmt.executeUpdate();

            System.out.println("Filas insertadas: " + filas);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, "Error al insertar suplidor: " + e.toString());

        }
    }

    // ACTUALIZAR PLAN DE PROVEEDOR
    public void actualizarPlanProveedor(int idSuplidor, int nuevoPlan) {

        String sql = "UPDATE suplidores SET plan_id = ? WHERE id_suplidor = ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, nuevoPlan);
            pstmt.setInt(2, idSuplidor);

            int filas = pstmt.executeUpdate();

            System.out.println("Filas actualizadas: " + filas);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, "Error al actualizar plan: " + e.toString());

        }
    }

    // BORRAR SUPLIDOR
    public void borrarSuplidor(int id) {

        String query = "DELETE FROM suplidores WHERE id_suplidor = ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, id);

            int filas = pstmt.executeUpdate();

            System.out.println("Registros borrados: " + filas);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, "Error al borrar datos: " + e.toString());

        }
    }

    // LEER DATOS
    public void leerSuplidores() {

        String sql = "SELECT * FROM suplidores";

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                System.out.println("ID Suplidor: " + rs.getInt("id_suplidor"));
                System.out.println("Empresa: " + rs.getString("nombre_empresa"));
                System.out.println("Teléfono: " + rs.getString("telefono"));
                System.out.println("Ubicación: " + rs.getString("ubicacion"));
                System.out.println("Plan: " + rs.getInt("plan_id"));
                System.out.println("Calificación: " + rs.getDouble("calificacion_promedio"));
                System.out.println("--------------------------");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, "Error al leer datos: " + e.toString());

        }
    }
}