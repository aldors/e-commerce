package com.aldo.e_commerce.testAuth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aldo.e_commerce.dto.authResponse.AuthResponse;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.ContraseñaUsuarioRequest;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.EmailUsuarioRequest;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.NombreUsuarioRequest;
import com.aldo.e_commerce.dto.loginAndLogout.LoginRequest;
import com.aldo.e_commerce.dto.registro.RegistroRequest;
import com.aldo.e_commerce.dto.registro.RegistroResponse;
import com.aldo.e_commerce.entity.RefreshToken;
import com.aldo.e_commerce.entity.Role;
import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.exception.ContraseñaActualIncorrecta;
import com.aldo.e_commerce.exception.EmailYaExistenteException;
import com.aldo.e_commerce.exception.NuevaContraseñaDiferenteException;
import com.aldo.e_commerce.exception.NuevoEmailDiferenteException;
import com.aldo.e_commerce.exception.NuevoNombreDiferenteException;
import com.aldo.e_commerce.exception.RefreshTokenExpiradoException;
import com.aldo.e_commerce.exception.RefreshTokenNoEncontradoException;
import com.aldo.e_commerce.exception.UsuarioNoEncontradoException;
import com.aldo.e_commerce.repository.RefreshTokenRepository;
import com.aldo.e_commerce.repository.UsuarioRepository;
import com.aldo.e_commerce.security.JwtService;
import com.aldo.e_commerce.service.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private Usuario usuario;
    private RegistroRequest registroRequest;
    private LoginRequest loginRequest;

    private NombreUsuarioRequest nombreUsuarioRequest;
    private EmailUsuarioRequest emailUsuarioRequest;
    private ContraseñaUsuarioRequest contraseñalUsuariorequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Aldo")
                .email("aldo@test.com")
                .password("123456")
                .role(Role.USER)
                .build();

        registroRequest = new RegistroRequest();

        registroRequest.setNombre("Aldo");
        registroRequest.setEmail("aldo@test.com");
        registroRequest.setPassword("123456");

        loginRequest = new LoginRequest();

        loginRequest.setEmail("aldo@test.com");
        loginRequest.setPassword("123456");

        nombreUsuarioRequest = new NombreUsuarioRequest();

        nombreUsuarioRequest.setNombre("Nuevo Nombre");

        emailUsuarioRequest = new EmailUsuarioRequest();

        emailUsuarioRequest.setEmail("nuevoemail@test.com");

        contraseñalUsuariorequest = new ContraseñaUsuarioRequest();

        contraseñalUsuariorequest.setPasswordActual(usuario.getPassword());
        contraseñalUsuariorequest.setPasswordNueva("nueva contraseña");


        // --- Simular usuario autenticado en SecurityContext ---
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, List.of());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    //Happy path
    @Test
    void deberiaRegistrarCorrectamente(){

        when(usuarioRepository.findByEmail(registroRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registroRequest.getPassword())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        RegistroResponse registroResponse = authServiceImpl.registro(registroRequest);

        assertNotNull(registroResponse);
        assertEquals(usuario.getNombre(), registroResponse.getNombre());
        assertEquals("USER", registroResponse.getRole());

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deberiaLogearCorrectamente(){

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(loginRequest.getEmail())
                .password("encodedPassword")
                .roles("USER")
                .build();

        when(userDetailsService.loadUserByUsername(loginRequest.getEmail())).thenReturn(userDetails);

        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(usuarioRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(usuario));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        assertNotNull(authResponse);
        assertEquals("access-token", authResponse.getAccessToken());
        assertEquals("refresh-token", authResponse.getRefreshToken());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void deberiaRefrescarElToken(){
        
        RefreshToken tokenEntity = RefreshToken.builder()
                .token("refresh-token")
                .usuario(usuario)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(tokenEntity));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(usuario.getEmail())
                .password("encodedPassword")
                .roles("USER")
                .build();

        when(userDetailsService.loadUserByUsername(usuario.getEmail())).thenReturn(userDetails);

        when(jwtService.isValid("refresh-token", userDetails)).thenReturn(true);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("new-access-token");

        AuthResponse authResponse = authServiceImpl.refreshToken("refresh-token");

        assertNotNull(authResponse);
        assertEquals("new-access-token", authResponse.getAccessToken());
        assertEquals("refresh-token", authResponse.getRefreshToken());

        verify(refreshTokenRepository).findByToken("refresh-token");
        
    }

    @Test
    void deberiaCerrarSesion(){

        authServiceImpl.logout("refresh-token");

        verify(refreshTokenRepository).deleteByToken("refresh-token");
    }

    // Test de las nuevas funcionalidades
    @Test
    void deberiaActualizarElNombre(){

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        authServiceImpl.actualizarNombre(nombreUsuarioRequest);

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deberiaActualizarEmail(){
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail(emailUsuarioRequest.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        authServiceImpl.actualizarEmail(emailUsuarioRequest);

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deberiaActualizarContraseña(){
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(contraseñalUsuariorequest.getPasswordActual(), usuario.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(contraseñalUsuariorequest.getPasswordNueva(), usuario.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(contraseñalUsuariorequest.getPasswordNueva())).thenReturn("nueva contraseña encriptada");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        authServiceImpl.actualizarContraseña(contraseñalUsuariorequest);

        verify(usuarioRepository).save(any(Usuario.class));
    }

    //Error path
    @Test
    void deberiaLanzarExcepcionSiYaHayUnEmailRegistrado(){

        when(usuarioRepository.findByEmail(registroRequest.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(EmailYaExistenteException.class, () -> authServiceImpl.registro(registroRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiElRefreshTokenNoSeEncuentra() {
        
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.empty());

        assertThrows(RefreshTokenNoEncontradoException.class,
                    () -> authServiceImpl.refreshToken("refresh-token"));
    }

    @Test
    void deberiaLanzarExcepcionSiElRefreshTokenEstaExpirado(){

        RefreshToken tokenEntity = RefreshToken.builder()
                .token("refresh-token")
                .usuario(usuario)
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(tokenEntity));

        assertThrows(RefreshTokenExpiradoException.class, () -> authServiceImpl.refreshToken("refresh-token"));
    }


    //Test de las nuevas funcionalidades
    @Test
    void deberiaLanzarExcepcionSiUsuarioNoEncontrado(){

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());

        //Este test funciona para los 3 metodos donde busca a un usuario, solo se cambia authServiceImpl.actualizarNombre por
        // por authServiceImpl.(Aqui el nombre del metodo -> actualizarEmail o actualizarContraseña)
        assertThrows(UsuarioNoEncontradoException.class, () -> authServiceImpl.actualizarNombre(nombreUsuarioRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiElNuevoNombreEsIgualAlActual(){

        nombreUsuarioRequest.setNombre(usuario.getNombre());

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(NuevoNombreDiferenteException.class, () -> authServiceImpl.actualizarNombre(nombreUsuarioRequest));
    }
    
    @Test
    void deberiaLanzarExcepcionSiElNuevoEmailEsIgualAlActual(){
        
        emailUsuarioRequest.setEmail(usuario.getEmail());
        
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(NuevoEmailDiferenteException.class, () -> authServiceImpl.actualizarEmail(emailUsuarioRequest));
    }

    @Test
    void deberiaLanzarExcepcionSiContraseñaActualEsIncorrecta(){

        contraseñalUsuariorequest.setPasswordActual("Esta contraseña actual es incorrecta");

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(ContraseñaActualIncorrecta.class, () -> authServiceImpl.actualizarContraseña(contraseñalUsuariorequest));
    }

    @Test
    void deberiaLanzarExcepcionSiLaNuevaContraseñaEsIgualALaActual(){

        contraseñalUsuariorequest.setPasswordActual("contraseñaActual");
        contraseñalUsuariorequest.setPasswordNueva("contraseñaActual");

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("contraseñaActual", usuario.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("contraseñaActual", usuario.getPassword())).thenReturn(true);

        assertThrows(NuevaContraseñaDiferenteException.class, () -> authServiceImpl.actualizarContraseña(contraseñalUsuariorequest));
    }
    
}
