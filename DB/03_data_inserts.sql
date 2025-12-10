USE facturactiva;
GO

-- =============================================
-- 4. INSERCIÓN DE DATOS INICIALES
-- =============================================

-- 4.1 INSERCIÓN INICIAL: ROLES
INSERT INTO Roles (nombre_rol) VALUES
('Cliente'),
('Jefe de Soporte'),
('Agente de Soporte'),
('Administrador');
GO

-- 4.2 INSERCIÓN INICIAL: ESTADOS
INSERT INTO Estados (nombre_estado, descripcion) VALUES
('Nuevo', 'Ticket recién creado, pendiente de asignación.'),
('Asignado', 'Ticket en manos de un Agente, iniciando el análisis.'),
('En Espera de Cliente', 'Agente solicitó más información al cliente.'),
('En Proceso (Técnico)', 'Agente realizando correcciones (JSON, Postman).'),
('Propuesta Enviada', 'Solución enviada al Cliente para aceptación.'),
('Cerrado (Solucionado)', 'Cliente aceptó la solución.');
GO

-- 4.3 INSERCIÓN INICIAL: PRIORIDADES
INSERT INTO Prioridades (nombre_prioridad, nivel) VALUES
('Baja', 1),
('Media', 2),
('Alta', 3);
GO

-- 4.4 INSERCIÓN INICIAL: TIPOS DE COMPROBANTE
INSERT INTO TiposComprobante (nombre_comprobante) VALUES
('Factura'),
('Boleta de Venta'),
('Nota de Crédito'),
('Nota de Débito'),
('Guía de Remisión - Remitente'),
('Guía de Remisión - Transportista');
GO

-- ===========================  ==================
-- 9. INSERTS DE USUARIOS
-- =============================================
INSERT INTO usuarios (id_rol, email, password_hash, nombres, apellidos, fecha_registro, activo) VALUES
(1, 'cliente@facturactiva.com', 'Y2xpZW50ZTEyMw==', 'Juan Carlos', 'Quispe', SYSDATETIME(), 1), -- cliente123
(2, 'jefeSoporte@facturactiva.com', 'amVmZTEyMw==', 'Maria Elena', 'Mamani', SYSDATETIME(), 1), -- jefe123
(3, 'soporte@facturactiva.com', 'c29wb3J0ZTEyMw==', 'Carlos Alberto', 'Pérez', SYSDATETIME(), 1); -- soporte123
GO

-- Tickets
-- Factura rechazada
INSERT INTO Tickets (id_usuario_cliente, id_estado, id_prioridad, id_tipo_comprobante, asunto, descripcion, numero_documento_rechazado, fecha_creacion, fecha_ultima_actualizacion)
VALUES ((SELECT id_usuario FROM Usuarios WHERE email = 'miguelperez@facturactiva.com'), 1, 3, 1, 'Factura rechazada por SUNAT', 'Error 2335 en factura F001-00001234', 'F001-00001234', GETDATE(), GETDATE());
-- Consulta sobre anulación
INSERT INTO Tickets (id_usuario_cliente, id_estado, id_prioridad, id_tipo_comprobante, asunto, descripcion, fecha_creacion, fecha_ultima_actualizacion)
VALUES ((SELECT id_usuario FROM Usuarios WHERE email = 'miguelperez@facturactiva.com'), 1, 2, 2, 'Consulta sobre anulación de boleta', '¿Cómo puedo anular una boleta emitida hace 2 días?', GETDATE(), GETDATE());
-- Problema con emisión
INSERT INTO Tickets (id_usuario_cliente, id_estado, id_prioridad, id_tipo_comprobante, asunto, descripcion, fecha_creacion, fecha_ultima_actualizacion)
VALUES ((SELECT id_usuario FROM Usuarios WHERE email = 'miguelperez@facturactiva.com'), 2, 1, 1, 'No puedo emitir facturas', 'El sistema me muestra error al intentar emitir facturas desde esta mañana', GETDATE(), GETDATE());

-- =============================================
-- CONSULTAS DE VERIFICACIÓN
-- =============================================

-- Verificar datos maestros
SELECT 'Roles' AS Tabla, COUNT(*) AS Total FROM Roles
UNION ALL
SELECT 'Estados', COUNT(*) FROM Estados
UNION ALL
SELECT 'Prioridades', COUNT(*) FROM Prioridades
UNION ALL
SELECT 'TiposComprobante', COUNT(*) FROM TiposComprobante
UNION ALL
SELECT 'Usuarios', COUNT(*) FROM Usuarios;
GO

-- Consulta para ver usuarios y sus roles
SELECT 
    u.id_usuario,
    u.nombres + ' ' + ISNULL(u.apellidos, '') AS nombre_completo,
    u.email,
    r.nombre_rol AS rol,
    u.activo
FROM Usuarios u
INNER JOIN Roles r ON u.id_rol = r.id_rol
ORDER BY u.id_usuario;
GO

-- =============================================
-- ACTUALIZACIONES
-- =============================================
-- Actualizar contraseña de cliente@facturactiva.com (cliente123)
UPDATE Usuarios 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE id_usuario = 4;

-- Actualizar contraseña de jefeSoporte@facturactiva.com (jefe123)
UPDATE Usuarios 
SET password_hash = '$2a$10$Q5ZQz4Ln8.T7.8y5U0Z8WOqmVq2h5YKw3JQE5W4rX6xF8jC9B0hKW' 
WHERE id_usuario = 5;

-- Actualizar contraseña de soporte@facturactiva.com (soporte123)
UPDATE Usuarios 
SET password_hash = '$2a$10$8K1p/8jE4LqKfVKr7TZ9sO5F7V4aA1J2h6V5X9cG4L7vZ8W3yB9nO' 
WHERE id_usuario = 6;

-- Verificar que se actualizaron correctamente
SELECT id_usuario, email, LEFT(password_hash, 20) as password_preview 
FROM Usuarios 
WHERE id_usuario IN (4, 5, 6);

ALTER TABLE Tickets
ADD ruta_archivo VARCHAR(500) NULL;

-- =============================================
-- CONSULTAS
-- =============================================
SELECT * FROM Estados ORDER BY id_estado;
SELECT * FROM TiposComprobante ORDER BY id_comprobante;
SELECT * FROM Prioridades ORDER BY nivel;

SELECT * FROM Roles ORDER BY id_rol;
SELECT * FROM Usuarios ORDER BY id_usuario;
SELECT * FROM Tickets ORDER BY id_ticket;