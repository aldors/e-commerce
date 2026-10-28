package com.aldo.e_commerce.testAuth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aldo.e_commerce.controller.AuthController;
import com.aldo.e_commerce.dto.auth.AuthResponse;
import com.aldo.e_commerce.dto.auth.ContraseñaUsuarioRequest;
import com.aldo.e_commerce.dto.auth.EmailUsuarioRequest;
import com.aldo.e_commerce.dto.auth.LoginRequest;
import com.aldo.e_commerce.dto.auth.LogoutRequest;
import com.aldo.e_commerce.dto.auth.NombreUsuarioRequest;
import com.aldo.e_commerce.dto.auth.RefreshTokenRequest;
import com.aldo.e_commerce.dto.auth.RegistroRequest;
import com.aldo.e_commerce.dto.auth.RegistroResponse;
import com.aldo.e_commerce.security.CustomUserDetailsService;
import com.aldo.e_commerce.security.JwtService;
import com.aldo.e_commerce.service.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    
    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void deberiaRegistrarCorrectamente() throws Exception {

        RegistroRequest registroRequest = new RegistroRequest();
        registroRequest.setNombre("Aldo");
        registroRequest.setEmail("aldo@test.com");
        registroRequest.setPassword("123456");

        RegistroResponse registroResponse = new RegistroResponse(1L, "Aldo", "USER");

        when(authService.registro(any(RegistroRequest.class))).thenReturn(registroResponse);

        mockMvc.perform(
                    post("/auth/registro")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registroRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nombre").value("Aldo"));
    }

    @Test
    @WithMockUser
    void deberiaLoguearCorrectamente() throws Exception {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("aldo@test.com");
        loginRequest.setPassword("123456");

        AuthResponse authResponse = new AuthResponse("access-token", "refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(
                    post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @WithMockUser
    void deberiaRefrescarElToken() throws Exception {

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();

        refreshTokenRequest.setRefreshToken("refresh-token");

        AuthResponse authResponse = new AuthResponse("new-access-token", "refresh-token");

        when(authService.refreshToken("refresh-token")).thenReturn(authResponse);

        mockMvc.perform(
                    post("/auth/refresh-token")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshTokenRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @WithMockUser
    void deberiaCerrarSesion() throws Exception {
        
        LogoutRequest logoutRequest = new LogoutRequest();

        logoutRequest.setRefreshToken("refresh-token");

        doNothing().when(authService).logout("refresh-token");

        mockMvc.perform(
                    post("/auth/logout")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(logoutRequest))
                )
                .andExpect(status().isNoContent());
    }


    //Tests de las nuevas funcionalidades
    @Test
    @WithMockUser
    void deberiaActualizarElNombre() throws Exception{

        NombreUsuarioRequest nombreUsuarioRequest = new NombreUsuarioRequest();
        nombreUsuarioRequest.setNombre("Nuevo nombre");

        doNothing().when(authService).actualizarNombre(nombreUsuarioRequest);

        mockMvc.perform(
            put("/auth/actualizar-nombre")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nombreUsuarioRequest))
        )
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deberiaActualizarElEmail() throws Exception{

        EmailUsuarioRequest emailUsuarioRequest = new EmailUsuarioRequest();
        emailUsuarioRequest.setEmail("nuevoemail@test.com");

        doNothing().when(authService).actualizarEmail(emailUsuarioRequest);

        mockMvc.perform(
            put("/auth/actualizar-email")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(emailUsuarioRequest))
        )
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deberiaActualizarLaContraseña() throws Exception{

        ContraseñaUsuarioRequest contraseñaUsuarioRequest = new ContraseñaUsuarioRequest();
        contraseñaUsuarioRequest.setPasswordActual("Actual");
        contraseñaUsuarioRequest.setPasswordNueva("Nueva1");

        doNothing().when(authService).actualizarContraseña(contraseñaUsuarioRequest);

        mockMvc.perform(
            put("/auth/actualizar-contraseña")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(contraseñaUsuarioRequest))
        )
        .andExpect(status().isOk());
    }
}
