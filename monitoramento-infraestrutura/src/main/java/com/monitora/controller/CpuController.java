package com.monitora.controller;

import com.monitora.dto.CpuInfoDTO;
import com.monitora.model.MetricaCpu;
import com.monitora.repository.MetricaCpuRepository;
import com.monitora.service.ColetorCpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cpu")
@RequiredArgsConstructor
public class CpuController {

    private final ColetorCpuService coletorCpuService;
    private final MetricaCpuRepository cpuRepo;

    @Value("${alerta.cpu.percentual:90}")
    private double alertaCpu;

    @GetMapping
    public String cpuDashboard(Model model) {
        CpuInfoDTO info = coletorCpuService.coletarDadosAtuais();
        model.addAttribute("cpu", info);
        model.addAttribute("paginaAtiva", "cpu");

        List<String> alertas = List.of();
        if (info.getPercentualUso() >= alertaCpu) {
            alertas = List.of("🔴 CPU acima de " + alertaCpu + "% (atual " + info.getPercentualUso() + "%)");
        }
        model.addAttribute("alertas", alertas);

        return "cpu";
    }

    @GetMapping("/api/current")
    @ResponseBody
    public CpuInfoDTO getCurrentCpu() {
        return coletorCpuService.coletarDadosAtuais();
    }

    @GetMapping("/api/historico")
    @ResponseBody
    public ResponseEntity<List<MetricaCpu>> getHistoricoCpu(
            @RequestParam(defaultValue = "1") int horas) {
        LocalDateTime inicio = LocalDateTime.now().minusHours(horas);
        List<MetricaCpu> lista = cpuRepo.findByCapturadoEmAfterOrderByCapturadoEmAsc(inicio);
        return ResponseEntity.ok(lista);
    }
}
