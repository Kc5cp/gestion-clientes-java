package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla Cliente.
 * Los metodos lanzan SQLException para que el controlador pueda mostrar
 * un mensaje de error real en pantalla (antes los errores se ocultaban).
 */
public class ClienteDAO {

    private static final String COLUMNAS =
            "idCliente, documentoDni, apellidos, nombres, correoElectronico, numeroCelular, direccionDomicilio";

    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM Cliente ORDER BY idCliente";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Busca clientes cuyo DNI, apellidos, nombres o correo contengan el texto indicado.
     * Los comodines de SQL (% _ [) escritos por el usuario se toman como texto normal.
     */
    public List<Cliente> buscar(String texto) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM Cliente "
                + "WHERE documentoDni LIKE ? OR apellidos LIKE ? OR nombres LIKE ? "
                + "OR correoElectronico LIKE ? "
                + "OR (apellidos + ' ' + nombres) LIKE ? OR (nombres + ' ' + apellidos) LIKE ? "
                + "ORDER BY idCliente";
        String patron = "%" + escaparComodines(texto) + "%";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 6; i++) {
                ps.setString(i, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Cantidad total de clientes registrados. */
    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Cliente";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Cliente obtenerPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM Cliente WHERE idCliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Indica si el DNI ya pertenece a OTRO cliente.
     *
     * @param dni       DNI a verificar
     * @param idExcluir id del cliente que se esta modificando
     *                  (usar 0 cuando se registra un cliente nuevo)
     */
    public boolean existeDni(String dni, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE documentoDni = ? AND idCliente <> ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dni);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean agregar(Cliente c) throws SQLException {
        String sql = "INSERT INTO Cliente (documentoDni, apellidos, nombres, correoElectronico, "
                + "numeroCelular, direccionDomicilio) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getDocumentoDni());
            ps.setString(2, c.getApellidos());
            ps.setString(3, c.getNombres());
            ps.setString(4, c.getCorreoElectronico());
            ps.setString(5, c.getNumeroCelular());
            ps.setString(6, c.getDireccionDomicilio());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE Cliente SET documentoDni = ?, apellidos = ?, nombres = ?, "
                + "correoElectronico = ?, numeroCelular = ?, direccionDomicilio = ? WHERE idCliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getDocumentoDni());
            ps.setString(2, c.getApellidos());
            ps.setString(3, c.getNombres());
            ps.setString(4, c.getCorreoElectronico());
            ps.setString(5, c.getNumeroCelular());
            ps.setString(6, c.getDireccionDomicilio());
            ps.setInt(7, c.getIdCliente());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Cliente WHERE idCliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------ Utilidades
    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("idCliente"));
        c.setDocumentoDni(rs.getString("documentoDni"));
        c.setApellidos(rs.getString("apellidos"));
        c.setNombres(rs.getString("nombres"));
        c.setCorreoElectronico(rs.getString("correoElectronico"));
        c.setNumeroCelular(rs.getString("numeroCelular"));
        c.setDireccionDomicilio(rs.getString("direccionDomicilio"));
        return c;
    }

    /** En SQL Server LIKE, los caracteres % _ [ son comodines: se encierran entre corchetes. */
    private String escaparComodines(String texto) {
        return texto.replace("[", "[[]").replace("%", "[%]").replace("_", "[_]");
    }
}
