package com.aldo.e_commerce.service.interfaces;

import com.aldo.e_commerce.dto.authResponse.AuthResponse;
import com.aldo.e_commerce.dto.authResponse.MeResponse;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.ContraseñaUsuarioRequest;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.EmailUsuarioRequest;
import com.aldo.e_commerce.dto.dtosUsuarioContraseñaEmail.NombreUsuarioRequest;
import com.aldo.e_commerce.dto.loginAndLogout.LoginRequest;
import com.aldo.e_commerce.dto.registro.RegistroRequest;
import com.aldo.e_commerce.dto.registro.RegistroResponse;

public interface AuthService {
    public RegistroResponse registro(RegistroRequest request);
    public AuthResponse login(LoginRequest request);
    public AuthResponse refreshToken(String refreshToken);
    public void logout(String refreshToken);
    public MeResponse me();

    public void actualizarNombre(NombreUsuarioRequest request);
    public void actualizarEmail(EmailUsuarioRequest request);
    public void actualizarContraseña(ContraseñaUsuarioRequest request);
}
