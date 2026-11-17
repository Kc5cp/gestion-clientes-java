package Controlador;

import Modelo.Cliente;
import Modelo.ClienteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del modulo Clientes (registro, busqueda, modificacion y eliminacion).
 *
 * GET  /ClienteServlet                      -> lista de clientes
 * GET  /ClienteServlet?q=texto              -> lista filtrada (DNI, apellidos, nombres o correo)
 * GET  /ClienteServlet?accion=nuevo         -> formulario de registro
 * GET  /ClienteServlet?accion=editar&id=N   -> formulario de modificacion
 * POST /ClienteServlet  accion=agregar      -> registra (valida en servidor)
 * POST /ClienteServlet  accion=actualizar   -> modifica (valida en servidor)
 * POST /ClienteServlet  accion=eliminar     -> elimina (desde el modal de Bootstrap)
 *
 * Despues de cada operacion correcta se usa Post/Redirect/Get y el mensaje
 * viaja en la sesion (mensaje "flash") para mostrarse una sola vez.
 */
@WebServlet(name = "ClienteServlet", urlPatterns = {"/ClienteServlet"})
public class ClienteServlet extends HttpServlet {

    private static final String VISTA_LISTAR    = "/WEB-INF/views/clientes/frmListar.jsp";
    private static final String VISTA_REGISTRAR = "/WEB-INF/views/clientes/frmRegistrar.jsp";
    private static final String VISTA_MODIFICAR = "/WEB-INF/views/clientes/frmModificar.jsp";

    private static final String MSG_ERROR_BD =
            "No se pudo completar la operación por un error de base de datos. Inténtelo nuevamente.";

    private final ClienteDAO dao = new ClienteDAO();

    // ------------------------------------------------------------------ GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        switch (accion) {
            case "nuevo":
                request.setAttribute("cliente", new Cliente());
                request.getRequestDispatcher(VISTA_REGISTRAR).forward(request, response);
                break;
            case "editar":
                mostrarFormularioModificar(request, response);
                break;
            default:
                listar(request, response);
        }
    }

    // ----------------------------------------------------------------- POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        switch (accion) {
            case "agregar":
                guardar(request, response, false);
                break;
            case "actualizar":
                guardar(request, response, true);
                break;
            case "eliminar":
                eliminar(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/ClienteServlet");
        }
    }

    // ------------------------------------------------------------- Acciones
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        pasarMensajeFlash(request);

        // Texto del buscador (opcional)
        String consulta = limpiar(request.getParameter("q"));
        if (consulta.length() > 100) {
            consulta = consulta.substring(0, 100);
        }

        try {
            List<Cliente> lista = consulta.isEmpty() ? dao.listar() : dao.buscar(consulta);
            request.setAttribute("lista", lista);
            request.setAttribute("cantidad", lista.size());
            request.setAttribute("total", consulta.isEmpty() ? lista.size() : dao.contar());
        } catch (SQLException e) {
            log("Error al listar clientes", e);
            request.setAttribute("lista", new ArrayList<Cliente>());
            request.setAttribute("cantidad", 0);
            request.setAttribute("total", 0);
            request.setAttribute("mensajeError",
                    "No se pudo conectar con la base de datos. Verifique que SQL Server esté en ejecución.");
        }
        request.setAttribute("consulta", consulta);
        request.getRequestDispatcher(VISTA_LISTAR).forward(request, response);
    }

    private void mostrarFormularioModificar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = leerEntero(request.getParameter("id"));
        try {
            Cliente cliente = (id > 0) ? dao.obtenerPorId(id) : null;
            if (cliente == null) {
                redirigirConMensaje(request, response, "danger", "El cliente solicitado no existe.");
                return;
            }
            request.setAttribute("cliente", cliente);
            request.getRequestDispatcher(VISTA_MODIFICAR).forward(request, response);
        } catch (SQLException e) {
            log("Error al buscar cliente", e);
            redirigirConMensaje(request, response, "danger", MSG_ERROR_BD);
        }
    }

    /** Registra (esModificacion = false) o modifica (esModificacion = true) un cliente. */
    private void guardar(HttpServletRequest request, HttpServletResponse response, boolean esModificacion)
            throws ServletException, IOException {

        Cliente c = new Cliente();
        c.setIdCliente(esModificacion ? leerEntero(request.getParameter("id")) : 0);
        c.setDocumentoDni(limpiar(request.getParameter("dni")));
        c.setApellidos(compactar(request.getParameter("apellidos")));
        c.setNombres(compactar(request.getParameter("nombres")));
        c.setCorreoElectronico(limpiar(request.getParameter("correo")).toLowerCase());
        c.setNumeroCelular(limpiar(request.getParameter("celular")));
        c.setDireccionDomicilio(compactar(request.getParameter("direccion")));

        String vista = esModificacion ? VISTA_MODIFICAR : VISTA_REGISTRAR;

        try {
            if (esModificacion && (c.getIdCliente() <= 0 || dao.obtenerPorId(c.getIdCliente()) == null)) {
                redirigirConMensaje(request, response, "danger", "El cliente que intenta modificar no existe.");
                return;
            }

            // 1. Validaciones de formato
            Map<String, String> errores = validar(c);

            // 2. DNI duplicado (solo si el DNI tiene formato correcto)
            if (!errores.containsKey("dni") && dao.existeDni(c.getDocumentoDni(), c.getIdCliente())) {
                errores.put("dni", "El DNI " + c.getDocumentoDni() + " ya está registrado en otro cliente.");
            }

            if (!errores.isEmpty()) {
                reenviarConErrores(request, response, vista, c, errores,
                        "No se pudo guardar el cliente. Revise los campos marcados.");
                return;
            }

            // 3. Grabar
            boolean ok = esModificacion ? dao.actualizar(c) : dao.agregar(c);
            if (ok) {
                redirigirConMensaje(request, response, "success", esModificacion
                        ? "Cliente modificado correctamente."
                        : "Cliente registrado correctamente.");
            } else {
                redirigirConMensaje(request, response, "danger", "No se pudo guardar el cliente.");
            }

        } catch (SQLException e) {
            log("Error al guardar cliente", e);
            Map<String, String> errores = new LinkedHashMap<>();
            String mensaje = MSG_ERROR_BD;
            // 2627 / 2601 = violacion de UNIQUE en SQL Server (dos registros simultaneos con el mismo DNI)
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                errores.put("dni", "El DNI " + c.getDocumentoDni() + " ya está registrado en otro cliente.");
                mensaje = "No se pudo guardar el cliente. Revise los campos marcados.";
            }
            reenviarConErrores(request, response, vista, c, errores, mensaje);
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = leerEntero(request.getParameter("id"));
        try {
            if (id > 0 && dao.eliminar(id)) {
                redirigirConMensaje(request, response, "success", "Cliente eliminado correctamente.");
            } else {
                redirigirConMensaje(request, response, "danger",
                        "No se pudo eliminar: el cliente no existe.");
            }
        } catch (SQLException e) {
            log("Error al eliminar cliente", e);
            redirigirConMensaje(request, response, "danger", MSG_ERROR_BD);
        }
    }

    // ---------------------------------------------------------- Validacion
    /**
     * Validacion del lado del servidor.
     * Devuelve un mapa campo -> mensaje (vacio si todo es correcto).
     */
    private Map<String, String> validar(Cliente c) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (c.getDocumentoDni().isEmpty()) {
            errores.put("dni", "El DNI es obligatorio.");
        } else if (!c.getDocumentoDni().matches("\\d{8}")) {
            errores.put("dni", "El DNI debe tener exactamente 8 dígitos numéricos.");
        }

        if (c.getApellidos().isEmpty()) {
            errores.put("apellidos", "Los apellidos son obligatorios.");
        } else if (c.getApellidos().length() > 100) {
            errores.put("apellidos", "Los apellidos no deben superar los 100 caracteres.");
        } else if (!c.getApellidos().matches("[\\p{L} .'-]+")) {
            errores.put("apellidos", "Los apellidos solo deben contener letras.");
        }

        if (c.getNombres().isEmpty()) {
            errores.put("nombres", "Los nombres son obligatorios.");
        } else if (c.getNombres().length() > 100) {
            errores.put("nombres", "Los nombres no deben superar los 100 caracteres.");
        } else if (!c.getNombres().matches("[\\p{L} .'-]+")) {
            errores.put("nombres", "Los nombres solo deben contener letras.");
        }

        if (c.getCorreoElectronico().isEmpty()) {
            errores.put("correo", "El correo electrónico es obligatorio.");
        } else if (c.getCorreoElectronico().length() > 100
                || !c.getCorreoElectronico().matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) {
            errores.put("correo", "Ingrese un correo electrónico válido (ejemplo: nombre@dominio.com).");
        }

        if (c.getNumeroCelular().isEmpty()) {
            errores.put("celular", "El celular es obligatorio.");
        } else if (!c.getNumeroCelular().matches("\\d{9}")) {
            errores.put("celular", "El celular debe tener exactamente 9 dígitos numéricos.");
        }

        if (c.getDireccionDomicilio().isEmpty()) {
            errores.put("direccion", "La dirección es obligatoria.");
        } else if (c.getDireccionDomicilio().length() > 200) {
            errores.put("direccion", "La dirección no debe superar los 200 caracteres.");
        }

        return errores;
    }

    // ------------------------------------------------------------ Utilidades
    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /** Quita espacios al inicio y al final y reduce los espacios repetidos a uno solo. */
    private String compactar(String valor) {
        return limpiar(valor).replaceAll("\\s+", " ");
    }

    private int leerEntero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Vuelve al formulario conservando lo que el usuario escribio y los mensajes de error. */
    private void reenviarConErrores(HttpServletRequest request, HttpServletResponse response,
            String vista, Cliente c, Map<String, String> errores, String mensaje)
            throws ServletException, IOException {

        request.setAttribute("cliente", c);
        request.setAttribute("errores", errores);
        request.setAttribute("mensajeError", mensaje);
        request.getRequestDispatcher(vista).forward(request, response);
    }

    /** Guarda el mensaje en sesion y redirige a la lista (Post/Redirect/Get). */
    private void redirigirConMensaje(HttpServletRequest request, HttpServletResponse response,
            String tipo, String texto) throws IOException {

        HttpSession sesion = request.getSession();
        sesion.setAttribute("flashTipo", tipo);
        sesion.setAttribute("flashTexto", texto);
        response.sendRedirect(request.getContextPath() + "/ClienteServlet");
    }

    /** Pasa el mensaje de la sesion al request y lo borra, para que se muestre una sola vez. */
    private void pasarMensajeFlash(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("flashTexto") == null) {
            return;
        }
        String atributo = "success".equals(sesion.getAttribute("flashTipo")) ? "mensajeExito" : "mensajeError";
        request.setAttribute(atributo, sesion.getAttribute("flashTexto"));
        sesion.removeAttribute("flashTipo");
        sesion.removeAttribute("flashTexto");
    }
}
