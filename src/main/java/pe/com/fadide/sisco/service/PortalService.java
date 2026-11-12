package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.MiObraResponse;

import java.util.List;

public interface PortalService {
    List<MiObraResponse> misObras(String correo);
}
