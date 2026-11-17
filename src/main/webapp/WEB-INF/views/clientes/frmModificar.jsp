<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Modificar Cliente"/>
<c:set var="moduloActivo" value="clientes"/>
<c:set var="accionFormulario" value="actualizar"/>
<c:set var="textoBoton" value="Guardar Cambios"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="encabezado-pagina">
    <div>
        <h2 id="tituloPagina">Modificar Cliente</h2>
        <p id="subtituloPagina" class="subtitulo">Cambia los datos que necesites y presiona Guardar Cambios.</p>
    </div>
</div>

<%@ include file="/WEB-INF/views/clientes/_formCliente.jspf" %>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
