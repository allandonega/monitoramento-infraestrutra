package com.monitora.controller;

import com.monitora.model.TesteVelocidade;
import com.monitora.repository.TesteVelocidadeRepository;
import com.monitora.service.TesteVelocidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/velocidade")
@RequiredArgsConstructor
public class VelocidadeController {

    private final TesteVelocidadeService testeService;
    private final TesteVelocidadeRepository repo;

    @GetMapping
    public String velocidadePage(Model model) {
        List<TesteVelocidade> historico = repo.findTop50ByOrderByExecutadoEmDesc();
        model.addAttribute("historico", historico);
        model.addAttribute("paginaAtiva", "velocidade");
        return "velocidade";
    }

    @PostMapping("/api/testar/download")
    @ResponseBody
    public ResponseEntity<?> testarDownload() {
        try {
            TesteVelocidade resultado = testeService.testarDownload();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/api/testar/upload")
    @ResponseBody
    public ResponseEntity<?> testarUpload() {
        try {
            TesteVelocidade resultado = testeService.testarUpload();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/api/testar/ping")
    @ResponseBody
    public ResponseEntity<?> testarPing() {
        try {
            TesteVelocidade resultado = testeService.testarPing();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/api/historico")
    @ResponseBody
    public ResponseEntity<List<TesteVelocidade>> getHistorico(
            @RequestParam(defaultValue = "7") int dias) {
        LocalDateTime desde = LocalDateTime.now().minusDays(dias);
        List<TesteVelocidade> lista = repo.findByExecutadoEmAfterOrderByExecutadoEmAsc(desde);
        return ResponseEntity.ok(lista);
    }
}
