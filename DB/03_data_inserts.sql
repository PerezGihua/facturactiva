USE facturactiva;
GO

-- =============================================
-- 4. INSERCIÓN DE DATOS INICIALES
-- =============================================

-- 4.1 INSERCIÓN INICIAL: ROLES
INSERT INTO Roles (nombre_rol) VALUES
('Cliente'),
('Jefe de Soporte'),
('Agente de Soporte')
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
update Usuarios set activo = '0' where id_usuario = 4;
update Usuarios set activo = '0' where id_usuario = 5;
update Usuarios set activo = '0' where id_usuario = 6;

-- =============================================
-- CONSULTAS
-- =============================================

SELECT * FROM ArchivosAdjuntos;

SELECT * FROM TiposComprobante ORDER BY id_comprobante;
SELECT * FROM Prioridades ORDER BY nivel;
SELECT * FROM Estados ORDER BY id_estado;
SELECT * FROM Roles ORDER BY id_rol;
SELECT * FROM Usuarios ORDER BY id_usuario;
SELECT * FROM Tickets ORDER BY id_ticket;
SELECT * FROM HistorialTicket ORDER BY id_historial;