package Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexion a SQL Server de la base de datos GestionClientes.
 * Es la misma base de datos del laboratorio inicial (script database/GestionClientes.sql).
 */
public class Conexion {

    // Parametros de conexion a SQL Server
    private static final String servidor = "localhost";
    private static final String puerto = "1433";
    private static final String baseDatos = "GestionClientes";
    private static final String usuario = "kccp";        // tu usuario
    private static final String password = "TU_CONTRASENA";     // tu contrasena de SQL Server

    // Cadena de conexion JDBC
    private static final String url = "jdbc:sqlserver://" + servidor + ":" + puerto
            + ";databaseName=" + baseDatos
            + ";encrypt=true;trustServerCertificate=true;";

    /**
     * Devuelve una conexion abierta. Si no se puede conectar lanza SQLException
     * para que el controlador muestre un mensaje claro en pantalla.
     */
    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver de SQL Server no encontrado", e);
        }
        return DriverManager.getConnection(url, usuario, password);
    }

    // Metodo main para probar la conexion directamente en NetBeans (clic derecho > Run File)
    public static void main(String[] args) {
        try (Connection cn = Conexion.getConexion()) {
            System.out.println("Prueba de conexion satisfactoria a: " + cn.getCatalog());
        } catch (SQLException e) {
            System.out.println("No se pudo conectar a la base de datos: " + e.getMessage());
        }
    }
}
