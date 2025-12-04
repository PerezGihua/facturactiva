USE facturactiva;
GO

-- =============================================
-- VISTAS PARA REPORTES (CU6)
-- =============================================

-- 6.1 Vista: Tickets Activos por Agente
CREATE VIEW VW_TicketsPorAgente AS
SELECT 
    u.id_usuario,
    u.nombres + ' ' + ISNULL(u.apellidos, '') AS nombre_completo,
    COUNT(t.id_ticket) AS total_tickets,
    SUM(CASE WHEN e.nombre_estado = 'Nuevo' THEN 1 ELSE 0 END) AS tickets_nuevos,
    SUM(CASE WHEN e.nombre_estado = 'Asignado' THEN 1 ELSE 0 END) AS tickets_asignados,
    SUM(CASE WHEN e.nombre_estado = 'En Proceso (T cnico)' THEN 1 ELSE 0 END) AS tickets_en_proceso
FROM Usuarios u
LEFT JOIN Tickets t ON u.id_usuario = t.id_usuario_agente
LEFT JOIN Estados e ON t.id_estado = e.id_estado
WHERE u.id_rol = 3 -- Agente de Soporte
  AND t.fecha_cierre IS NULL
GROUP BY u.id_usuario, u.nombres, u.apellidos;
GO

-- 6.2 Vista: Tiempo Promedio de Soluci n
CREATE VIEW VW_TiempoPromedioSolucion AS
SELECT 
    AVG(DATEDIFF(HOUR, t.fecha_creacion, t.fecha_cierre)) AS horas_promedio,
    p.nombre_prioridad,
    COUNT(t.id_ticket) AS total_tickets_cerrados
FROM Tickets t
INNER JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
WHERE t.fecha_cierre IS NOT NULL
GROUP BY p.nombre_prioridad;
GO

-- 6.3 Vista: Resumen de Tickets por Cliente
CREATE VIEW VW_TicketsPorCliente AS
SELECT 
    u.id_usuario,
    u.nombres + ' ' + ISNULL(u.apellidos, '') AS nombre_completo,
    u.email,
    COUNT(t.id_ticket) AS total_tickets,
    SUM(CASE WHEN t.fecha_cierre IS NULL THEN 1 ELSE 0 END) AS tickets_abiertos,
    SUM(CASE WHEN t.fecha_cierre IS NOT NULL THEN 1 ELSE 0 END) AS tickets_cerrados
FROM Usuarios u
INNER JOIN Tickets t ON u.id_usuario = t.id_usuario_cliente
WHERE u.id_rol = 1 -- Cliente
GROUP BY u.id_usuario, u.nombres, u.apellidos, u.email;
GO

-- =============================================
-- STORED PROCEDURES PARA OPERACIONES CLAVE
-- =============================================

-- 7.1 SP: Crear Ticket (CU1)
CREATE PROCEDURE SP_CrearTicket
    @id_usuario_cliente INT,
    @id_tipo_comprobante INT,
    @asunto VARCHAR(255),
    @descripcion TEXT,
    @numero_documento_rechazado VARCHAR(50) = NULL,
    @id_prioridad INT = 2 -- Media por defecto
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Insertar el ticket
        INSERT INTO Tickets (
            id_usuario_cliente,
            id_estado,
            id_prioridad,
            id_tipo_comprobante,
            asunto,
            descripcion,
            numero_documento_rechazado
        )
        VALUES (
            @id_usuario_cliente,
            1, -- Estado: Nuevo
            @id_prioridad,
            @id_tipo_comprobante,
            @asunto,
            @descripcion,
            @numero_documento_rechazado
        );
        
        -- Registrar en historial
        DECLARE @id_ticket INT = SCOPE_IDENTITY();
        
        INSERT INTO HistorialTicket (id_ticket, id_usuario_afector, tipo_evento, detalle)
        VALUES (@id_ticket, @id_usuario_cliente, 'CREACION', 'Ticket creado por el cliente');
        
        COMMIT TRANSACTION;
        SELECT @id_ticket AS id_ticket_creado;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 7.2 SP: Asignar Ticket (CU2)
CREATE PROCEDURE SP_AsignarTicket
    @id_ticket INT,
    @id_usuario_agente INT,
    @id_usuario_jefe INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Actualizar el ticket
        UPDATE Tickets
        SET id_usuario_agente = @id_usuario_agente,
            id_usuario_jefe = @id_usuario_jefe,
            id_estado = 2 -- Asignado
        WHERE id_ticket = @id_ticket;
        
        -- Registrar en historial
        INSERT INTO HistorialTicket (id_ticket, id_usuario_afector, tipo_evento, detalle)
        VALUES (@id_ticket, @id_usuario_jefe, 'ASIGNACION', 
                'Ticket asignado al agente ID: ' + CAST(@id_usuario_agente AS VARCHAR));
        
        COMMIT TRANSACTION;
        SELECT 'Ticket asignado exitosamente' AS mensaje;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 7.3 SP: Cambiar Estado del Ticket
CREATE PROCEDURE SP_CambiarEstadoTicket
    @id_ticket INT,
    @id_estado_nuevo INT,
    @id_usuario_afector INT,
    @comentario_adicional VARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Obtener estado actual
        DECLARE @id_estado_actual INT;
        DECLARE @nombre_estado_actual VARCHAR(50);
        DECLARE @nombre_estado_nuevo VARCHAR(50);
        
        SELECT @id_estado_actual = t.id_estado, 
               @nombre_estado_actual = e.nombre_estado
        FROM Tickets t
        INNER JOIN Estados e ON t.id_estado = e.id_estado
        WHERE t.id_ticket = @id_ticket;
        
        SELECT @nombre_estado_nuevo = nombre_estado
        FROM Estados
        WHERE id_estado = @id_estado_nuevo;
        
        -- Actualizar el ticket
        UPDATE Tickets
        SET id_estado = @id_estado_nuevo,
            fecha_ultima_actualizacion = SYSDATETIMEOFFSET()
        WHERE id_ticket = @id_ticket;
        
        -- Si se está cerrando el ticket, actualizar fecha_cierre
        IF @id_estado_nuevo = 6 -- Cerrado (Solucionado)
        BEGIN
            UPDATE Tickets
            SET fecha_cierre = SYSDATETIMEOFFSET()
            WHERE id_ticket = @id_ticket;
        END
        
        -- Registrar en historial
        DECLARE @detalle VARCHAR(1000);
        SET @detalle = 'Estado cambiado de ' + @nombre_estado_actual + ' a ' + @nombre_estado_nuevo;
        
        IF @comentario_adicional IS NOT NULL
        BEGIN
            SET @detalle = @detalle + '. ' + @comentario_adicional;
        END
        
        INSERT INTO HistorialTicket (id_ticket, id_usuario_afector, tipo_evento, detalle)
        VALUES (@id_ticket, @id_usuario_afector, 'ESTADO_CAMBIADO', @detalle);
        
        COMMIT TRANSACTION;
        SELECT 'Estado del ticket actualizado exitosamente' AS mensaje;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 7.4 SP: Cerrar Ticket (CU1.2)
CREATE PROCEDURE SP_CerrarTicket
    @id_ticket INT,
    @id_usuario_cliente INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Verificar que el ticket pertenece al cliente
        IF EXISTS (SELECT 1 FROM Tickets WHERE id_ticket = @id_ticket AND id_usuario_cliente = @id_usuario_cliente)
        BEGIN
            -- Llamar al SP de cambiar estado para cerrar el ticket
            EXEC SP_CambiarEstadoTicket 
                @id_ticket = @id_ticket,
                @id_estado_nuevo = 6, -- Cerrado (Solucionado)
                @id_usuario_afector = @id_usuario_cliente,
                @comentario_adicional = 'Ticket cerrado por el cliente - Soluci n aceptada';
            
            SELECT 'Ticket cerrado exitosamente' AS mensaje;
        END
        ELSE
        BEGIN
            SELECT 'Error: El ticket no pertenece al cliente especificado' AS mensaje;
        END
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 7.5 SP: Agregar Comentario al Ticket
CREATE PROCEDURE SP_AgregarComentario
    @id_ticket INT,
    @id_usuario INT,
    @contenido TEXT,
    @tipo_comunicacion VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Insertar el comentario
        INSERT INTO Comentarios (id_ticket, id_usuario, contenido, tipo_comunicacion)
        VALUES (@id_ticket, @id_usuario, @contenido, @tipo_comunicacion);
        
        -- Actualizar fecha de última actualización del ticket
        UPDATE Tickets
        SET fecha_ultima_actualizacion = SYSDATETIMEOFFSET()
        WHERE id_ticket = @id_ticket;
        
        -- Registrar en historial
        DECLARE @id_comentario INT = SCOPE_IDENTITY();
        
        INSERT INTO HistorialTicket (id_ticket, id_usuario_afector, tipo_evento, detalle)
        VALUES (@id_ticket, @id_usuario, 'COMENTARIO_AGREGADO', 
                'Comentario agregado al ticket. Tipo: ' + ISNULL(@tipo_comunicacion, 'General'));
        
        COMMIT TRANSACTION;
        SELECT @id_comentario AS id_comentario_creado;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 7.6 SP: Validar Usuario
CREATE PROCEDURE SP_Validar_User
    @username VARCHAR(100),  -- email del usuario
    @psw VARCHAR(255)        -- password_hash
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Variables para los resultados
    DECLARE @id_rol INT;
    DECLARE @message VARCHAR(100);
    DECLARE @nombreUser VARCHAR(201);
    
    -- Buscar el usuario en la base de datos
    IF EXISTS (
        SELECT 1 FROM Usuarios u
        WHERE u.email = @username 
          AND u.password_hash = @psw 
          AND u.activo = 1
    )
    BEGIN
        -- Obtener los datos del usuario
        SELECT 
            @id_rol = u.id_rol,
            @nombreUser = u.nombres + ' ' + ISNULL(u.apellidos, '')
        FROM Usuarios u
        WHERE u.email = @username 
          AND u.password_hash = @psw 
          AND u.activo = 1;
        
        SET @message = 'Autenticación exitosa';
        
        -- Retornar los datos solicitados
        SELECT 
            @id_rol AS id_rol,
            @message AS message,
            @nombreUser AS nombreUser;
    END
    ELSE
    BEGIN
        -- Usuario no encontrado o credenciales incorrectas
        SET @id_rol = NULL;
        SET @nombreUser = NULL;
        SET @message = 'Usuario o contraseña incorrectos';
        
        -- Retornar los datos (con valores NULL para id_rol y nombreUser)
        SELECT 
            @id_rol AS id_rol,
            @message AS message,
            @nombreUser AS nombreUser;
    END
END;
GO

-- 7.7 SP: Obtener Tickets por Usuario
CREATE PROCEDURE SP_ObtenerTicketsPorUsuario
    @id_usuario INT,
    @rol_usuario INT
AS
BEGIN
    SET NOCOUNT ON;
    
    IF @rol_usuario = 1 -- Cliente
    BEGIN
        -- Cliente ve sus propios tickets
        SELECT 
            t.id_ticket,
            t.asunto,
            t.descripcion,
            e.nombre_estado,
            p.nombre_prioridad,
            tc.nombre_comprobante,
            t.fecha_creacion,
            t.fecha_ultima_actualizacion,
            t.fecha_cierre,
            a.nombres + ' ' + ISNULL(a.apellidos, '') AS nombre_agente
        FROM Tickets t
        INNER JOIN Estados e ON t.id_estado = e.id_estado
        INNER JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
        INNER JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante
        LEFT JOIN Usuarios a ON t.id_usuario_agente = a.id_usuario
        WHERE t.id_usuario_cliente = @id_usuario
        ORDER BY t.fecha_creacion DESC;
    END
    ELSE IF @rol_usuario = 3 -- Agente de Soporte
    BEGIN
        -- Agente ve tickets asignados a él
        SELECT 
            t.id_ticket,
            t.asunto,
            t.descripcion,
            e.nombre_estado,
            p.nombre_prioridad,
            tc.nombre_comprobante,
            t.fecha_creacion,
            t.fecha_ultima_actualizacion,
            t.fecha_cierre,
            c.nombres + ' ' + ISNULL(c.apellidos, '') AS nombre_cliente
        FROM Tickets t
        INNER JOIN Estados e ON t.id_estado = e.id_estado
        INNER JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
        INNER JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante
        INNER JOIN Usuarios c ON t.id_usuario_cliente = c.id_usuario
        WHERE t.id_usuario_agente = @id_usuario
        ORDER BY t.fecha_creacion DESC;
    END
    ELSE IF @rol_usuario = 2 -- Jefe de Soporte
    BEGIN
        -- Jefe ve todos los tickets
        SELECT 
            t.id_ticket,
            t.asunto,
            t.descripcion,
            e.nombre_estado,
            p.nombre_prioridad,
            tc.nombre_comprobante,
            t.fecha_creacion,
            t.fecha_ultima_actualizacion,
            t.fecha_cierre,
            c.nombres + ' ' + ISNULL(c.apellidos, '') AS nombre_cliente,
            a.nombres + ' ' + ISNULL(a.apellidos, '') AS nombre_agente
        FROM Tickets t
        INNER JOIN Estados e ON t.id_estado = e.id_estado
        INNER JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
        INNER JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante
        INNER JOIN Usuarios c ON t.id_usuario_cliente = c.id_usuario
        LEFT JOIN Usuarios a ON t.id_usuario_agente = a.id_usuario
        ORDER BY t.fecha_creacion DESC;
    END
    ELSE -- Administrador
    BEGIN
        -- Administrador ve todos los tickets
        SELECT 
            t.id_ticket,
            t.asunto,
            t.descripcion,
            e.nombre_estado,
            p.nombre_prioridad,
            tc.nombre_comprobante,
            t.fecha_creacion,
            t.fecha_ultima_actualizacion,
            t.fecha_cierre,
            c.nombres + ' ' + ISNULL(c.apellidos, '') AS nombre_cliente,
            a.nombres + ' ' + ISNULL(a.apellidos, '') AS nombre_agente
        FROM Tickets t
        INNER JOIN Estados e ON t.id_estado = e.id_estado
        INNER JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
        INNER JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante
        INNER JOIN Usuarios c ON t.id_usuario_cliente = c.id_usuario
        LEFT JOIN Usuarios a ON t.id_usuario_agente = a.id_usuario
        ORDER BY t.fecha_creacion DESC;
    END
END;
GO

-- 7.8 SP: Obtener Detalle de Ticket Completo
CREATE PROCEDURE SP_ObtenerDetalleTicket
    @id_ticket INT,
    @id_usuario INT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Verificar que el usuario tiene permiso para ver este ticket
    DECLARE @permiso BIT = 0;
    
    IF EXISTS (
        SELECT 1 
        FROM Tickets t
        INNER JOIN Usuarios u ON u.id_usuario = @id_usuario
        WHERE t.id_ticket = @id_ticket
          AND (
            t.id_usuario_cliente = @id_usuario OR
            t.id_usuario_agente = @id_usuario OR
            t.id_usuario_jefe = @id_usuario OR
            u.id_rol = 4 -- Administrador
          )
    )
    BEGIN
        SET @permiso = 1;
        
        -- Obtener información del ticket
        SELECT 
            t.id_ticket,
            t.asunto,
            t.descripcion,
            t.numero_documento_rechazado,
            e.nombre_estado,
            e.descripcion AS descripcion_estado,
            p.nombre_prioridad,
            tc.nombre_comprobante,
            t.fecha_creacion,
            t.fecha_ultima_actualizacion,
            t.fecha_cierre,
            -- Información del cliente
            c.nombres + ' ' + ISNULL(c.apellidos, '') AS nombre_cliente,
            c.email AS email_cliente,
            -- Información del agente
            a.nombres + ' ' + ISNULL(a.apellidos, '') AS nombre_agente,
            a.email AS email_agente,
            -- Información del jefe
            j.nombres + ' ' + ISNULL(j.apellidos, '') AS nombre_jefe,
            j.email AS email_jefe
        FROM Tickets t
        INNER JOIN Estados e ON t.id_estado = e.id_estado
        INNER JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
        INNER JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante
        INNER JOIN Usuarios c ON t.id_usuario_cliente = c.id_usuario
        LEFT JOIN Usuarios a ON t.id_usuario_agente = a.id_usuario
        LEFT JOIN Usuarios j ON t.id_usuario_jefe = j.id_usuario
        WHERE t.id_ticket = @id_ticket;
        
        -- Obtener comentarios del ticket
        SELECT 
            c.id_comentario,
            c.contenido,
            c.tipo_comunicacion,
            c.fecha_creacion,
            u.nombres + ' ' + ISNULL(u.apellidos, '') AS nombre_usuario,
            u.id_rol
        FROM Comentarios c
        INNER JOIN Usuarios u ON c.id_usuario = u.id_usuario
        WHERE c.id_ticket = @id_ticket
        ORDER BY c.fecha_creacion ASC;
        
        -- Obtener historial del ticket
        SELECT 
            h.tipo_evento,
            h.detalle,
            h.fecha_evento,
            u.nombres + ' ' + ISNULL(u.apellidos, '') AS nombre_usuario_afector
        FROM HistorialTicket h
        LEFT JOIN Usuarios u ON h.id_usuario_afector = u.id_usuario
        WHERE h.id_ticket = @id_ticket
        ORDER BY h.fecha_evento ASC;
    END
    ELSE
    BEGIN
        SELECT 'No tiene permisos para ver este ticket' AS mensaje_error;
    END
END;
GO

-- =============================================
-- PRUEBAS PARA LOS STORED PROCEDURES
-- =============================================

/* 
PRUEBA 1: SP_CrearTicket
Ejemplo de uso:
EXEC SP_CrearTicket 
    @id_usuario_cliente = 1,
    @id_tipo_comprobante = 1,
    @asunto = 'Error en factura electrónica',
    @descripcion = 'La factura no se está generando correctamente',
    @numero_documento_rechazado = 'F001-00012345',
    @id_prioridad = 3;
*/

/* 
PRUEBA 2: SP_AsignarTicket
Ejemplo de uso:
EXEC SP_AsignarTicket 
    @id_ticket = 1,
    @id_usuario_agente = 3,
    @id_usuario_jefe = 2;
*/

/* 
PRUEBA 3: SP_CambiarEstadoTicket
Ejemplo de uso:
EXEC SP_CambiarEstadoTicket 
    @id_ticket = 1,
    @id_estado_nuevo = 3, -- En Espera de Cliente
    @id_usuario_afector = 3,
    @comentario_adicional = 'Se necesita información adicional del cliente';
*/

/* 
PRUEBA 4: SP_CerrarTicket
Ejemplo de uso:
EXEC SP_CerrarTicket 
    @id_ticket = 1,
    @id_usuario_cliente = 1;
*/

/* 
PRUEBA 5: SP_AgregarComentario
Ejemplo de uso:
EXEC SP_AgregarComentario 
    @id_ticket = 1,
    @id_usuario = 3,
    @contenido = 'He revisado el documento y encontré el error en el JSON',
    @tipo_comunicacion = 'Técnico';
*/

/* 
PRUEBA 6: SP_Validar_User
Ejemplo de uso (usuario válido):
EXEC SP_Validar_User 
    @username = 'admin@facturactiva.com',
    @psw = 'YWRtaW&MM=';

Ejemplo de uso (usuario inválido):
EXEC SP_Validar_User 
    @username = 'noexiste@mail.com',
    @psw = 'password123';

-- La salida será:
-- id_rol | message                         | nombreUser
-- 4      | Autenticación exitosa           | Juan Carlos Administrador Sistema
-- NULL   | Usuario o contraseña incorrectos | NULL
*/

/* 
PRUEBA 7: SP_ObtenerTicketsPorUsuario
Ejemplo de uso:
EXEC SP_ObtenerTicketsPorUsuario 
    @id_usuario = 1,
    @rol_usuario = 1; -- Cliente
*/

/* 
PRUEBA 8: SP_ObtenerDetalleTicket
Ejemplo de uso:
EXEC SP_ObtenerDetalleTicket 
    @id_ticket = 1,
    @id_usuario = 1;
*/