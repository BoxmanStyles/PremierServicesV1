package BaseDeDatos;

public class Main {

    public static void main(String[] args) {

        Conexion conexion = new Conexion();

        conexion.establecerConexion();

        conexion.leerSuplidores();

    }

}