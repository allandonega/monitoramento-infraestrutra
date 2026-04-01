package com.monitora.service;

import com.monitora.repository.MetricaDiscoRepository;
import com.monitora.repository.MetricaMemoriaRepository;
import com.monitora.repository.MetricaRedeRepository;
import com.monitora.repository.ProcessoMemoriaRepository;
import com.monitora.repository.MetricaCpuRepository;
import com.monitora.repository.ProcessoCpuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricasScheduler {

    private final ColetorDiscoService coletorDisco;
    private final ColetorMemoriaService coletorMemoria;
    private final ColetorRedeService coletorRede;
    private final ColetorCpuService coletorCpu;
    private final AlertaService alertaService;
    private final MetricaDiscoRepository discoRepo;
    private final MetricaMemoriaRepository memoriaRepo;
    private final MetricaRedeRepository redeRepo;
    private final ProcessoMemoriaRepository processoRepo;
    private final MetricaCpuRepository cpuRepo;
    private final ProcessoCpuRepository processoCpuRepo;

    @Value("${historico.retencao.dias:30}")
    private int retencaoDias;

    // Disco: a cada 5 minutos
    @Scheduled(fixedDelayString = "${coleta.disco.intervalo:300000}")
    public void coletarDisco() {
        try {
            coletorDisco.listarParticoes().forEach(coletorDisco::coletarEPersistir);
            log.debug("Métricas de disco coletadas");
        } catch (Exception e) {
            log.error("Erro ao coletar disco: {}", e.getMessage());
        }
    }

    // Memória: a cada 30 segundos
    @Scheduled(fixedDelayString = "${coleta.memoria.intervalo:30000}")
    public void coletarMemoria() {
        try {
            coletorMemoria.coletarEPersistir();
            log.debug("Métricas de memória coletadas");
        } catch (Exception e) {
            log.error("Erro ao coletar memória: {}", e.getMessage());
        }
    }

    // Rede: a cada 10 segundos
    @Scheduled(fixedDelayString = "${coleta.rede.intervalo:10000}")
    public void coletarRede() {
        try {
            coletorRede.coletarEPersistir();
            log.debug("Métricas de rede coletadas");
        } catch (Exception e) {
            log.error("Erro ao coletar rede: {}", e.getMessage());
        }
    }

    // CPU: a cada 10 segundos
    @Scheduled(fixedDelayString = "${coleta.cpu.intervalo:10000}")
    public void coletarCpu() {
        try {
            coletorCpu.coletarEPersistir();
            alertaService.verificarEEnviarAlertas();
            log.debug("Métricas de CPU coletadas");
        } catch (Exception e) {
            log.error("Erro ao coletar CPU: {}", e.getMessage());
        }
    }

    // Limpeza de histórico: uma vez por dia à meia-noite
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void limparHistoricoAntigo() {
        LocalDateTime limite = LocalDateTime.now().minusDays(retencaoDias);
        log.info("Limpando histórico anterior a: {}", limite);
        discoRepo.deleteOlderThan(limite);
        memoriaRepo.deleteOlderThan(limite);
        redeRepo.deleteOlderThan(limite);
        processoRepo.deleteOlderThan(limite);
        cpuRepo.deleteOlderThan(limite);
        processoCpuRepo.deleteOlderThan(limite);
    }
}
