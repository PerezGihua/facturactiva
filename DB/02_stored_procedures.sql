USE facturactiva;
GO

-- SP: Validar Usuario
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

-- SP: Obtener Usuario By Email
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

-- 8.0 SP: Registrar Usuario
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

-- SP: Registrar Usuario
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
    
    SELECT 
        t.id_ticket,
        t.asunto,
        t.descripcion,
        t.numero_documento_rechazado,
        t.fecha_creacion,
        t.fecha_ultima_actualizacion,
        t.fecha_cierre,
        -- Información de catálogos
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

CREATE PROCEDURE sp_crear_ticket_con_archivo
    @id_usuario_cliente INT,
    @id_tipo_comprobante INT,
    @asunto VARCHAR(255),
    @descripcion TEXT,
    @numero_documento_rechazado VARCHAR(50) = NULL,
    @ruta_archivo VARCHAR(500) = NULL,
    @id_estado INT = 1, -- Por defecto: ABIERTO
    @id_prioridad INT = 2 -- Por defecto: MEDIA
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @nuevo_id INT;
    
    INSERT INTO Tickets (
        id_usuario_cliente,
        id_tipo_comprobante,
        asunto,
        descripcion,
        numero_documento_rechazado,
        ruta_archivo,
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
        @ruta_archivo,
        @id_estado,
        @id_prioridad,
        GETDATE(),
        GETDATE()
    );
    
    SET @nuevo_id = SCOPE_IDENTITY();
    
    -- Retornar el ticket creado
    SELECT 
        t.id_ticket,
        t.asunto,
        t.descripcion,
        t.numero_documento_rechazado,
        t.ruta_archivo,
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
    WHERE t.id_ticket = @nuevo_id;
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
*/