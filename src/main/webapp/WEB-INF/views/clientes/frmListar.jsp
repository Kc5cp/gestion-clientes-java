<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Lista de Clientes"/>
<c:set var="moduloActivo" value="clientes"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="encabezado-pagina">
    <div>
        <h2 id="tituloPagina">Lista de Clientes</h2>
        <p id="subtituloPagina" class="subtitulo">Busca, registra, modifica o elimina los clientes de la empresa.</p>
    </div>
    <a id="btnNuevoCliente" class="btn btn-primary"
       href="${pageContext.request.contextPath}/ClienteServlet?accion=nuevo">
        <svg class="icono" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>
        Nuevo Cliente
    </a>
</div>

<%-- Mensajes del servidor (exito / error) --%>
<c:if test="${not empty mensajeExito}">
    <div id="alertExito" class="alert alert-success alert-dismissible fade show" role="alert">
        <span id="alertExitoTexto"><c:out value="${mensajeExito}"/></span>
        <button id="btnCerrarAlertExito" type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
    </div>
</c:if>
<c:if test="${not empty mensajeError}">
    <div id="alertError" class="alert alert-danger alert-dismissible fade show" role="alert">
        <span id="alertErrorTexto"><c:out value="${mensajeError}"/></span>
        <button id="btnCerrarAlertError" type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
    </div>
</c:if>

<%-- Buscador: filtra por DNI, apellidos, nombres o correo --%>
<form id="formBuscar" class="tarjeta barra-busqueda" action="${pageContext.request.contextPath}/ClienteServlet"
      method="get" role="search">
    <label for="txtBuscar" class="visually-hidden">Buscar cliente</label>
    <div class="campo-busqueda">
        <svg class="icono" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             stroke-linecap="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>
        <input type="search" id="txtBuscar" name="q" class="form-control" maxlength="100"
               autocomplete="off" placeholder="Buscar por DNI, apellidos, nombres o correo"
               value="<c:out value='${consulta}'/>">
    </div>
    <button id="btnBuscar" type="submit" class="btn btn-primary">Buscar</button>
    <c:if test="${not empty consulta}">
        <a id="btnLimpiarBusqueda" class="btn btn-outline-secondary"
           href="${pageContext.request.contextPath}/ClienteServlet">Limpiar</a>
    </c:if>
</form>

<p id="resumenClientes" class="resumen">
    <c:choose>
        <c:when test="${empty consulta}">
            Clientes registrados: <strong id="totalClientes">${total}</strong>
        </c:when>
        <c:otherwise>
            Resultados para «<strong><c:out value="${consulta}"/></strong>»:
            <strong id="cantidadResultados">${cantidad}</strong> de
            <strong id="totalClientes">${total}</strong> clientes registrados
        </c:otherwise>
    </c:choose>
</p>

<div class="tarjeta tarjeta-tabla">
    <div class="table-responsive">
        <table id="tablaClientes" class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th scope="col">ID</th>
                    <th scope="col">DNI</th>
                    <th scope="col">Apellidos</th>
                    <th scope="col">Nombres</th>
                    <th scope="col">Correo</th>
                    <th scope="col">Celular</th>
                    <th scope="col">Dirección</th>
                    <th scope="col" class="text-center">Acciones</th>
                </tr>
            </thead>
            <tbody id="cuerpoTablaClientes">
                <c:forEach var="c" items="${lista}">
                    <%-- data-dni permite ubicar la fila por DNI: tr[data-dni='12345678'] --%>
                    <tr id="filaCliente-${c.idCliente}" data-dni="<c:out value='${c.documentoDni}'/>">
                        <td class="col-id">${c.idCliente}</td>
                        <td class="col-dni"><c:out value="${c.documentoDni}"/></td>
                        <td class="col-apellidos"><c:out value="${c.apellidos}"/></td>
                        <td class="col-nombres"><c:out value="${c.nombres}"/></td>
                        <td class="col-correo"><c:out value="${c.correoElectronico}"/></td>
                        <td class="col-celular"><c:out value="${c.numeroCelular}"/></td>
                        <td class="col-direccion"><c:out value="${c.direccionDomicilio}"/></td>
                        <td class="text-center text-nowrap">
                            <a id="btnEditar-${c.idCliente}" class="btn btn-outline-primary btn-sm btn-editar"
                               href="${pageContext.request.contextPath}/ClienteServlet?accion=editar&id=${c.idCliente}">
                                <svg class="icono" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                     stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 20h4L19 9a2.83 2.83 0 0 0-4-4L4 16v4z"/></svg>
                                Editar</a>
                            <button id="btnEliminar-${c.idCliente}" type="button"
                                    class="btn btn-outline-danger btn-sm btn-eliminar"
                                    data-bs-toggle="modal" data-bs-target="#modalEliminar"
                                    data-id="${c.idCliente}"
                                    data-nombre="<c:out value='${c.nombreCompleto}'/>">
                                <svg class="icono" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                                     stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 7h16M10 11v6M14 11v6M6 7l1 12a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-12M9 7V4h6v3"/></svg>
                                Eliminar</button>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty lista}">
                    <tr id="filaSinClientes" class="fila-vacia">
                        <td colspan="8" class="text-center">
                            <c:choose>
                                <c:when test="${empty consulta}">
                                    <p id="msgSinClientes" class="mb-2">No hay clientes registrados actualmente.</p>
                                    <a id="btnPrimerCliente" class="btn btn-success btn-sm"
                                       href="${pageContext.request.contextPath}/ClienteServlet?accion=nuevo">Registrar el primer cliente</a>
                                </c:when>
                                <c:otherwise>
                                    <p id="msgSinResultados" class="mb-0">No se encontraron clientes que coincidan con la búsqueda.</p>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<%-- Modal de confirmacion (reemplaza al confirm() nativo del navegador) --%>
<div id="modalEliminar" class="modal fade" tabindex="-1" aria-labelledby="modalEliminarTitulo" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <form id="formEliminar" action="${pageContext.request.contextPath}/ClienteServlet" method="post">
                <input type="hidden" name="accion" value="eliminar">
                <input type="hidden" id="inputEliminarId" name="id" value="">
                <div class="modal-header">
                    <h5 id="modalEliminarTitulo" class="modal-title">Eliminar cliente</h5>
                    <button id="btnCerrarModalEliminar" type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    ¿Seguro que desea eliminar al cliente
                    <strong id="modalEliminarNombre"></strong>?
                    Esta acción no se puede deshacer.
                </div>
                <div class="modal-footer">
                    <button id="btnCancelarEliminar" type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button id="btnConfirmarEliminar" type="submit" class="btn btn-danger">Sí, eliminar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    // Al abrir el modal, copia el id y el nombre del cliente de la fila seleccionada
    document.getElementById('modalEliminar').addEventListener('show.bs.modal', function (evento) {
        var boton = evento.relatedTarget;
        document.getElementById('inputEliminarId').value = boton.getAttribute('data-id');
        document.getElementById('modalEliminarNombre').textContent = boton.getAttribute('data-nombre');
    });
</script>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
