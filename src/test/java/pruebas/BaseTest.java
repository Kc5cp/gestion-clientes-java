package pruebas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Configuracion comun para TODAS las pruebas (Clientes y Navegacion).
 *
 * Antes de cada prueba abre una ventana nueva de Chrome y, al terminar,
 * guarda una captura de pantalla en la carpeta "evidencias" y cierra Chrome.
 *
 * Tambien trae metodos de ayuda para no repetir codigo en cada prueba:
 * abrir una pagina, escribir en una caja, elegir una opcion, leer un mensaje, etc.
 */
public abstract class BaseTest {

    /** Direccion de la aplicacion en Tomcat. Cambiala si tu proyecto usa otro nombre o puerto. */
    protected static final String URL_BASE =
            System.getProperty("urlBase", "http://localhost:8080/GestionClientes");

    /** Pausa (en milisegundos) para que se pueda ver lo que hace Selenium. Usa 0 para ir rapido. */
    private static final long PAUSA_MS = Long.getLong("pausa", 600L);

    /** Tiempo maximo que se espera a que aparezca un elemento en la pagina. */
    private static final Duration TIEMPO_ESPERA = Duration.ofSeconds(10);

    /** Carpeta donde se guardan las capturas de pantalla de cada prueba. */
    private static final Path CARPETA_EVIDENCIAS = Paths.get("evidencias");

    /**
     * Oculta los avisos "Unable to find CDP implementation" que aparecen cuando Chrome
     * es mas nuevo que Selenium. No afectan a las pruebas.
     */
    private static final Logger LOG_SELENIUM = Logger.getLogger("org.openqa.selenium");

    static {
        LOG_SELENIUM.setLevel(Level.SEVERE);
    }

    protected WebDriver driver;
    protected WebDriverWait espera;

    // ================================================================ Ciclo de cada prueba

    @BeforeEach
    void abrirNavegador(TestInfo info) {
        System.out.println(">> Ejecutando: " + info.getDisplayName());

        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments("--start-maximized");
        if (Boolean.getBoolean("sinVentana")) {          // -DsinVentana=true para no mostrar Chrome
            opciones.addArguments("--headless=new", "--window-size=1366,768");
        }

        driver = new ChromeDriver(opciones);              // Selenium Manager descarga chromedriver solo
        espera = new WebDriverWait(driver, TIEMPO_ESPERA);
    }

    @AfterEach
    void cerrarNavegador(TestInfo info) {
        if (driver == null) {
            return;
        }
        try {
            pausa();
            guardarEvidencia(info);
        } finally {
            driver.quit();
        }
    }

    /** Guarda una captura de la pantalla final de la prueba (sirve como evidencia para el informe). */
    private void guardarEvidencia(TestInfo info) {
        String clase = info.getTestClass().map(Class::getSimpleName).orElse("Prueba");
        String archivo = clase + "_" + nombreDeArchivo(info.getDisplayName()) + ".png";
        try {
            Files.createDirectories(CARPETA_EVIDENCIAS);
            byte[] imagen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(CARPETA_EVIDENCIAS.resolve(archivo), imagen);
        } catch (IOException | WebDriverException e) {
            System.err.println("No se pudo guardar la captura " + archivo + ": " + e.getMessage());
        }
    }

    /** "CP-CLI-01 Registrar cliente válido" -> "CP-CLI-01_Registrar_cliente_valido" */
    private static String nombreDeArchivo(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.replaceAll("[^A-Za-z0-9-]+", "_").replaceAll("_+$", "");
    }

    // ================================================================ Metodos de ayuda

    /** Abre una pagina de la aplicacion. Ejemplo: abrir("/ClienteServlet"). */
    protected void abrir(String ruta) {
        driver.get(URL_BASE + ruta);
    }

    /** Busca un elemento por su id y espera a que se vea en pantalla. */
    protected WebElement elemento(String id) {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
    }

    /** Borra la caja de texto y escribe el texto indicado. */
    protected void escribir(String id, String texto) {
        WebElement caja = elemento(id);
        caja.clear();
        caja.sendKeys(texto);
    }

    /**
     * Escribe una fecha en un campo type="date".
     * Se usa JavaScript porque al teclear la fecha el formato depende del idioma de Windows.
     *
     * @param fechaIso fecha en formato yyyy-MM-dd (por ejemplo LocalDate.now().toString())
     */
    protected void escribirFecha(String id, String fechaIso) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", elemento(id), fechaIso);
    }

    /** Elige una opcion de una lista desplegable por su value. */
    protected void seleccionar(String id, String valor) {
        new Select(elemento(id)).selectByValue(valor);
    }

    /** Texto visible de un elemento (por ejemplo el de una alerta o un mensaje de error). */
    protected String texto(String id) {
        return elemento(id).getText().trim();
    }

    /** Valor actual de una caja de texto o lista desplegable. */
    protected String valor(String id) {
        String valor = espera.until(ExpectedConditions.presenceOfElementLocated(By.id(id))).getDomProperty("value");
        return valor == null ? "" : valor;
    }

    /** Indica si existe al menos un elemento que cumpla el selector. */
    protected boolean existe(By selector) {
        return !driver.findElements(selector).isEmpty();
    }

    /** Hace clic en el elemento con ese id y espera a que cargue la pagina siguiente. */
    protected void clicYEsperar(String id) {
        clicYEsperar(elemento(id));
    }

    /** Hace clic en un elemento (boton o enlace) y espera a que cargue la pagina siguiente. */
    protected void clicYEsperar(WebElement boton) {
        WebElement paginaActual = driver.findElement(By.tagName("html"));
        llevarALaVista(boton);
        pausa();
        espera.until(ExpectedConditions.elementToBeClickable(boton)).click();
        espera.until(ExpectedConditions.stalenessOf(paginaActual));
    }

    /**
     * Mueve la pagina hasta que el elemento quede en el centro de la pantalla.
     *
     * Bootstrap 5 activa el desplazamiento suave (scroll-behavior: smooth). Si un boton esta
     * mas abajo de lo que se ve, Selenium hace clic mientras la pagina aun se esta moviendo y
     * aparece el error "element click intercepted". Por eso primero se desactiva el
     * desplazamiento suave y se lleva el elemento a la vista de forma inmediata.
     */
    protected void llevarALaVista(WebElement elemento) {
        ((JavascriptExecutor) driver).executeScript(
                "document.documentElement.style.scrollBehavior = 'auto';"
                + "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", elemento);
    }

    /**
     * Abre el modal de eliminar de una fila y pulsa "Sí, eliminar" o "Cancelar".
     *
     * @param fila      fila de la tabla (tr) que se quiere eliminar
     * @param confirmar true = "Sí, eliminar"; false = "Cancelar"
     */
    protected void eliminarDesdeModal(WebElement fila, boolean confirmar) {
        WebElement botonEliminar = fila.findElement(By.cssSelector(".btn-eliminar"));
        llevarALaVista(botonEliminar);
        botonEliminar.click();

        WebElement modal = driver.findElement(By.id("modalEliminar"));
        espera.until(ExpectedConditions.attributeContains(modal, "class", "show"));
        espera.until(d -> "1".equals(modal.getCssValue("opacity")));    // termino la animacion
        pausa();

        if (confirmar) {
            clicYEsperar("btnConfirmarEliminar");
        } else {
            elemento("btnCancelarEliminar").click();
            espera.until(ExpectedConditions.invisibilityOf(modal));
        }
    }

    /** Numero aleatorio con la cantidad de digitos indicada (para no repetir DNI o codigos). */
    protected static String numeroAleatorio(int digitos) {
        long minimo = (long) Math.pow(10, digitos - 1);
        return String.valueOf(minimo + ThreadLocalRandom.current().nextLong(9 * minimo));
    }

    /** Pausa corta para que se vea lo que hace Selenium. */
    protected void pausa() {
        if (PAUSA_MS <= 0) {
            return;
        }
        try {
            Thread.sleep(PAUSA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
