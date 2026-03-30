package com.monitora.service;

import com.monitora.dto.MemoriaInfoDTO;
import com.monitora.dto.ProcessoDTO;
import com.monitora.model.MetricaMemoria;
import com.monitora.model.ProcessoMemoria;
import com.monitora.repository.MetricaMemoriaRepository;
import com.monitora.repository.ProcessoMemoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColetorMemoriaService {

    private final MetricaMemoriaRepository memoriaRepository;
    private final ProcessoMemoriaRepository processoRepository;
    private final SystemInfo systemInfo = new SystemInfo();

    @Value("${alerta.processo.memoria.mb:500}")
    private long alertaProcessoMb;

    public MemoriaInfoDTO coletarDadosAtuais() {
        GlobalMemory memoria = systemInfo.getHardware().getMemory();
        VirtualMemory swap = memoria.getVirtualMemory();
        OperatingSystem os = systemInfo.getOperatingSystem();

        long total = memoria.getTotal();
        long disponivel = memoria.getAvailable();
        long usado = total - disponivel;
        double percentual = total > 0 ? (double) usado / total * 100.0 : 0;

        List<OSProcess> processos = os.getProcesses();
        List<ProcessoDTO> topProcessos = processos.stream()
            .sorted(Comparator.comparingLong(OSProcess::getResidentSetSize).reversed())
            .limit(20)
            .map(p -> {
                long memProc = p.getResidentSetSize();
                double percProc = total > 0 ? (double) memProc / total * 100.0 : 0;
                long thresholdBytes = alertaProcessoMb * 1024 * 1024;
                return ProcessoDTO.builder()
                    .nome(p.getName())
                    .pid(p.getProcessID())
                    .memoriaBytes(memProc)
                    .percentualMemoria(Math.round(percProc * 10.0) / 10.0)
                    .usuario(p.getUser())
                    .acimaDeLimite(memProc > thresholdBytes)
                    .build();
            })
            .collect(Collectors.toList());

        List<String> sugestoes = gerarSugestoes(topProcessos, percentual);

        return MemoriaInfoDTO.builder()
            .totalBytes(total)
            .usadoBytes(usado)
            .disponivelBytes(disponivel)
            .percentualUso(Math.round(percentual * 10.0) / 10.0)
            .swapTotalBytes(swap.getSwapTotal())
            .swapUsadoBytes(swap.getSwapUsed())
            .topProcessos(topProcessos)
            .sugestoes(sugestoes)
            .build();
    }

    public void coletarEPersistir() {
        GlobalMemory memoria = systemInfo.getHardware().getMemory();
        VirtualMemory swap = memoria.getVirtualMemory();
        OperatingSystem os = systemInfo.getOperatingSystem();

        long total = memoria.getTotal();
        long disponivel = memoria.getAvailable();
        long usado = total - disponivel;
        double percentual = total > 0 ? (double) usado / total * 100.0 : 0;
        LocalDateTime agora = LocalDateTime.now();

        MetricaMemoria metrica = MetricaMemoria.builder()
            .capturadoEm(agora)
            .totalBytes(total)
            .usadoBytes(usado)
            .disponivelBytes(disponivel)
            .percentualUso(Math.round(percentual * 10.0) / 10.0)
            .swapTotalBytes(swap.getSwapTotal())
            .swapUsadoBytes(swap.getSwapUsed())
            .build();
        memoriaRepository.save(metrica);

        // Salvar top processos
        List<OSProcess> processos = os.getProcesses();
        long thresholdBytes = alertaProcessoMb * 1024 * 1024;

        processos.stream()
            .sorted(Comparator.comparingLong(OSProcess::getResidentSetSize).reversed())
            .limit(50)
            .forEach(p -> {
                long memProc = p.getResidentSetSize();
                if (memProc > 0) {
                    double percProc = total > 0 ? (double) memProc / total * 100.0 : 0;
                    ProcessoMemoria pm = ProcessoMemoria.builder()
                        .capturadoEm(agora)
                        .nomeProcesso(p.getName())
                        .pid((long) p.getProcessID())
                        .memoriaBytes(memProc)
                        .percentualMemoria(Math.round(percProc * 10.0) / 10.0)
                        .usuario(p.getUser())
                        .build();
                    processoRepository.save(pm);
                }
            });
    }

    private List<String> gerarSugestoes(List<ProcessoDTO> processos, double percentualMemoria) {
        List<String> sugestoes = new ArrayList<>();

        if (percentualMemoria > 85) {
            sugestoes.add("🔴 Uso crítico de memória (" + String.format("%.0f%%", percentualMemoria) + "). Considere reiniciar aplicações não essenciais.");
        } else if (percentualMemoria > 70) {
            sugestoes.add("🟡 Uso de memória elevado (" + String.format("%.0f%%", percentualMemoria) + "). Monitore processos em crescimento.");
        }

        processos.stream()
            .filter(ProcessoDTO::isAcimaDeLimite)
            .limit(3)
            .forEach(p -> sugestoes.add(
                "⚠️ Processo '" + p.getNome() + "' (PID " + p.getPid() + ") está usando " +
                p.getMemoriaFormatada() + ". Verifique se é esperado ou reinicie o processo."
            ));

        long emRepousoOuDesnecessarios = processos.stream()
            .filter(p -> p.getMemoriaBytes() < 10 * 1024 * 1024) // < 10MB
            .count();
        if (emRepousoOuDesnecessarios > 5) {
            sugestoes.add("💡 Há " + emRepousoOuDesnecessarios + " processos usando muito pouca memória. Considere fechar os desnecessários.");
        }

        if (sugestoes.isEmpty()) {
            sugestoes.add("✅ Uso de memória dentro dos parâmetros normais.");
        }

        return sugestoes;
    }
}
