package com.monitora.controller;

import com.monitora.dto.DiscoInfoDTO;
import com.monitora.dto.MemoriaInfoDTO;
import com.monitora.dto.RedeInfoDTO;
import com.monitora.model.MetricaDisco;
import com.monitora.model.MetricaMemoria;
import com.monitora.repository.MetricaDiscoRepository;
import com.monitora.repository.MetricaMemoriaRepository;
import com.monitora.service.ColetorDiscoService;
import com.monitora.service.ColetorMemoriaService;
import com.monitora.service.ColetorRedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ColetorDiscoService coletorDisco;
    private final ColetorMemoriaService coletorMemoria;
    private final ColetorRedeService coletorRede;
    private final MetricaDiscoRepository discoRepo;
    private final MetricaMemoriaRepository memoriaRepo;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        MemoriaInfoDTO memoria = coletorMemoria.coletarDadosAtuais();
        List<DiscoInfoDTO> discos = coletorDisco.coletarDadosAtuais();
        RedeInfoDTO rede = coletorRede.coletarDadosAtuais();

        model.addAttribute("memoria", memoria);
        model.addAttribute("discos", discos);
        model.addAttribute("rede", rede);
        model.addAttribute("paginaAtiva", "dashboard");

        // Histórico de memória mapeado para tipos simples (evita exceptions do Jackson com LocalDateTime/Entidades)
        List<MetricaMemoria> historico = memoriaRepo
            .findByCapturadoEmAfterOrderByCapturadoEmAsc(LocalDateTime.now().minusHours(1));
            
        List<java.util.Map<String, Object>> historicoMemoria = historico.stream().map(m -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("capturadoEm", m.getCapturadoEm().toString());
            map.put("percentualUso", m.getPercentualUso());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        model.addAttribute("historicoMemoria", historicoMemoria);

        return "dashboard";
    }
}
