package com.monitora.controller;

import com.monitora.dto.CpuInfoDTO;
import com.monitora.model.MetricaCpu;
import com.monitora.repository.MetricaCpuRepository;
import com.monitora.service.ColetorCpuService;
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
@RequestMapping("/cpu")
@RequiredArgsConstructor
public class CpuController {

    private final ColetorCpuService coletorCpuService;
    private final MetricaCpuRepository cpuRepo;

    @GetMapping
    public String cpuDashboard(Model model) {
        CpuInfoDTO info = coletorCpuService.coletarDadosAtuais();
        model.addAttribute("cpu", info);
        model.addAttribute("paginaAtiva", "cpu");

        List<MetricaCpu> historico = cpuRepo.findByCapturadoEmAfterOrderByCapturadoEmAsc(
                LocalDateTime.now().minusHours(1));
        List<Map<String, Object>> historicoMapped = historico.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("capturadoEm", m.getCapturadoEm().toString());
            map.put("percentualUso", m.getPercentualUso());
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("historicoCpu", historicoMapped);

        return "cpu";
    }

    @GetMapping("/api/current")
    @ResponseBody
    public CpuInfoDTO getCurrentCpu() {
        return coletorCpuService.coletarDadosAtuais();
    }

    @GetMapping("/api/historico")
    @ResponseBody
    public ResponseEntity<List<MetricaCpu>> historico(
            @RequestParam(defaultValue = "1") int horas) {
        return ResponseEntity.ok(cpuRepo.findByCapturadoEmAfterOrderByCapturadoEmAsc(
                LocalDateTime.now().minusHours(horas)));
    }
}
