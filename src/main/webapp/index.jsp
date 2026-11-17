<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    // El sistema solo tiene el modulo Clientes: la pagina de inicio es la lista de clientes.
    response.sendRedirect(request.getContextPath() + "/ClienteServlet");
%>
