package com.monitora.controller;

import com.monitora.dto.DiretorioDTO;
import com.monitora.dto.DiscoInfoDTO;
import com.monitora.model.MetricaDisco;
import com.monitora.repository.MetricaDiscoRepository;
import com.monitora.service.ColetorDiscoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/disco")
@RequiredArgsConstructor
public class DiscoController {

    private final ColetorDiscoService coletorDisco;
    private final MetricaDiscoRepository repository;

    @GetMapping
    public String disco(Model model) {
        List<DiscoInfoDTO> discos = coletorDisco.coletarDadosAtuais();
        model.addAttribute("discos", discos);
        model.addAttribute("paginaAtiva", "disco");

        List<MetricaDisco> historico = repository.findByCapturadoEmAfterOrderByCapturadoEmAsc(
                LocalDateTime.now().minusHours(24));
        List<Map<String, Object>> historicoMapped = historico.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("capturadoEm", m.getCapturadoEm().toString());
            map.put("particao", m.getParticao());
            map.put("percentualUso", m.getPercentualUso());
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("historicoDisco", historicoMapped);

        return "disco";
    }

    @GetMapping("/api/atual")
    @ResponseBody
    public ResponseEntity<List<DiscoInfoDTO>> dadosAtuais() {
        return ResponseEntity.ok(coletorDisco.coletarDadosAtuais());
    }

    @GetMapping("/api/historico")
    @ResponseBody
    public ResponseEntity<List<MetricaDisco>> historico(
            @RequestParam(defaultValue = "24") int horas) {
        List<MetricaDisco> dados = repository.findByCapturadoEmAfterOrderByCapturadoEmAsc(
            LocalDateTime.now().minusHours(horas));
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/api/diretorios")
    @ResponseBody
    public ResponseEntity<List<DiretorioDTO>> topDiretorios(
            @RequestParam String particao,
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(coletorDisco.listarTopDiretorios(particao, limite));
    }
}
