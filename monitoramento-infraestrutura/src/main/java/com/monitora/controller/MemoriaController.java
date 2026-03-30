package com.monitora.controller;

import com.monitora.dto.MemoriaInfoDTO;
import com.monitora.model.MetricaMemoria;
import com.monitora.model.ProcessoMemoria;
import com.monitora.repository.MetricaMemoriaRepository;
import com.monitora.repository.ProcessoMemoriaRepository;
import com.monitora.service.ColetorMemoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/memoria")
@RequiredArgsConstructor
public class MemoriaController {

    private final ColetorMemoriaService coletorMemoria;
    private final MetricaMemoriaRepository memoriaRepo;
    private final ProcessoMemoriaRepository processoRepo;

    @GetMapping
    public String memoria(Model model) {
        MemoriaInfoDTO dados = coletorMemoria.coletarDadosAtuais();
        model.addAttribute("memoria", dados);
        model.addAttribute("paginaAtiva", "memoria");
        return "memoria";
    }

    @GetMapping("/api/atual")
    @ResponseBody
    public ResponseEntity<MemoriaInfoDTO> dadosAtuais() {
        return ResponseEntity.ok(coletorMemoria.coletarDadosAtuais());
    }

    @GetMapping("/api/historico")
    @ResponseBody
    public ResponseEntity<List<MetricaMemoria>> historico(
            @RequestParam(defaultValue = "1") int horas) {
        List<MetricaMemoria> dados = memoriaRepo
            .findByCapturadoEmAfterOrderByCapturadoEmAsc(LocalDateTime.now().minusHours(horas));
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/api/processos")
    @ResponseBody
    public ResponseEntity<List<ProcessoMemoria>> topProcessos() {
        return ResponseEntity.ok(processoRepo.findUltimosProcessos());
    }
}
