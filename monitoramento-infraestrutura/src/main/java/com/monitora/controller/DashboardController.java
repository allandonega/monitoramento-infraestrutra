package com.monitora.controller;

import com.monitora.dto.DiscoInfoDTO;
import com.monitora.dto.MemoriaInfoDTO;
import com.monitora.dto.RedeInfoDTO;
import com.monitora.model.MetricaCpu;
import com.monitora.model.MetricaDisco;
import com.monitora.model.MetricaMemoria;
import com.monitora.model.MetricaRede;
import com.monitora.repository.MetricaCpuRepository;
import com.monitora.repository.MetricaDiscoRepository;
import com.monitora.repository.MetricaMemoriaRepository;
import com.monitora.repository.MetricaRedeRepository;
import com.monitora.service.ColetorCpuService;
import com.monitora.service.ColetorDiscoService;
import com.monitora.service.ColetorMemoriaService;
import com.monitora.service.ColetorRedeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private static final ExecutorService DASHBOARD_EXEC = Executors.newCachedThreadPool();

    private final ColetorDiscoService coletorDisco;
    private final ColetorMemoriaService coletorMemoria;
    private final ColetorRedeService coletorRede;
    private final ColetorCpuService coletorCpu;
    private final MetricaDiscoRepository discoRepo;
    private final MetricaMemoriaRepository memoriaRepo;
    private final MetricaCpuRepository cpuRepo;
    private final MetricaRedeRepository redeRepo;

    @Value("${dashboard.coleta.timeout.ms:2000}")
    private long dashboardTimeoutMs;

    @Value("${alerta.cpu.percentual:90}")
    private double alertaCpu;

    @org.springframework.beans.factory.annotation.Value("${alerta.disco.percentual:80}")
    private double alertaDisco;

    @org.springframework.beans.factory.annotation.Value("${alerta.memoria.percentual:85}")
    private double alertaMemoria;
    private <T> T executeWithTimeout(Supplier<T> task, T fallback, String stage) {
        Future<T> future = DASHBOARD_EXEC.submit(task::get);
        try {
            return future.get(dashboardTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("Tempo esgotado no dashboard {} ({} ms). Usando fallback.", stage, dashboardTimeoutMs);
            return fallback;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erro no dashboard {}: {}", stage, e.getMessage(), e);
            return fallback;
        }
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        MemoriaInfoDTO memoria = executeWithTimeout(
            () -> coletorMemoria.coletarDadosAtuais(),
            MemoriaInfoDTO.builder().percentualUso(0.0).build(),
            "memoria");

        List<DiscoInfoDTO> discos = executeWithTimeout(
            () -> coletorDisco.coletarDadosAtuais(),
            List.of(),
            "discos");

        RedeInfoDTO rede = executeWithTimeout(
            () -> coletorRede.coletarDadosAtuais(),
            RedeInfoDTO.builder().conexoes(List.of()).interfaces(List.of()).totalConexoesAtivas(0).totalConexoesFechadas(0).conexoesSuspeitas(List.of()).build(),
            "rede");

        model.addAttribute("memoria", memoria);
        model.addAttribute("discos", discos);
        model.addAttribute("rede", rede);
        model.addAttribute("paginaAtiva", "dashboard");

        List<MetricaMemoria> historicoMemoriaRaw = memoriaRepo
            .findByCapturadoEmAfterOrderByCapturadoEmAsc(LocalDateTime.now().minusMinutes(30));
        List<Map<String, Object>> historicoMemoria = historicoMemoriaRaw.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("capturadoEm", m.getCapturadoEm().toString());
            map.put("percentualUso", m.getPercentualUso());
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("historicoMemoria", historicoMemoria);

        List<MetricaCpu> historicoCpuRaw = cpuRepo.findByCapturadoEmAfterOrderByCapturadoEmAsc(LocalDateTime.now().minusMinutes(30));
        List<Map<String, Object>> historicoCpu = historicoCpuRaw.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("capturadoEm", m.getCapturadoEm().toString());
            map.put("percentualUso", m.getPercentualUso());
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("historicoCpu", historicoCpu);

        List<MetricaRede> historicoRedeRaw = redeRepo.findByCapturadoEmAfterAndIpRemotoIsNotNullOrderByCapturadoEmDesc(LocalDateTime.now().minusMinutes(30));
        List<Map<String, Object>> historicoRede = historicoRedeRaw.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("capturadoEm", m.getCapturadoEm().toString());
            map.put("bytesEnviadosPorSegundo", m.getBytesEnviadosPorSegundo());
            map.put("bytesRecebidosPorSegundo", m.getBytesRecebidosPorSegundo());
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("historicoRede", historicoRede);

        // Alertas de nível
        java.util.List<String> alertas = new java.util.ArrayList<>();
        if (memoria.getPercentualUso() >= alertaMemoria) {
            alertas.add("🔴 Memória alta: " + memoria.getPercentualUso() + "% (limite " + alertaMemoria + "%)");
        }
        if (!discos.isEmpty() && discos.get(0).getPercentualUso() >= alertaDisco) {
            alertas.add("🔴 Disco alto: " + discos.get(0).getPercentualUso() + "% (limite " + alertaDisco + "%)");
        }
        double cpuAtual = executeWithTimeout(
            () -> coletorCpu.coletarDadosAtuais().getPercentualUso(),
            0.0,
            "cpu");
        if (cpuAtual >= alertaCpu) {
            alertas.add("🔴 CPU alta: " + cpuAtual + "% (limite " + alertaCpu + "%)");
        }
        model.addAttribute("alertas", alertas);

        return "dashboard";
    }
}
