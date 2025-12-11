USE facturactiva;
GO

CREATE PROCEDURE dbo.SP_Validar_User
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

CREATE PROCEDURE dbo.SP_ObtenerUsuarioPorEmail
    @p_email VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        id_usuario,
        id_rol,
        email,
        password_hash,
        nombres,
        apellidos,
        fecha_registro,
        activo
    FROM Usuarios
    WHERE email = @p_email 
      AND activo = 1;
END;

ALTER PROCEDURE sp_obtener_tickets_por_usuario
    @id_usuario_cliente INT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        t.id_ticket,
        t.asunto,
        t.descripcion,
        t.numero_documento_rechazado,
        t.ruta_archivo, -- NUEVO CAMPO
        t.fecha_creacion,
        t.fecha_ultima_actualizacion,
        t.fecha_cierre,
        e.nombre_estado,
        p.nombre_prioridad,
        tc.nombre_comprobante AS nombre_tipo_comprobante,
        ua.nombres AS nombre_agente
    FROM Tickets t
    LEFT JOIN Estados e ON t.id_estado = e.id_estado
    LEFT JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
    LEFT JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante
    LEFT JOIN Usuarios ua ON t.id_usuario_agente = ua.id_usuario
    WHERE t.id_usuario_cliente = @id_usuario_cliente
    ORDER BY t.fecha_creacion DESC;
END;
GO

CREATE PROCEDURE dbo.SP_RegistrarUsuario
    @p_id_rol INT,
    @p_email VARCHAR(255),
    @p_password_hash VARCHAR(255),
    @p_nombres VARCHAR(100),
    @p_apellidos VARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_id_usuario INT;
    DECLARE @v_message VARCHAR(255);
    DECLARE @v_error_code INT = 0;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- 1. Verificar si el email ya existe
        IF EXISTS (SELECT 1 FROM Usuarios WHERE email = @p_email)
        BEGIN
            SET @v_message = 'El email ya está registrado';
            SET @v_error_code = 1;
            
            SELECT 
                NULL AS id_usuario,
                @v_message AS message,
                @v_error_code AS error_code;
                
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- 2. Verificar que el rol existe
        IF NOT EXISTS (SELECT 1 FROM Roles WHERE id_rol = @p_id_rol)
        BEGIN
            SET @v_message = 'El rol especificado no existe';
            SET @v_error_code = 2;
            
            SELECT 
                NULL AS id_usuario,
                @v_message AS message,
                @v_error_code AS error_code;
                
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- 3. Insertar el nuevo usuario
        INSERT INTO Usuarios (
            id_rol,
            email,
            password_hash,
            nombres,
            apellidos,
            fecha_registro,
            activo
        )
        VALUES (
            @p_id_rol,
            @p_email,
            @p_password_hash,
            @p_nombres,
            @p_apellidos,
            SYSDATETIMEOFFSET(),
            1  -- Usuario activo por defecto
        );
        
        -- 4. Obtener el ID del usuario recién creado
        SET @v_id_usuario = SCOPE_IDENTITY();
        SET @v_message = 'Usuario registrado exitosamente';
        
        -- 5. Retornar el resultado exitoso
        SELECT 
            @v_id_usuario AS id_usuario,
            @v_message AS message,
            0 AS error_code;
        
        COMMIT TRANSACTION;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
            
        SELECT 
            NULL AS id_usuario,
            ERROR_MESSAGE() AS message,
            ERROR_NUMBER() AS error_code;
    END CATCH
END;
GO

CREATE PROCEDURE dbo.SP_VerificarEmailExiste
    @p_email VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @v_existe BIT;
    
    IF EXISTS (SELECT 1 FROM Usuarios WHERE email = @p_email)
        SET @v_existe = 1;
    ELSE
        SET @v_existe = 0;
    
    SELECT @v_existe AS existe;
END;
GO

CREATE PROCEDURE sp_obtener_tickets_por_usuario
    @id_usuario_cliente INT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- ⭐ PRIMERO: Actualizar prioridades según tiempo transcurrido
    EXEC sp_actualizar_prioridades_por_tiempo;
    
    -- Variable para almacenar el rol del usuario
    DECLARE @id_rol INT;
    
    -- Obtener el rol del usuario
    SELECT @id_rol = id_rol 
    FROM Usuarios 
    WHERE id_usuario = @id_usuario_cliente;
    
    -- Consulta dinámica según el rol
    SELECT 
        t.id_ticket,
        t.asunto,
        t.descripcion,
        t.numero_documento_rechazado,
        t.fecha_creacion,
        t.fecha_ultima_actualizacion,
        t.fecha_cierre,
        t.activo,
        -- ⭐ Obtener el NOMBRE del primer archivo adjunto (si existe)
        (
            SELECT TOP 1 nombre_archivo 
            FROM ArchivosAdjuntos 
            WHERE id_ticket = t.id_ticket 
            ORDER BY fecha_subida ASC
        ) AS nombre_archivo,
        -- ⭐ Obtener la RUTA del primer archivo adjunto (si existe)
        (
            SELECT TOP 1 ruta_almacenamiento 
            FROM ArchivosAdjuntos 
            WHERE id_ticket = t.id_ticket 
            ORDER BY fecha_subida ASC
        ) AS ruta_archivo,
        -- IDs de catálogos
        t.id_estado,
        t.id_prioridad,
        t.id_tipo_comprobante,
        -- Información de catálogos (nombres)
        e.nombre_estado,
        p.nombre_prioridad,
        tc.nombre_comprobante AS nombre_tipo_comprobante,
        ua.nombres AS nombre_agente,
        uc.nombres AS nombre_cliente,
        uj.nombres AS nombre_jefe,
        -- Información adicional útil
        DATEDIFF(DAY, t.fecha_creacion, GETDATE()) AS dias_transcurridos
    FROM Tickets t
    LEFT JOIN Estados e ON t.id_estado = e.id_estado
    LEFT JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
    LEFT JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante
    LEFT JOIN Usuarios ua ON t.id_usuario_agente = ua.id_usuario
    LEFT JOIN Usuarios uc ON t.id_usuario_cliente = uc.id_usuario
    LEFT JOIN Usuarios uj ON t.id_usuario_jefe = uj.id_usuario
    WHERE 
        CASE 
            WHEN @id_rol = 1 THEN t.id_usuario_cliente  -- Cliente
            WHEN @id_rol = 2 THEN t.id_usuario_jefe     -- Jefe de Soporte
            WHEN @id_rol = 3 THEN t.id_usuario_agente   -- Agente de Soporte
            ELSE t.id_usuario_cliente
        END = @id_usuario_cliente
        AND t.activo = 1
    ORDER BY 
        p.nivel DESC,
        t.fecha_creacion DESC;
END;
GO

CREATE PROCEDURE sp_crear_ticket_con_archivo     
    @id_usuario_cliente INT,     
    @id_tipo_comprobante INT,     
    @asunto VARCHAR(255),     
    @descripcion TEXT,     
    @numero_documento_rechazado VARCHAR(50) = NULL,     
    @ruta_archivo VARCHAR(500) = NULL,     
    @nombre_archivo VARCHAR(255) = NULL,     
    @id_estado INT = 1, 
    @id_prioridad INT = 1 
AS 
BEGIN     
    SET NOCOUNT ON;
    DECLARE @nuevo_id INT;
    
    -- Insertar el nuevo ticket
    INSERT INTO Tickets (         
        id_usuario_cliente,         
        id_tipo_comprobante,         
        asunto,         
        descripcion,         
        numero_documento_rechazado,         
        id_estado,         
        id_prioridad,         
        fecha_creacion,         
        fecha_ultima_actualizacion     
    )     
    VALUES (         
        @id_usuario_cliente,         
        @id_tipo_comprobante,         
        @asunto,         
        @descripcion,         
        @numero_documento_rechazado,         
        @id_estado,         
        @id_prioridad,         
        GETDATE(),         
        GETDATE()     
    );
    
    SET @nuevo_id = SCOPE_IDENTITY();
    
    -- Si hay un archivo, insertarlo en ArchivosAdjuntos
    IF @ruta_archivo IS NOT NULL AND @ruta_archivo != ''     
    BEGIN         
        -- Si no se proporcionó nombre_archivo, extraerlo de la ruta
        IF @nombre_archivo IS NULL OR @nombre_archivo = ''         
        BEGIN             
            SET @nombre_archivo = REVERSE(SUBSTRING(REVERSE(@ruta_archivo), 1, CHARINDEX('\', REVERSE(@ruta_archivo)) - 1));         
        END
        
        INSERT INTO ArchivosAdjuntos (             
            id_ticket,             
            id_usuario,             
            nombre_archivo,             
            ruta_almacenamiento,             
            es_correccion,             
            fecha_subida         
        )         
        VALUES (             
            @nuevo_id,             
            @id_usuario_cliente,             
            @nombre_archivo,             
            @ruta_archivo,             
            0,             
            GETDATE()         
        );     
    END
    
    -- Registrar en historial
    INSERT INTO HistorialTicket (         
        id_ticket,         
        id_usuario_afector,         
        tipo_evento,         
        detalle,         
        fecha_evento     
    )     
    VALUES (         
        @nuevo_id,         
        @id_usuario_cliente,         
        'CREACION_TICKET',         
        'Ticket creado por el cliente',         
        GETDATE()     
    );
    
    -- Asignación automática
    EXEC sp_asignar_agente_automatico @id_ticket = @nuevo_id;
    
    -- ⭐ RETORNAR EL TICKET CON nombre_archivo Y ruta_archivo
    SELECT          
        t.id_ticket,         
        t.asunto,         
        t.descripcion,         
        t.numero_documento_rechazado,         
        -- ⭐ Obtener nombre_archivo desde ArchivosAdjuntos
        (             
            SELECT TOP 1 nombre_archivo              
            FROM ArchivosAdjuntos              
            WHERE id_ticket = t.id_ticket              
            ORDER BY fecha_subida ASC         
        ) AS nombre_archivo,
        -- ⭐ Obtener ruta_archivo desde ArchivosAdjuntos
        (             
            SELECT TOP 1 ruta_almacenamiento              
            FROM ArchivosAdjuntos              
            WHERE id_ticket = t.id_ticket              
            ORDER BY fecha_subida ASC         
        ) AS ruta_archivo,         
        t.fecha_creacion,         
        t.fecha_ultima_actualizacion,         
        t.fecha_cierre,         
        e.nombre_estado,         
        p.nombre_prioridad,         
        tc.nombre_comprobante AS nombre_tipo_comprobante,         
        ua.nombres AS nombre_agente,         
        uc.nombres AS nombre_cliente     
    FROM Tickets t     
    LEFT JOIN Estados e ON t.id_estado = e.id_estado     
    LEFT JOIN Prioridades p ON t.id_prioridad = p.id_prioridad     
    LEFT JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante     
    LEFT JOIN Usuarios ua ON t.id_usuario_agente = ua.id_usuario     
    LEFT JOIN Usuarios uc ON t.id_usuario_cliente = uc.id_usuario     
    WHERE t.id_ticket = @nuevo_id; 
END; 
GO

CREATE PROCEDURE sp_asignar_agente_automatico
    @id_ticket INT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @id_agente_asignado INT;
    DECLARE @ticket_existe BIT = 0;
    DECLARE @ticket_activo BIT = 0;
    DECLARE @ticket_ya_asignado BIT = 0;
    DECLARE @agentes_disponibles INT = 0;
    
    -- =============================================
    -- VALIDACIÓN 1: Verificar que el ticket existe y está activo
    -- =============================================
    SELECT 
        @ticket_existe = 1,
        @ticket_activo = activo,
        @ticket_ya_asignado = CASE WHEN id_usuario_agente IS NOT NULL THEN 1 ELSE 0 END
    FROM Tickets
    WHERE id_ticket = @id_ticket;
    
    IF @ticket_existe = 0
    BEGIN
        PRINT 'ERROR: El ticket ' + CAST(@id_ticket AS VARCHAR) + ' no existe';
        RETURN;
    END
    
    IF @ticket_activo = 0
    BEGIN
        PRINT 'ERROR: El ticket ' + CAST(@id_ticket AS VARCHAR) + ' está inactivo/eliminado';
        RETURN;
    END
    
    IF @ticket_ya_asignado = 1
    BEGIN
        PRINT 'AVISO: El ticket ' + CAST(@id_ticket AS VARCHAR) + ' ya tiene un agente asignado';
        -- Podrías decidir si continuar o no, por ahora continuamos
    END
    
    -- =============================================
    -- VALIDACIÓN 2: Verificar que existen agentes activos disponibles
    -- =============================================
    SELECT @agentes_disponibles = COUNT(*)
    FROM Usuarios
    WHERE id_rol = 3 -- Agente de Soporte
        AND activo = 1;
    
    IF @agentes_disponibles = 0
    BEGIN
        PRINT 'ERROR: No hay agentes de soporte activos disponibles';
        
        -- Registrar en historial el intento fallido
        INSERT INTO HistorialTicket (
            id_ticket,
            id_usuario_afector,
            tipo_evento,
            detalle,
            fecha_evento
        )
        VALUES (
            @id_ticket,
            NULL,
            'ASIGNACION_FALLIDA',
            'No se pudo asignar: No hay agentes de soporte activos disponibles',
            GETDATE()
        );
        
        RETURN;
    END
    
    -- =============================================
    -- VALIDACIÓN 3: Buscar el agente con menos carga de trabajo
    -- =============================================
    SELECT TOP 1 
        @id_agente_asignado = u.id_usuario
    FROM Usuarios u
    WHERE u.id_rol = 3 -- Agente de Soporte
        AND u.activo = 1 -- ⭐ Solo agentes activos
    ORDER BY (
        SELECT COUNT(*) 
        FROM Tickets t 
        WHERE t.id_usuario_agente = u.id_usuario 
            AND t.id_estado NOT IN (5, 6) -- No contar tickets cerrados o con propuesta enviada
            AND t.activo = 1 -- ⭐ Solo contar tickets activos
    ) ASC, u.id_usuario ASC; -- En caso de empate, tomar el de menor ID
    
    -- =============================================
    -- VALIDACIÓN 4: Verificar que se encontró un agente
    -- =============================================
    IF @id_agente_asignado IS NULL
    BEGIN
        PRINT 'ERROR: No se pudo encontrar un agente disponible';
        
        INSERT INTO HistorialTicket (
            id_ticket,
            id_usuario_afector,
            tipo_evento,
            detalle,
            fecha_evento
        )
        VALUES (
            @id_ticket,
            NULL,
            'ASIGNACION_FALLIDA',
            'No se pudo encontrar un agente disponible',
            GETDATE()
        );
        
        RETURN;
    END
    
    -- =============================================
    -- ASIGNACIÓN: Todo validado, proceder a asignar
    -- =============================================
    UPDATE Tickets
    SET 
        id_usuario_agente = @id_agente_asignado,
        id_estado = 2, -- Cambiar a "Asignado"
        fecha_ultima_actualizacion = GETDATE()
    WHERE id_ticket = @id_ticket
        AND activo = 1; -- ⭐ Doble verificación de que está activo
    
    -- Verificar que el UPDATE fue exitoso
    IF @@ROWCOUNT = 0
    BEGIN
        PRINT 'ERROR: No se pudo actualizar el ticket ' + CAST(@id_ticket AS VARCHAR);
        RETURN;
    END
    
    -- Registrar en historial la asignación exitosa
    INSERT INTO HistorialTicket (
        id_ticket,
        id_usuario_afector,
        tipo_evento,
        detalle,
        fecha_evento
    )
    VALUES (
        @id_ticket,
        @id_agente_asignado,
        'ASIGNACION_AUTOMATICA',
        'Ticket asignado automáticamente al agente con menor carga de trabajo',
        GETDATE()
    );
    
    PRINT 'ÉXITO: Ticket ' + CAST(@id_ticket AS VARCHAR) + ' asignado al agente ' + CAST(@id_agente_asignado AS VARCHAR);
END;
GO

CREATE PROCEDURE sp_actualizar_prioridades_por_tiempo
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @tickets_actualizados INT = 0;
    
    CREATE TABLE #TicketsActualizados (
        id_ticket INT,
        prioridad_anterior INT,
        prioridad_nueva INT,
        razon VARCHAR(255)
    );
    
    -- Tickets para PRIORIDAD 3 (ALTA)
    INSERT INTO #TicketsActualizados (id_ticket, prioridad_anterior, prioridad_nueva, razon)
    SELECT 
        id_ticket,
        id_prioridad AS prioridad_anterior,
        3 AS prioridad_nueva,
        'Han pasado 2 o más días sin atención'
    FROM Tickets
    WHERE 
        id_prioridad < 3
        AND id_estado NOT IN (5, 6)
        AND activo = 1 -- ⭐ Solo tickets activos
        AND DATEDIFF(DAY, fecha_creacion, GETDATE()) >= 2
        AND (
            fecha_ultima_actualizacion IS NULL 
            OR DATEDIFF(DAY, fecha_creacion, fecha_ultima_actualizacion) < 1
        );
    
    UPDATE t
    SET 
        t.id_prioridad = 3,
        t.fecha_ultima_actualizacion = GETDATE()
    FROM Tickets t
    INNER JOIN #TicketsActualizados tmp ON t.id_ticket = tmp.id_ticket
    WHERE tmp.prioridad_nueva = 3;
    
    SET @tickets_actualizados = @@ROWCOUNT;
    
    INSERT INTO HistorialTicket (id_ticket, id_usuario_afector, tipo_evento, detalle, fecha_evento)
    SELECT 
        id_ticket, NULL, 'CAMBIO_PRIORIDAD_AUTOMATICO',
        'Prioridad cambiada de ' + CAST(prioridad_anterior AS VARCHAR) + ' a ALTA (3) - ' + razon,
        GETDATE()
    FROM #TicketsActualizados
    WHERE prioridad_nueva = 3;
    
    -- Tickets para PRIORIDAD 2 (MEDIA)
    INSERT INTO #TicketsActualizados (id_ticket, prioridad_anterior, prioridad_nueva, razon)
    SELECT 
        id_ticket,
        id_prioridad AS prioridad_anterior,
        2 AS prioridad_nueva,
        'Ha pasado 1 día sin atención'
    FROM Tickets
    WHERE 
        id_prioridad < 2
        AND id_estado NOT IN (5, 6)
        AND activo = 1 -- ⭐ Solo tickets activos
        AND DATEDIFF(DAY, fecha_creacion, GETDATE()) >= 1
        AND DATEDIFF(DAY, fecha_creacion, GETDATE()) < 2
        AND (
            fecha_ultima_actualizacion IS NULL 
            OR DATEDIFF(DAY, fecha_creacion, fecha_ultima_actualizacion) < 1
        );
    
    UPDATE t
    SET 
        t.id_prioridad = 2,
        t.fecha_ultima_actualizacion = GETDATE()
    FROM Tickets t
    INNER JOIN #TicketsActualizados tmp ON t.id_ticket = tmp.id_ticket
    WHERE tmp.prioridad_nueva = 2;
    
    SET @tickets_actualizados = @tickets_actualizados + @@ROWCOUNT;
    
    INSERT INTO HistorialTicket (id_ticket, id_usuario_afector, tipo_evento, detalle, fecha_evento)
    SELECT 
        id_ticket, NULL, 'CAMBIO_PRIORIDAD_AUTOMATICO',
        'Prioridad cambiada de ' + CAST(prioridad_anterior AS VARCHAR) + ' a MEDIA (2) - ' + razon,
        GETDATE()
    FROM #TicketsActualizados
    WHERE prioridad_nueva = 2;
    
    DROP TABLE #TicketsActualizados;
    
    PRINT 'Se actualizaron ' + CAST(@tickets_actualizados AS VARCHAR) + ' tickets';
END;
GO

CREATE PROCEDURE sp_eliminar_ticket
    @id_ticket INT,
    @id_usuario INT -- Usuario que realiza la eliminación
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @ticket_existe BIT = 0;
    DECLARE @ticket_activo BIT = 0;
    
    -- Verificar si el ticket existe y está activo
    SELECT 
        @ticket_existe = 1,
        @ticket_activo = activo
    FROM Tickets
    WHERE id_ticket = @id_ticket;
    
    -- Validaciones
    IF @ticket_existe = 0
    BEGIN
        SELECT 
            0 AS success,
            'El ticket no existe' AS message;
        RETURN;
    END
    
    IF @ticket_activo = 0
    BEGIN
        SELECT 
            0 AS success,
            'El ticket ya está eliminado' AS message;
        RETURN;
    END
    
    -- Desactivar el ticket
    UPDATE Tickets
    SET 
        activo = 0,
        fecha_ultima_actualizacion = GETDATE()
    WHERE id_ticket = @id_ticket;
    
    -- Registrar en el historial
    INSERT INTO HistorialTicket (
        id_ticket,
        id_usuario_afector,
        tipo_evento,
        detalle,
        fecha_evento
    )
    VALUES (
        @id_ticket,
        @id_usuario,
        'TICKET_ELIMINADO',
        'Ticket desactivado por el usuario',
        GETDATE()
    );
    
    -- Retornar éxito
    SELECT 
        1 AS success,
        'Ticket eliminado exitosamente' AS message,
        @id_ticket AS id_ticket;
END;
GO

CREATE PROCEDURE sp_obtener_detalle_ticket
    @id_ticket INT,
    @id_usuario INT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Validar que el ticket existe y está activo
    IF NOT EXISTS (SELECT 1 FROM Tickets WHERE id_ticket = @id_ticket AND activo = 1)
    BEGIN
        -- En lugar de RAISERROR, retornar resultado vacío
        SELECT NULL AS id_ticket, 'Ticket no encontrado' AS mensaje WHERE 1=0;
        RETURN;
    END
    
    -- Validar que el usuario tiene acceso al ticket
    IF NOT EXISTS (
        SELECT 1 FROM Tickets 
        WHERE id_ticket = @id_ticket 
        AND (id_usuario_cliente = @id_usuario OR id_usuario_agente = @id_usuario)
    )
    BEGIN
        -- En lugar de RAISERROR, retornar resultado vacío
        SELECT NULL AS id_ticket, 'Sin permisos para ver este ticket' AS mensaje WHERE 1=0;
        RETURN;
    END
    
    -- 1. Información principal del ticket
    SELECT 
        t.id_ticket,
        t.asunto,
        t.numero_documento_rechazado AS numeroDocumento,
        t.descripcion,
        t.fecha_creacion AS fechaCreacion,
        e.nombre_estado AS estado,
        t.id_estado AS idEstado,
        p.nombre_prioridad AS prioridad,
        t.id_prioridad AS idPrioridad,
        tc.nombre_comprobante AS tipoComprobante,
        uc.nombres AS nombreCliente,
        ua.nombres AS nombreAgente,
        t.id_usuario_agente AS idUsuarioAgente
    FROM Tickets t
    LEFT JOIN Estados e ON t.id_estado = e.id_estado
    LEFT JOIN Prioridades p ON t.id_prioridad = p.id_prioridad
    LEFT JOIN TiposComprobante tc ON t.id_tipo_comprobante = tc.id_comprobante
    LEFT JOIN Usuarios uc ON t.id_usuario_cliente = uc.id_usuario
    LEFT JOIN Usuarios ua ON t.id_usuario_agente = ua.id_usuario
    WHERE t.id_ticket = @id_ticket;
    
    -- 2. Archivos adjuntos
    SELECT 
        aa.id_archivo AS idArchivo,
        aa.nombre_archivo AS nombreArchivo,
        aa.ruta_almacenamiento AS rutaAlmacenamiento,
        aa.es_correccion AS esCorreccion,
        aa.fecha_subida AS fechaSubida,
        u.nombres AS nombreUsuario
    FROM ArchivosAdjuntos aa
    LEFT JOIN Usuarios u ON aa.id_usuario = u.id_usuario
    WHERE aa.id_ticket = @id_ticket
    ORDER BY aa.fecha_subida ASC;
    
    -- 3. Comentarios
    SELECT 
        c.id_comentario AS idComentario,
        c.contenido,
        c.fecha_creacion AS fechaCreacion,
        u.nombres AS nombreUsuario,
        c.id_usuario AS idUsuario,
        CASE 
            WHEN u.id_rol = 2 THEN 'AGENTE'
            ELSE 'CLIENTE'
        END AS tipoUsuario
    FROM Comentarios c
    LEFT JOIN Usuarios u ON c.id_usuario = u.id_usuario
    WHERE c.id_ticket = @id_ticket
    ORDER BY c.fecha_creacion ASC;
    
    -- 4. Respuestas a comentarios
    SELECT 
        rc.id_respuesta AS idRespuesta,
        rc.id_comentario AS idComentario,
        rc.contenido,
        rc.fecha_creacion AS fechaCreacion,
        u.nombres AS nombreUsuario,
        rc.id_usuario AS idUsuario,
        CASE 
            WHEN u.id_rol = 2 THEN 'AGENTE'
            ELSE 'CLIENTE'
        END AS tipoUsuario
    FROM RespuestasComentarios rc
    LEFT JOIN Usuarios u ON rc.id_usuario = u.id_usuario
    WHERE rc.id_comentario IN (
        SELECT id_comentario FROM Comentarios WHERE id_ticket = @id_ticket
    )
    ORDER BY rc.fecha_creacion ASC;
END;
GO

CREATE PROCEDURE sp_agregar_comentario_ticket
    @id_ticket INT,
    @id_usuario INT,
    @contenido TEXT,
    @id_comentario_padre INT = NULL  -- NULL = comentario nuevo, valor = respuesta
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @id_comentario_nuevo INT;
    DECLARE @id_respuesta_nueva INT;
    DECLARE @es_cliente BIT = 0;
    DECLARE @id_agente_actual INT;
    DECLARE @nuevo_estado INT;
    DECLARE @nueva_prioridad INT;
    DECLARE @nombre_estado VARCHAR(50);
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Validar que el ticket existe y está activo
        IF NOT EXISTS (SELECT 1 FROM Tickets WHERE id_ticket = @id_ticket AND activo = 1)
        BEGIN
            RAISERROR('El ticket no existe o ha sido eliminado', 16, 1);
            RETURN;
        END
        
        -- Validar que el usuario tiene acceso al ticket
        IF NOT EXISTS (
            SELECT 1 FROM Tickets 
            WHERE id_ticket = @id_ticket 
            AND (id_usuario_cliente = @id_usuario OR id_usuario_agente = @id_usuario)
        )
        BEGIN
            RAISERROR('No tiene permisos para comentar en este ticket', 16, 1);
            RETURN;
        END
        
        -- Verificar si el usuario es cliente o agente
        SELECT @es_cliente = CASE 
            WHEN id_usuario_cliente = @id_usuario THEN 1 
            ELSE 0 
        END,
        @id_agente_actual = id_usuario_agente
        FROM Tickets 
        WHERE id_ticket = @id_ticket;
        
        -- Si es una respuesta a un comentario
        IF @id_comentario_padre IS NOT NULL
        BEGIN
            -- Validar que el comentario padre existe
            IF NOT EXISTS (SELECT 1 FROM Comentarios WHERE id_comentario = @id_comentario_padre)
            BEGIN
                RAISERROR('El comentario padre no existe', 16, 1);
                RETURN;
            END
            
            -- Insertar respuesta
            INSERT INTO RespuestasComentarios (
                id_comentario,
                id_usuario,
                contenido,
                fecha_creacion
            )
            VALUES (
                @id_comentario_padre,
                @id_usuario,
                @contenido,
                SYSDATETIMEOFFSET()
            );
            
            SET @id_respuesta_nueva = SCOPE_IDENTITY();
            
            -- Registrar en historial
            INSERT INTO HistorialTicket (
                id_ticket,
                id_usuario_afector,
                tipo_evento,
                detalle,
                fecha_evento
            )
            VALUES (
                @id_ticket,
                @id_usuario,
                'RESPUESTA_COMENTARIO',
                'Respuesta agregada al comentario #' + CAST(@id_comentario_padre AS VARCHAR),
                GETDATE()
            );
        END
        ELSE
        BEGIN
            -- Insertar comentario nuevo
            INSERT INTO Comentarios (
                id_ticket,
                id_usuario,
                contenido,
                fecha_creacion
            )
            VALUES (
                @id_ticket,
                @id_usuario,
                @contenido,
                SYSDATETIMEOFFSET()
            );
            
            SET @id_comentario_nuevo = SCOPE_IDENTITY();
            
            -- Registrar en historial
            INSERT INTO HistorialTicket (
                id_ticket,
                id_usuario_afector,
                tipo_evento,
                detalle,
                fecha_evento
            )
            VALUES (
                @id_ticket,
                @id_usuario,
                'COMENTARIO_AGREGADO',
                'Comentario agregado al ticket',
                GETDATE()
            );
        END
        
        -- Si el usuario es CLIENTE, actualizar estado y prioridad
        IF @es_cliente = 1
        BEGIN
            -- Cambiar estado a ASIGNADO (id_estado = 2)
            SET @nuevo_estado = 2;
            
            -- Cambiar prioridad a ALTA (id_prioridad = 3)
            SET @nueva_prioridad = 3;
            
            -- Actualizar el ticket
            UPDATE Tickets
            SET 
                id_estado = @nuevo_estado,
                id_prioridad = @nueva_prioridad,
                fecha_ultima_actualizacion = GETDATE(),
                -- Mantener el mismo agente si ya tiene uno asignado
                id_usuario_agente = ISNULL(@id_agente_actual, id_usuario_agente)
            WHERE id_ticket = @id_ticket;
            
            -- Registrar cambio de estado en historial
            INSERT INTO HistorialTicket (
                id_ticket,
                id_usuario_afector,
                tipo_evento,
                detalle,
                fecha_evento
            )
            VALUES (
                @id_ticket,
                @id_usuario,
                'CAMBIO_ESTADO',
                'Estado cambiado a ASIGNADO por comentario del cliente',
                GETDATE()
            );
            
            -- Registrar cambio de prioridad en historial
            INSERT INTO HistorialTicket (
                id_ticket,
                id_usuario_afector,
                tipo_evento,
                detalle,
                fecha_evento
            )
            VALUES (
                @id_ticket,
                @id_usuario,
                'CAMBIO_PRIORIDAD',
                'Prioridad cambiada a ALTA por comentario del cliente',
                GETDATE()
            );
        END
        
        -- Obtener el nombre del estado actual
        SELECT @nombre_estado = nombre_estado 
        FROM Estados e
        JOIN Tickets t ON e.id_estado = t.id_estado
        WHERE t.id_ticket = @id_ticket;
        
        COMMIT TRANSACTION;
        
        -- Retornar resultado
        SELECT 
            1 AS success,
            'Comentario agregado exitosamente' AS message,
            @id_comentario_nuevo AS idComentario,
            @id_respuesta_nueva AS idRespuesta,
            @nombre_estado AS estadoTicket;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrorMessage, 16, 1);
    END CATCH
END;
GO
-- =============================================
-- PRUEBAS PARA LOS STORED PROCEDURES
-- =============================================
/* 
PRUEBA SP_Validar_User:
EXEC SP_Validar_User 
    @username = 'admin@facturactiva.com',
    @psw = 'YWRtaW&MM=';

PRUEBA SP_ObtenerUsuarioPorEmail:
EXEC SP_ObtenerUsuarioPorEmail @p_email = 'cliente@facturactiva.com';

PRUEBA SP_RegistrarUsuario:
EXEC SP_RegistrarUsuario 
    @p_id_rol = 1,
    @p_email = 'test@facturactiva.com',
    @p_password_hash = @test_hash,
    @p_nombres = 'Usuario',
    @p_apellidos = 'Prueba';

PRUEBA SP_VerificarEmailExiste:
EXEC SP_VerificarEmailExiste @p_email = 'test@facturactiva.com';

PRUEBA sp_obtener_tickets_por_usuario
DECLARE @id_cliente_test INT, @id_agente_test INT, @id_jefe_test INT;
SELECT @id_cliente_test = id_usuario FROM Usuarios WHERE email = 'erickquispe@facturactiva.com';
EXEC sp_obtener_tickets_por_usuario @id_usuario_cliente = @id_cliente_test;

PRINT '=== Probando SP con IDs adicionales ===';
EXEC sp_obtener_tickets_por_usuario @id_usuario_cliente = 8;
GO
*/