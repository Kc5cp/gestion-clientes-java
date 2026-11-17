package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Pruebas del menu superior y de los botones que mueven al usuario entre pantallas. */
@DisplayName("Navegación del sistema")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NavegacionTest extends BaseTest {

    @Test
    @Order(1)
    @DisplayName("CP-NAV-01 La dirección raíz del sistema abre la lista de clientes")
    void raizAbreLaLista() {
        abrir("/");
        espera.until(ExpectedConditions.urlContains("/ClienteServlet"));

        assertEquals("Lista de Clientes", texto("tituloPagina"));
    }

    @Test
    @Order(2)
    @DisplayName("CP-NAV-02 El menú Registrar cliente abre el formulario de registro")
    void menuRegistrarCliente() {
        abrir("/ClienteServlet");
        clicYEsperar("navNuevoCliente");

        assertEquals("Registrar Cliente", texto("tituloPagina"));
        assertTrue(existe(By.id("formCliente")), "Debe verse el formulario");
    }

    @Test
    @Order(3)
    @DisplayName("CP-NAV-03 El menú Clientes vuelve a la lista")
    void menuClientes() {
        abrir("/ClienteServlet?accion=nuevo");
        clicYEsperar("navClientes");

        assertEquals("Lista de Clientes", texto("tituloPagina"));
    }

    @Test
    @Order(4)
    @DisplayName("CP-NAV-04 El nombre del sistema (logo) vuelve a la lista")
    void logoVuelveALaLista() {
        abrir("/ClienteServlet?accion=nuevo");
        clicYEsperar("navInicio");

        assertEquals("Lista de Clientes", texto("tituloPagina"));
    }

    @Test
    @Order(5)
    @DisplayName("CP-NAV-05 El menú marca la opción en la que estás")
    void menuMarcaLaOpcionActiva() {
        abrir("/ClienteServlet");
        assertTrue(elemento("navClientes").getDomProperty("className").contains("active"),
                "En la lista, 'Clientes' debe estar marcado");
        assertFalse(elemento("navNuevoCliente").getDomProperty("className").contains("active"),
                "En la lista, 'Registrar cliente' no debe estar marcado");

        clicYEsperar("navNuevoCliente");
        assertTrue(elemento("navNuevoCliente").getDomProperty("className").contains("active"),
                "En el formulario, 'Registrar cliente' debe estar marcado");
        assertFalse(elemento("navClientes").getDomProperty("className").contains("active"),
                "En el formulario, 'Clientes' no debe estar marcado");
    }

    @Test
    @Order(6)
    @DisplayName("CP-NAV-06 Cancelar en el formulario vuelve a la lista sin guardar")
    void cancelarNoGuarda() {
        String dni = numeroAleatorio(8);
        abrir("/ClienteServlet?accion=nuevo");
        escribir("txtDni", dni);
        escribir("txtApellidos", "No Debe Guardarse");
        clicYEsperar("btnCancelar");

        assertEquals("Lista de Clientes", texto("tituloPagina"));
        assertFalse(existe(By.cssSelector("tr[data-dni='" + dni + "']")),
                "El cliente no debe haberse guardado");
    }
}
