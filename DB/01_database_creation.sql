-- =============================================
-- SCRIPTS - FACTURACTIVA
-- =============================================

-- Crear la base de datos
CREATE DATABASE facturactiva;
GO

USE facturactiva;
GO

-- =============================================
-- 1. TABLAS MAESTRAS (DIMENSIONES)
-- =============================================

-- 1.1 TABLA DE ROLES
CREATE TABLE Roles (
    id_rol INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL
);
GO

-- 1.2 TABLA DE ESTADOS DEL TICKET
CREATE TABLE Estados (
    id_estado INT IDENTITY(1,1) PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(255) NULL
);
GO

-- 1.3 TABLA DE PRIORIDADES
CREATE TABLE Prioridades (
    id_prioridad INT IDENTITY(1,1) PRIMARY KEY,
    nombre_prioridad VARCHAR(20) UNIQUE NOT NULL,
    nivel INT UNIQUE NOT NULL
);
GO

-- 1.4 TABLA DE TIPOS DE COMPROBANTE
CREATE TABLE TiposComprobante (
    id_comprobante INT IDENTITY(1,1) PRIMARY KEY,
    nombre_comprobante VARCHAR(100) UNIQUE NOT NULL
);
GO

-- =============================================
-- 2. TABLAS TRANSACCIONALES Y DE USUARIOS
-- =============================================

-- 2.1 TABLA DE USUARIOS
CREATE TABLE Usuarios (
    id_usuario INT IDENTITY(1,1) PRIMARY KEY,
    id_rol INT NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NULL,
    fecha_registro DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),
    activo BIT DEFAULT 1,
    CONSTRAINT FK_Usuarios_Roles FOREIGN KEY (id_rol) REFERENCES Roles(id_rol)
);
GO

-- 2.2 TABLA DE TICKETS (Centro del sistema)
CREATE TABLE Tickets (
    id_ticket INT IDENTITY(1,1) PRIMARY KEY,
    -- Relaciones con Usuarios
    id_usuario_cliente INT NOT NULL,
    id_usuario_agente INT NULL,
    id_usuario_jefe INT NULL,
    -- Clasificacion
    id_estado INT NOT NULL,
    id_prioridad INT NOT NULL,
    id_tipo_comprobante INT NOT NULL,
    -- Contenido
    asunto VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    numero_documento_rechazado VARCHAR(50) NULL,
    -- Fechas
    fecha_creacion DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),
    fecha_ultima_actualizacion DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),
    fecha_cierre DATETIMEOFFSET NULL,
    -- Constraints
    CONSTRAINT FK_Tickets_Cliente FOREIGN KEY (id_usuario_cliente) REFERENCES Usuarios(id_usuario),
    CONSTRAINT FK_Tickets_Agente FOREIGN KEY (id_usuario_agente) REFERENCES Usuarios(id_usuario),
    CONSTRAINT FK_Tickets_Jefe FOREIGN KEY (id_usuario_jefe) REFERENCES Usuarios(id_usuario),
    CONSTRAINT FK_Tickets_Estados FOREIGN KEY (id_estado) REFERENCES Estados(id_estado),
    CONSTRAINT FK_Tickets_Prioridades FOREIGN KEY (id_prioridad) REFERENCES Prioridades(id_prioridad),
    CONSTRAINT FK_Tickets_TiposComprobante FOREIGN KEY (id_tipo_comprobante) REFERENCES TiposComprobante(id_comprobante)
);
GO

-- 2.3 TABLA DE COMENTARIOS/INTERACCIONES
CREATE TABLE Comentarios (
    id_comentario INT IDENTITY(1,1) PRIMARY KEY,
    id_ticket INT NOT NULL,
    id_usuario INT NOT NULL,
    contenido TEXT NOT NULL,
    fecha_creacion DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),
    tipo_comunicacion VARCHAR(50) NULL,
    CONSTRAINT FK_Comentarios_Tickets FOREIGN KEY (id_ticket) REFERENCES Tickets(id_ticket),
    CONSTRAINT FK_Comentarios_Usuarios FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
);
GO

-- 2.4 TABLA DE ARCHIVOS ADJUNTOS
CREATE TABLE ArchivosAdjuntos (
    id_archivo INT IDENTITY(1,1) PRIMARY KEY,
    id_ticket INT NOT NULL,
    id_usuario INT NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_almacenamiento VARCHAR(500) UNIQUE NOT NULL,
    es_correccion BIT DEFAULT 0,
    fecha_subida DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),
    CONSTRAINT FK_Archivos_Tickets FOREIGN KEY (id_ticket) REFERENCES Tickets(id_ticket),
    CONSTRAINT FK_Archivos_Usuarios FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
);
GO

-- 2.5 TABLA DE HISTORIAL DEL TICKET (Auditor a)
CREATE TABLE HistorialTicket (
    id_historial INT IDENTITY(1,1) PRIMARY KEY,
    id_ticket INT NOT NULL,
    id_usuario_afector INT NULL,
    tipo_evento VARCHAR(50) NOT NULL,
    detalle TEXT NULL,
    fecha_evento DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),
    CONSTRAINT FK_Historial_Tickets FOREIGN KEY (id_ticket) REFERENCES Tickets(id_ticket),
    CONSTRAINT FK_Historial_Usuarios FOREIGN KEY (id_usuario_afector) REFERENCES Usuarios(id_usuario)
);
GO

-- =============================================
-- 3.  NDICES PARA OPTIMIZACI N (RNF2.5)
-- =============================================

--  ndices en Tickets
CREATE INDEX IDX_Tickets_Cliente ON Tickets(id_usuario_cliente);
CREATE INDEX IDX_Tickets_Agente ON Tickets(id_usuario_agente);
CREATE INDEX IDX_Tickets_Estado ON Tickets(id_estado);
CREATE INDEX IDX_Tickets_FechaCreacion ON Tickets(fecha_creacion);
GO

--  ndices en HistorialTicket para reportes
CREATE INDEX IDX_Historial_Ticket ON HistorialTicket(id_ticket);
CREATE INDEX IDX_Historial_Usuario ON HistorialTicket(id_usuario_afector);
CREATE INDEX IDX_Historial_Fecha ON HistorialTicket(fecha_evento);
GO

--  ndices en Comentarios
CREATE INDEX IDX_Comentarios_Ticket ON Comentarios(id_ticket);
GO