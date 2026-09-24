package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.LoginRequestDTO;
import pe.com.fadide.sisco.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO dto);
}
