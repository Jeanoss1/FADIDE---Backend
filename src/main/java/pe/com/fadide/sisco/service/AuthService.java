package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.GoogleLoginRequestDTO;
import pe.com.fadide.sisco.dto.LoginRequestDTO;
import pe.com.fadide.sisco.dto.LoginResponseDTO;
import pe.com.fadide.sisco.dto.RegisterRequestDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO dto);

    LoginResponseDTO register(RegisterRequestDTO dto);

    LoginResponseDTO loginWithGoogle(GoogleLoginRequestDTO dto);
}
