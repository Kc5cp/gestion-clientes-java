# Gestión de Clientes (Java Web)

Sistema web para registrar, buscar, modificar y eliminar clientes. Hecho con Jakarta EE 10 (Servlets y JSP), Tomcat 10.1, SQL Server y JDK 11.

## Cómo ejecutarlo
1. En SQL Server, ejecutar `database/GestionClientes.sql`.
2. En `src/main/java/Modelo/Conexion.java`, poner tu usuario y contraseña de SQL Server.
3. Abrir el proyecto en NetBeans, hacer Clean and Build y luego Run.
4. Entrar a http://localhost:8080/GestionClientes

## Pruebas automatizadas (Selenium)
21 pruebas (15 de Clientes y 6 de navegación) en `src/test/java/pruebas`, con Selenium 4.44.0 y JUnit 5.
Con el sistema encendido: clic derecho en el proyecto > Test.

## Documentación
- Informe: `docs/informe`
- Diagramas de actividades: `docs/diagramas`
- Capturas de las pruebas: `docs/evidencias`

Autora: Katty 
