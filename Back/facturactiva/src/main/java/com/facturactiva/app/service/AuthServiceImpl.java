package com.facturactiva.app.service;

import com.facturactiva.app.dto.LoginRequest;
import com.facturactiva.app.dto.LoginResponse;
import com.facturactiva.app.dto.RegisterRequest;
import com.facturactiva.app.dto.RegisterResponse;
import com.facturactiva.app.dto.UserDetailsDTO;
import com.facturactiva.app.entity.UsuarioEntity;
import com.facturactiva.app.exception.AuthException;
import com.facturactiva.app.repository.AuthRepository;
import com.facturactiva.app.security.JwtTokenProvider;
import com.facturactiva.app.util.Constantes;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("{} Iniciando proceso de autenticación para: {}", 
                Constantes.METODO_LOGIN, loginRequest.getEmail());

        try {
            // Buscar el usuario primero para validaciones previas
            UsuarioEntity usuario = authRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.warn("{} Usuario no encontrado: {}", Constantes.METODO_LOGIN, loginRequest.getEmail());
                        return new AuthException(Constantes.MSG_AUTH_FAILURE);
                    });

            // Verificar que el usuario esté activo
            if (!usuario.getActivo()) {
                log.warn("{} Usuario inactivo: {}", Constantes.METODO_LOGIN, loginRequest.getEmail());
                throw new AuthException(Constantes.MSG_AUTH_FAILURE);
            }

            log.debug("{} Autenticando usuario con Spring Security", Constantes.METODO_LOGIN);

            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("{} Autenticación exitosa, generando token JWT", Constantes.METODO_LOGIN);

            // Generar token JWT
            String nombreCompleto = usuario.getNombres() + " " + 
                    (usuario.getApellidos() != null ? usuario.getApellidos() : "");
            String token = jwtTokenProvider.generateToken(authentication, usuario.getIdRol(), nombreCompleto);

            log.info("{} Autenticación exitosa para: {}", 
                    Constantes.METODO_LOGIN, loginRequest.getEmail());

            return new LoginResponse(
                    token,
                    usuario.getIdRol(),
                    nombreCompleto,
                    loginRequest.getEmail(),
                    Constantes.MSG_AUTH_SUCCESS
            );

        } catch (BadCredentialsException e) {
            // Capturar específicamente el error de credenciales incorrectas
            log.warn("{} Credenciales incorrectas para: {}", 
                    Constantes.METODO_LOGIN, loginRequest.getEmail());
            throw new AuthException(Constantes.MSG_AUTH_FAILURE);
        } catch (AuthException e) {
            // Re-lanzar excepciones de autenticación ya manejadas
            throw e;
        } catch (Exception e) {
            // Capturar cualquier otro error inesperado
            log.error("{} Error inesperado durante la autenticación para {}: {}", 
                    Constantes.METODO_LOGIN, loginRequest.getEmail(), e.getMessage());
            throw new AuthException(Constantes.MSG_AUTH_FAILURE);
        }
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("{} Iniciando proceso de registro para: {}", 
                Constantes.METODO_REGISTER, registerRequest.getEmail());

        try {
            // 1. Verificar si el email ya existe
            if (authRepository.emailExists(registerRequest.getEmail())) {
                log.warn("{} Email ya registrado: {}", Constantes.METODO_REGISTER, registerRequest.getEmail());
                return RegisterResponse.builder()
                        .success(false)
                        .message(Constantes.MSG_EMAIL_EXISTS)
                        .build();
            }

            // 2. Hash de la contraseña con BCrypt
            String passwordHash = passwordEncoder.encode(registerRequest.getPassword());
            log.debug("{} Password hasheado con BCrypt", Constantes.METODO_REGISTER);

            // 3. Registrar usuario mediante SP
            Map<String, Object> result = authRepository.registerUser(registerRequest, passwordHash);

            Integer errorCode = (Integer) result.get(Constantes.PARAM_ERROR_CODE);
            String message = (String) result.get(Constantes.PARAM_MESSAGE);
            Object idUsuarioObj = result.get(Constantes.PARAM_ID_USUARIO);

            // 4. Verificar resultado del registro
            if (errorCode == null || errorCode != 0 || idUsuarioObj == null) {
                log.error("{} Error al registrar usuario: {}", Constantes.METODO_REGISTER, message);
                return RegisterResponse.builder()
                        .success(false)
                        .message(message != null ? message : Constantes.MSG_REGISTER_FAILURE)
                        .build();
            }

            Integer idUsuario = (Integer) idUsuarioObj;

            log.info("{} Usuario registrado exitosamente con ID: {}", Constantes.METODO_REGISTER, idUsuario);

            // 5. Retornar respuesta exitosa
            return RegisterResponse.builder()
                    .success(true)
                    .idUsuario(idUsuario)
                    .email(registerRequest.getEmail())
                    .nombres(registerRequest.getNombres())
                    .apellidos(registerRequest.getApellidos())
                    .idRol(registerRequest.getIdRol())
                    .message(Constantes.MSG_REGISTER_SUCCESS)
                    .build();

        } catch (Exception e) {
            log.error("{} Error durante el registro: {}", 
                    Constantes.METODO_REGISTER, e.getMessage(), e);
            return RegisterResponse.builder()
                    .success(false)
                    .message(Constantes.MSG_REGISTER_FAILURE + ": " + e.getMessage())
                    .build();
        }
    }

    @Override
    public void logout() {
        log.info("Usuario cerrando sesión");
        SecurityContextHolder.clearContext();
    }

    @Override
    public Map<String, Object> validateUser(String email, String password) {
        return authRepository.validateUser(email, password);
    }

    @Override
    public Optional<UsuarioEntity> findByEmail(String email) {
        return authRepository.findByEmail(email);
    }
}