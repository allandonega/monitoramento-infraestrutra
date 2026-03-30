package com.monitora.controller;

import com.monitora.dto.RedeInfoDTO;
import com.monitora.model.MetricaRede;
import com.monitora.repository.MetricaRedeRepository;
import com.monitora.service.ColetorRedeService;
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
@RequestMapping("/rede")
@RequiredArgsConstructor
public class RedeController {

    private final ColetorRedeService coletorRede;
    private final MetricaRedeRepository redeRepo;

    @GetMapping
    public String rede(Model model) {
        RedeInfoDTO dados = coletorRede.coletarDadosAtuais();
        model.addAttribute("rede", dados);
        model.addAttribute("paginaAtiva", "rede");
        return "rede";
    }

    @GetMapping("/api/atual")
    @ResponseBody
    public ResponseEntity<RedeInfoDTO> dadosAtuais() {
        return ResponseEntity.ok(coletorRede.coletarDadosAtuais());
    }

    @GetMapping("/api/conexoes")
    @ResponseBody
    public ResponseEntity<List<MetricaRede>> historicoConexoes(
            @RequestParam(defaultValue = "1") int horas) {
        List<MetricaRede> dados = redeRepo
            .findByCapturadoEmAfterAndIpRemotoIsNotNullOrderByCapturadoEmDesc(
                LocalDateTime.now().minusHours(horas));
        return ResponseEntity.ok(dados);
    }
}
