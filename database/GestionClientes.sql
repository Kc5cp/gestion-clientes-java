/* =====================================================================
   Script de base de datos - Sistema de Gestion de Clientes
   Base de datos: GestionClientes   (la misma de tu script IS LB8.sql)

   Diferencia con IS LB8.sql: este script NO borra la tabla. Si Cliente ya
   existe conserva tus datos y solo agrega las reglas que falten
   (DNI unico, DNI de 8 digitos y celular de 9 digitos).
   Se puede ejecutar varias veces sin problema.
   ===================================================================== */

-- 1. Crear la base de datos si no existe
IF DB_ID('GestionClientes') IS NULL
    CREATE DATABASE GestionClientes;
GO

USE GestionClientes;
GO

-- 2. Dar acceso al login de la aplicacion (el mismo de Conexion.java).
--    Si el login ya es administrador (sysadmin) o dueno de la base,
--    este bloque no hace nada.
BEGIN TRY
    IF EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'kccp')
       AND NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'kccp')
    BEGIN
        CREATE USER kccp FOR LOGIN kccp;
        ALTER ROLE db_owner ADD MEMBER kccp;
    END
END TRY
BEGIN CATCH
    PRINT 'Aviso: no se creo el usuario kccp en GestionClientes (probablemente ya tiene acceso).';
END CATCH
GO

-- 3. Tabla Cliente (mismas columnas de IS LB8.sql)
IF OBJECT_ID('dbo.Cliente', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Cliente (
        idCliente          INT IDENTITY(1,1) PRIMARY KEY,
        documentoDni       VARCHAR(8)   NOT NULL,
        apellidos          VARCHAR(100) NOT NULL,
        nombres            VARCHAR(100) NOT NULL,
        correoElectronico  VARCHAR(100) NOT NULL,
        numeroCelular      VARCHAR(9)   NOT NULL,
        direccionDomicilio VARCHAR(200) NOT NULL
    );
    PRINT 'Tabla Cliente creada.';
END
ELSE
    PRINT 'La tabla Cliente ya existe: se conservan sus datos.';
GO

-- 4. Reglas en la base de datos (respaldo de las validaciones del servidor).
--    Si algun dato actual las incumple, se avisa y se sigue con la siguiente.

-- 4.1 DNI unico (no puede haber dos clientes con el mismo DNI)
IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = 'UQ_Cliente_Dni' AND object_id = OBJECT_ID('dbo.Cliente'))
BEGIN
    BEGIN TRY
        ALTER TABLE dbo.Cliente ADD CONSTRAINT UQ_Cliente_Dni UNIQUE (documentoDni);
    END TRY
    BEGIN CATCH
        PRINT 'Aviso: no se pudo crear UQ_Cliente_Dni. Hay DNI repetidos: borra o corrige los duplicados y vuelve a ejecutar.';
    END CATCH
END
GO

-- 4.2 DNI: exactamente 8 digitos
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name = 'CK_Cliente_Dni' AND parent_object_id = OBJECT_ID('dbo.Cliente'))
BEGIN
    BEGIN TRY
        ALTER TABLE dbo.Cliente ADD CONSTRAINT CK_Cliente_Dni
            CHECK (documentoDni LIKE REPLICATE('[0-9]', 8));
    END TRY
    BEGIN CATCH
        PRINT 'Aviso: no se pudo crear CK_Cliente_Dni. Hay DNI que no tienen 8 digitos: corrigelos y vuelve a ejecutar.';
    END CATCH
END
GO

-- 4.3 Celular: exactamente 9 digitos
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name = 'CK_Cliente_Celular' AND parent_object_id = OBJECT_ID('dbo.Cliente'))
BEGIN
    BEGIN TRY
        ALTER TABLE dbo.Cliente ADD CONSTRAINT CK_Cliente_Celular
            CHECK (numeroCelular LIKE REPLICATE('[0-9]', 9));
    END TRY
    BEGIN CATCH
        PRINT 'Aviso: no se pudo crear CK_Cliente_Celular. Hay celulares que no tienen 9 digitos: corrigelos y vuelve a ejecutar.';
    END CATCH
END
GO

-- 5. (Opcional) Datos de ejemplo para ver la lista con informacion.
--    Quita los comentarios del bloque de abajo (las marcas de apertura y cierre) y ejecuta solo ese bloque.
/*
INSERT INTO dbo.Cliente (documentoDni, apellidos, nombres, correoElectronico, numeroCelular, direccionDomicilio)
VALUES ('73388307', 'Cespedes Panduro', 'Katty Carolina', 'kacespedespa@ucvvirtual.edu.pe', '947041172', 'Prolg. Almirante Grau 120, Moyobamba'),
       ('45781236', 'Ramirez Torres',    'Luis Alberto',   'lramirez@correo.com',            '912345678', 'Av. Grau 450, Moyobamba'),
       ('70214589', 'Paredes Ruiz',      'Ana Lucia',      'aparedes@correo.com',            '987654321', 'Jr. Junin 128, Moyobamba');
*/

-- 6. Verificar el resultado
SELECT * FROM dbo.Cliente;
GO
