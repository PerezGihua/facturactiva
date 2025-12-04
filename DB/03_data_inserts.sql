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

-- =============================================
-- 9. INSERTS DE USUARIOS
-- =============================================
INSERT INTO usuarios (id_rol, email, password_hash, nombres, apellidos, fecha_registro, activo) VALUES
(1, 'admin@facturactiva.com', 'YWRtaW4xMjM=', 'Juan Carlos', 'Administrador Sistema', '2025-10-17 00:29:43.3795700 -05:00', 1), -- admin123
(2, 'jefe@facturactiva.com', 'amVmZTEyMw==', 'Maria Elena', 'Supervisor García', '2025-10-17 00:29:43.4001313 -05:00', 1), -- jefe123
(3, 'agente1@facturactiva.com', 'YWdlbnRlMTIz', 'Carlos Alberto', 'Técnico Pérez', '2025-10-17 00:29:43.4011286 -05:00', 1); -- agente123
GO

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
SELECT 'TiposComprobante', COUNT(*) FROM TiposComprobante;
GO

-- Verificar usuarios insertados
SELECT 'Usuarios' AS Tabla, COUNT(*) AS Total FROM Usuarios;
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
-- CONSULTAS DE EJEMPLO PARA PROBAR
-- =============================================

-- 1. Ver todos los estados disponibles
SELECT * FROM Estados ORDER BY id_estado;

-- 2. Ver todos los tipos de comprobante
SELECT * FROM TiposComprobante ORDER BY id_comprobante;

-- 3. Ver todas las prioridades
SELECT * FROM Prioridades ORDER BY nivel;

-- 4. Probar las vistas (una vez se hayan creado tickets)
/*
SELECT * FROM VW_TicketsPorAgente;
SELECT * FROM VW_TiempoPromedioSolucion;
SELECT * FROM VW_TicketsPorCliente;
*/