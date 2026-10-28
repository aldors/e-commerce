package com.aldo.e_commerce.service.interfaces;

import com.aldo.e_commerce.dto.auth.AuthResponse;
import com.aldo.e_commerce.dto.auth.ContraseñaUsuarioRequest;
import com.aldo.e_commerce.dto.auth.EmailUsuarioRequest;
import com.aldo.e_commerce.dto.auth.LoginRequest;
import com.aldo.e_commerce.dto.auth.MeResponse;
import com.aldo.e_commerce.dto.auth.NombreUsuarioRequest;
import com.aldo.e_commerce.dto.auth.RegistroRequest;
import com.aldo.e_commerce.dto.auth.RegistroResponse;

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
