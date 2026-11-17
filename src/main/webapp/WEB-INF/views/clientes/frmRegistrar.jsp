<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Registrar Cliente"/>
<c:set var="moduloActivo" value="nuevo"/>
<c:set var="accionFormulario" value="agregar"/>
<c:set var="textoBoton" value="Guardar Cliente"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="encabezado-pagina">
    <div>
        <h2 id="tituloPagina">Registrar Cliente</h2>
        <p id="subtituloPagina" class="subtitulo">Completa los datos del nuevo cliente y presiona Guardar Cliente.</p>
    </div>
</div>

<%@ include file="/WEB-INF/views/clientes/_formCliente.jspf" %>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
