package pe.com.fadide.sisco.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.dto.PenalidadRequestDTO;
import pe.com.fadide.sisco.dto.PenalidadResponseDTO;
import pe.com.fadide.sisco.service.PenalidadService;

import java.util.List;

@RestController
@RequestMapping("/api/penalidades")
@RequiredArgsConstructor
public class PenalidadController {

    private final PenalidadService penalidadService;

    @GetMapping
    public ResponseEntity<List<PenalidadResponseDTO>> listAll() {
        return ResponseEntity.ok(penalidadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenalidadResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(penalidadService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PenalidadResponseDTO> create(@RequestBody PenalidadRequestDTO dto) {
        return new ResponseEntity<>(penalidadService.create(dto), HttpStatus.CREATED);
    }
}