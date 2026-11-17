package pruebas;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Pruebas del modulo Clientes (registro, busqueda, modificacion y eliminacion).
 *
 * Las pruebas se ejecutan EN ORDEN y se apoyan unas en otras:
 *   - CP-CLI-01 registra el cliente A y CP-CLI-08 registra el cliente B.
 *   - Las pruebas 09 a 13 trabajan con ellos.
 *   - CP-CLI-14 elimina al cliente A y CP-CLI-15 elimina al cliente B,
 *     asi la base de datos queda igual que antes de empezar.
 *
 * Los DNI son aleatorios para poder repetir las pruebas sin chocar con datos existentes.
 * Los clientes reales que ya tengas en la tabla no se tocan.
 */
@DisplayName("Módulo Clientes")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClienteTest extends BaseTest {

    /** DNI del cliente A (se registra en CP-CLI-01). */
    private static final String DNI_A = numeroAleatorio(8);

    /** DNI del cliente B (se registra en CP-CLI-08); siempre distinto al del cliente A. */
    private static final String DNI_B = dniDistintoDe(DNI_A);

    private static final String MSG_REVISE = "No se pudo guardar el cliente. Revise los campos marcados.";

    // ---------------------------------------------------------------- ayudas

    private static String dniDistintoDe(String otro) {
        String dni = numeroAleatorio(8);
        while (dni.equals(otro)) {
            dni = numeroAleatorio(8);
        }
        return dni;
    }

    /** Fila de la tabla de un cliente (se ubica por su DNI). */
    private By fila(String dni) {
        return By.cssSelector("tr[data-dni='" + dni + "']");
    }

    /** Todas las filas de clientes que se ven en la tabla. */
    private List<WebElement> filasDeLaTabla() {
        return driver.findElements(By.cssSelector("#cuerpoTablaClientes tr[id^='filaCliente-']"));
    }

    private void abrirFormularioNuevo() {
        abrir("/ClienteServlet?accion=nuevo");
    }

    private void llenarFormulario(String dni, String apellidos, String nombres,
                                  String correo, String celular, String direccion) {
        escribir("txtDni", dni);
        escribir("txtApellidos", apellidos);
        escribir("txtNombres", nombres);
        escribir("txtCorreo", correo);
        escribir("txtCelular", celular);
        escribir("txtDireccion", direccion);
    }

    /** Llena el formulario con datos validos, cambiando solo el DNI y el celular. */
    private void llenarConDniYCelular(String dni, String celular) {
        llenarFormulario(dni, "Prueba Selenium", "Usuario Automatico",
                "selenium@prueba.com", celular, "Av. Las Pruebas 123");
    }

    /** Texto exacto guardado en una celda (getText() juntaria los espacios repetidos, esto no). */
    private String textoExacto(String dni, String clase) {
        return driver.findElement(fila(dni)).findElement(By.cssSelector("." + clase))
                .getDomProperty("textContent").trim();
    }

    // ---------------------------------------------------------------- pruebas

    @Test
    @Order(1)
    @DisplayName("CP-CLI-01 Registrar cliente con datos válidos")
    void registrarClienteValido() {
        abrir("/ClienteServlet");
        clicYEsperar("btnNuevoCliente");

        llenarConDniYCelular(DNI_A, "987654321");
        clicYEsperar("btnGuardar");

        assertEquals("Cliente registrado correctamente.", texto("alertExitoTexto"));
        assertTrue(existe(fila(DNI_A)), "El cliente nuevo debe aparecer en la lista");
    }

    @Test
    @Order(2)
    @DisplayName("CP-CLI-02 No permite registrar un DNI duplicado")
    void noPermiteDniDuplicado() {
        abrirFormularioNuevo();
        llenarConDniYCelular(DNI_A, "987654321");          // mismo DNI de CP-CLI-01
        clicYEsperar("btnGuardar");

        assertEquals(MSG_REVISE, texto("alertErrorTexto"));
        assertEquals("El DNI " + DNI_A + " ya está registrado en otro cliente.", texto("errorDni"));
    }

    @Test
    @Order(3)
    @DisplayName("CP-CLI-03 DNI con 7 dígitos muestra error y conserva los datos")
    void dniConSieteDigitos() {
        abrirFormularioNuevo();
        llenarConDniYCelular("1234567", "987654321");
        clicYEsperar("btnGuardar");

        assertEquals(MSG_REVISE, texto("alertErrorTexto"));
        assertEquals("El DNI debe tener exactamente 8 dígitos numéricos.", texto("errorDni"));
        // Lo que el usuario escribio no se pierde
        assertEquals("Prueba Selenium", valor("txtApellidos"));
        assertEquals("selenium@prueba.com", valor("txtCorreo"));
    }

    @Test
    @Order(4)
    @DisplayName("CP-CLI-04 DNI con letras muestra error")
    void dniConLetras() {
        abrirFormularioNuevo();
        llenarConDniYCelular("1234567A", "987654321");
        clicYEsperar("btnGuardar");

        assertEquals("El DNI debe tener exactamente 8 dígitos numéricos.", texto("errorDni"));
    }

    @Test
    @Order(5)
    @DisplayName("CP-CLI-05 Celular con 8 dígitos muestra error")
    void celularConOchoDigitos() {
        abrirFormularioNuevo();
        llenarConDniYCelular(numeroAleatorio(8), "98765432");
        clicYEsperar("btnGuardar");

        assertEquals(MSG_REVISE, texto("alertErrorTexto"));
        assertEquals("El celular debe tener exactamente 9 dígitos numéricos.", texto("errorCelular"));
    }

    @Test
    @Order(6)
    @DisplayName("CP-CLI-06 Correo sin formato válido muestra error")
    void correoInvalido() {
        abrirFormularioNuevo();
        llenarFormulario(numeroAleatorio(8), "Prueba Selenium", "Usuario Automatico",
                "correo-sin-arroba", "987654321", "Av. Las Pruebas 123");
        clicYEsperar("btnGuardar");

        assertEquals("Ingrese un correo electrónico válido (ejemplo: nombre@dominio.com).",
                texto("errorCorreo"));
    }

    @Test
    @Order(7)
    @DisplayName("CP-CLI-07 Formulario vacío muestra todos los campos obligatorios")
    void formularioVacio() {
        abrirFormularioNuevo();
        clicYEsperar("btnGuardar");

        assertAll(
                () -> assertEquals(MSG_REVISE, texto("alertErrorTexto")),
                () -> assertEquals("El DNI es obligatorio.", texto("errorDni")),
                () -> assertEquals("Los apellidos son obligatorios.", texto("errorApellidos")),
                () -> assertEquals("Los nombres son obligatorios.", texto("errorNombres")),
                () -> assertEquals("El correo electrónico es obligatorio.", texto("errorCorreo")),
                () -> assertEquals("El celular es obligatorio.", texto("errorCelular")),
                () -> assertEquals("La dirección es obligatoria.", texto("errorDireccion")));
    }

    @Test
    @Order(8)
    @DisplayName("CP-CLI-08 Registrar quita espacios repetidos y guarda el correo en minúsculas")
    void registrarNormalizaLosDatos() {
        abrirFormularioNuevo();
        llenarFormulario(DNI_B, "  Quispe   Rojas ", "Maria   Luz",
                "MARIA.LUZ@Correo.COM", "912345670", "Jr.   Lima    45");
        clicYEsperar("btnGuardar");

        assertEquals("Cliente registrado correctamente.", texto("alertExitoTexto"));
        assertAll(
                () -> assertEquals("Quispe Rojas", textoExacto(DNI_B, "col-apellidos")),
                () -> assertEquals("Maria Luz", textoExacto(DNI_B, "col-nombres")),
                () -> assertEquals("maria.luz@correo.com", textoExacto(DNI_B, "col-correo")),
                () -> assertEquals("Jr. Lima 45", textoExacto(DNI_B, "col-direccion")));
    }

    @Test
    @Order(9)
    @DisplayName("CP-CLI-09 Buscar por DNI muestra solo al cliente buscado")
    void buscarPorDni() {
        abrir("/ClienteServlet");
        escribir("txtBuscar", DNI_A);
        clicYEsperar("btnBuscar");

        List<WebElement> filas = filasDeLaTabla();
        assertEquals(1, filas.size(), "Solo debe aparecer un cliente");
        assertEquals(DNI_A, filas.get(0).findElement(By.cssSelector(".col-dni")).getText().trim());
        assertEquals("1", texto("cantidadResultados"));
        assertEquals(DNI_A, valor("txtBuscar"), "El buscador conserva lo que se escribió");
        assertTrue(existe(By.id("btnLimpiarBusqueda")), "Debe aparecer el botón Limpiar");
    }

    @Test
    @Order(10)
    @DisplayName("CP-CLI-10 Buscar algo que no existe muestra mensaje y Limpiar restablece la lista")
    void buscarSinResultadosYLimpiar() {
        abrir("/ClienteServlet");
        escribir("txtBuscar", "sin-coincidencia-" + numeroAleatorio(6));
        clicYEsperar("btnBuscar");

        assertEquals("No se encontraron clientes que coincidan con la búsqueda.", texto("msgSinResultados"));
        assertEquals("0", texto("cantidadResultados"));
        assertTrue(filasDeLaTabla().isEmpty(), "No debe haber ninguna fila");

        clicYEsperar("btnLimpiarBusqueda");

        assertTrue(existe(fila(DNI_A)), "Al limpiar vuelve el cliente A");
        assertTrue(existe(fila(DNI_B)), "Al limpiar vuelve el cliente B");
        assertFalse(existe(By.id("msgSinResultados")), "Ya no debe verse el mensaje de sin resultados");
        assertEquals("", valor("txtBuscar"), "El buscador queda vacío");
        assertTrue(Integer.parseInt(texto("totalClientes")) >= 2,
                "El contador debe contar al menos a los dos clientes de prueba");
    }

    @Test
    @Order(11)
    @DisplayName("CP-CLI-11 Modificar el celular del cliente")
    void modificarCliente() {
        abrir("/ClienteServlet");
        WebElement filaA = driver.findElement(fila(DNI_A));
        clicYEsperar(filaA.findElement(By.cssSelector(".btn-editar")));

        assertEquals("Modificar Cliente", texto("tituloPagina"));
        assertEquals(DNI_A, valor("txtDni"), "El formulario debe cargar los datos del cliente");

        escribir("txtCelular", "912345678");
        clicYEsperar("btnGuardar");

        assertEquals("Cliente modificado correctamente.", texto("alertExitoTexto"));
        assertEquals("912345678", textoExacto(DNI_A, "col-celular"));
    }

    @Test
    @Order(12)
    @DisplayName("CP-CLI-12 No permite modificar usando el DNI de otro cliente")
    void noPermiteDniDeOtroClienteAlModificar() {
        abrir("/ClienteServlet");
        WebElement filaA = driver.findElement(fila(DNI_A));
        clicYEsperar(filaA.findElement(By.cssSelector(".btn-editar")));

        escribir("txtDni", DNI_B);                       // el DNI del cliente B
        clicYEsperar("btnGuardar");

        assertEquals(MSG_REVISE, texto("alertErrorTexto"));
        assertEquals("El DNI " + DNI_B + " ya está registrado en otro cliente.", texto("errorDni"));

        // El cliente A no cambió
        abrir("/ClienteServlet");
        assertTrue(existe(fila(DNI_A)), "El cliente A debe conservar su DNI");
    }

    @Test
    @Order(13)
    @DisplayName("CP-CLI-13 Cancelar en el modal no elimina el cliente")
    void cancelarEliminacion() {
        abrir("/ClienteServlet");
        eliminarDesdeModal(driver.findElement(fila(DNI_A)), false);

        assertTrue(existe(fila(DNI_A)), "El cliente debe seguir en la lista");
    }

    @Test
    @Order(14)
    @DisplayName("CP-CLI-14 Confirmar en el modal elimina el cliente")
    void eliminarCliente() {
        abrir("/ClienteServlet");
        eliminarDesdeModal(driver.findElement(fila(DNI_A)), true);

        assertEquals("Cliente eliminado correctamente.", texto("alertExitoTexto"));
        assertFalse(existe(fila(DNI_A)), "El cliente ya no debe estar en la lista");
    }

    @Test
    @Order(15)
    @DisplayName("CP-CLI-15 Eliminar el cliente B de la prueba CP-CLI-08 (limpieza)")
    void eliminarClienteDeLimpieza() {
        abrir("/ClienteServlet");
        eliminarDesdeModal(driver.findElement(fila(DNI_B)), true);

        assertEquals("Cliente eliminado correctamente.", texto("alertExitoTexto"));
        assertFalse(existe(fila(DNI_B)), "El cliente B ya no debe estar en la lista");
    }
}
