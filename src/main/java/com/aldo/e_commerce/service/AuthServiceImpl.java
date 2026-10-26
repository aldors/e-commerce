package com.aldo.e_commerce.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.e_commerce.dto.authResponse.AuthResponse;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.ContraseñaUsuarioRequest;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.EmailUsuarioRequest;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.NombreUsuarioRequest;
import com.aldo.e_commerce.dto.loginAndLogout.LoginRequest;
import com.aldo.e_commerce.dto.registro.RegistroRequest;
import com.aldo.e_commerce.dto.registro.RegistroResponse;
import com.aldo.e_commerce.entity.RefreshToken;
import com.aldo.e_commerce.entity.Usuario;
import com.aldo.e_commerce.exception.ContraseñaActualIncorrecta;
import com.aldo.e_commerce.exception.EmailYaExistenteException;
import com.aldo.e_commerce.exception.NuevaContraseñaDiferenteException;
import com.aldo.e_commerce.exception.NuevoEmailDiferenteException;
import com.aldo.e_commerce.exception.NuevoNombreDiferenteException;
import com.aldo.e_commerce.exception.RefreshTokenExpiradoException;
import com.aldo.e_commerce.exception.RefreshTokenNoEncontradoException;
import com.aldo.e_commerce.exception.UsuarioNoEncontradoException;
import com.aldo.e_commerce.mapper.AuthMapper;
import com.aldo.e_commerce.mapper.RegistroMapper;
import com.aldo.e_commerce.repository.RefreshTokenRepository;
import com.aldo.e_commerce.repository.UsuarioRepository;
import com.aldo.e_commerce.security.JwtService;
import com.aldo.e_commerce.service.interfaces.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.expirationRefreshToken}")
    private long jwtExpirationRefreshToken;

    @Override 
    public RegistroResponse registro(RegistroRequest request){

        if(usuarioRepository.findByEmail(request.getEmail()).isPresent()){
            throw new EmailYaExistenteException(request.getEmail());
        }

        // Aquí se mapea el RegistroRequest a Usuario, se guarda en la base de datos y luego se mapea a RegistroResponse para devolverlo al controlador
        Usuario usuario = RegistroMapper.toEntity(request);

        // Encriptar la contraseña antes de guardar el usuario ya que el mapeo no lo hace debido a que el PasswordEncoder no es parte del RegistroMapper
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        return RegistroMapper.toResponse(usuarioGuardado);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Aqui se usa el UserDetailsByEmailService para cargar el usuario por email en lugar de por username, ya que en este caso el email es el identificador único del usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UsuarioNoEncontradoException());
        
        refreshTokenRepository.deleteByUsuario(usuario);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken tokenEntity = RefreshToken.builder()
                .token(refreshToken)
                // ¿Por que se hace asi ahora? Porque el RefreshToken ya no tiene un campo 'nombre' que almacena el email del usuario, sino que ahora tiene una relación con la entidad Usuario, por lo que se puede obtener el usuario directamente a través de esa relación. Esto hace que el diseño sea más limpio y orientado a objetos, ya que el RefreshToken ahora tiene una referencia directa al Usuario al que pertenece, en lugar de almacenar solo un campo de texto con el email.
                .usuario(usuario)
                .expirationDate(LocalDateTime.now().plusSeconds(jwtExpirationRefreshToken / 1000)) // Establecer la fecha de expiración del refresh token
                .build();

        refreshTokenRepository.save(tokenEntity);

        return AuthMapper.toResponse(accessToken, refreshToken);
    }
    
    @Override
    @Transactional(noRollbackFor = {DisabledException.class, RefreshTokenExpiradoException.class})
    public AuthResponse refreshToken(String refreshToken){

        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenNoEncontradoException(refreshToken));

        if(tokenEntity.getExpirationDate().isBefore(LocalDateTime.now())){
            refreshTokenRepository.deleteByToken(refreshToken); // Eliminar el token expirado de la base de datos
            throw new RefreshTokenExpiradoException(refreshToken);
        }

        Usuario usuario = tokenEntity.getUsuario(); // Obtener el usuario asociado al refresh token
        String email = usuario.getEmail(); // Obtener el email del usuario

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!userDetails.isEnabled()) {
            refreshTokenRepository.deleteByUsuario(usuario);
            throw new DisabledException("La cuenta está desactivada");
        }

        // ¿Debo quitar esto? No, es importante mantener esta validación para asegurarnos de que el refresh token es válido y no ha sido manipulado. Aunque el token ya existe en la base de datos, es posible que haya sido modificado o que el usuario haya cambiado su contraseña, lo que invalidaría el token. Por lo tanto, es recomendable mantener esta validación para garantizar la seguridad del sistema. 
        if(!jwtService.isValid(refreshToken, userDetails)){
            throw new RuntimeException("Refresh token no válido");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return AuthMapper.toResponse(newAccessToken, refreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken){
        refreshTokenRepository.deleteByToken(refreshToken);
    }


    //*******
    // Nuevas funcionalidades: cambiar nombre de usuario, email y posiblemente contraseña
    //*******
    
    @Override
    public void actualizarNombre(NombreUsuarioRequest request){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new UsuarioNoEncontradoException());

        if(usuario.getNombre().equals(request.getNombre())){
            throw new NuevoNombreDiferenteException();
        }
        
        usuario.setNombre(request.getNombre());

        usuarioRepository.save(usuario);
    }

    // Este metodo es basico, necesita qe el front haga un logout automatico, para actualizar el jwt que contiene email antiguo
    @Override
    public void actualizarEmail(EmailUsuarioRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailActual = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(emailActual).orElseThrow(() -> new UsuarioNoEncontradoException());

        if(usuario.getEmail().equals(request.getEmail())){
            throw new NuevoEmailDiferenteException();
        }

        if(usuarioRepository.findByEmail(request.getEmail()).isPresent()){
            throw new EmailYaExistenteException(request.getEmail());
        }

        usuario.setEmail(request.getEmail());

        usuarioRepository.save(usuario);
    }
    
    @Override
    public void actualizarContraseña(ContraseñaUsuarioRequest request){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailActual = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(emailActual).orElseThrow(() -> new UsuarioNoEncontradoException());

        if(!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())){
            throw new ContraseñaActualIncorrecta();
        }

        if(passwordEncoder.matches(request.getPasswordNueva(), usuario.getPassword())){
            throw new NuevaContraseñaDiferenteException();
        }

        String nuevaPasswordEncriptada = passwordEncoder.encode(request.getPasswordNueva());

        usuario.setPassword(nuevaPasswordEncriptada);
        
        usuarioRepository.save(usuario);
    }
}
