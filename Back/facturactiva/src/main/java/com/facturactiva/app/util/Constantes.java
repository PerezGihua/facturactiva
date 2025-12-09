package com.facturactiva.app.util;

public class Constantes {

    // RUTAS
    public static final String FRONT_URL = "http://localhost:4200";
    public static final String RUTA_LOGS = "C:/facturactiva/logs";

    // NOMBRE METODOS
    public static final String METODO_AUTHENTICATE_USER = "[AUTENTICACION DE USUARIO]";
    public static final String METODO_LOGIN = "[LOGIN]";
    public static final String METODO_REGISTER = "[REGISTRO]";

    // PARAMETROS DEL SP - AUTENTICACION
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "psw";
    public static final String PARAM_ID_ROL = "id_rol";
    public static final String PARAM_ROLE_NAME = "roleName";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_NOMBRE_USER = "nombreUser";
    public static final String PARAM_APELLIDO_USER = "apellido_usuario";

    // PARAMETROS DEL SP - REGISTRO
    public static final String PARAM_P_ID_ROL = "p_id_rol";
    public static final String PARAM_P_EMAIL = "p_email";
    public static final String PARAM_P_PASSWORD_HASH = "p_password_hash";
    public static final String PARAM_P_NOMBRES = "p_nombres";
    public static final String PARAM_P_APELLIDOS = "p_apellidos";
    public static final String PARAM_ID_USUARIO = "id_usuario";
    public static final String PARAM_ERROR_CODE = "error_code";

    // MENSAJES DE AUTENTICACION
    public static final String MSG_AUTH_SUCCESS = "Autenticación exitosa";
    public static final String MSG_AUTH_FAILURE = "Usuario o contraseña incorrectos";
    public static final String MSG_USER_NOT_FOUND = "Usuario no encontrado";

    // MENSAJES DE REGISTRO
    public static final String MSG_REGISTER_SUCCESS = "Usuario registrado exitosamente";
    public static final String MSG_REGISTER_FAILURE = "Error al registrar usuario";
    public static final String MSG_EMAIL_EXISTS = "El email ya está registrado";
    public static final String MSG_INVALID_ROLE = "El rol especificado no existe";

    // NOMBRE DE TABLAS
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_ROLES = "Roles";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_USER_NAME = "userName";
    public static final String COLUMN_PASSWORD = "psw";
    public static final String COLUMN_ROLE_ID = "roleIdUsers";
    public static final String COLUMN_ROLE_NAME = "roleName";
    public static final String OUT_MENSAJE = "mensaje";
    public static final String OUT_ROLE_NAME = "roleName";

    // NOMBRES DE SP
    public static final String SP_VALIDAR_USER = "SP_Validar_User";
    public static final String SP_REGISTRAR_USUARIO = "SP_RegistrarUsuario";
    public static final String SP_VERIFICAR_EMAIL_EXISTE = "SP_VerificarEmailExiste";

    // KEYS DE RESULTADOS
    public static final String RESULT_SET_KEY = "#result-set-1";

    // POSIBLES ERRORES
    public static final String DATABASE_ERROR = "ERROR_DB";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String INVALID_INPUT = "ERROR_INVALID_INPUT";

    // METODOS
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    // CARACTERES
    public static final String ASTERISCO = "*";
    public static final String UNO = "1";
    public static final String ESPACIO_BLANCO = " ";
    public static final String GUION = "-";

    private Constantes() {
        // Constructor privado para prevenir instanciación
    }
}