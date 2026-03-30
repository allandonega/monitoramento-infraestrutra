package com.monitora.service;

import com.monitora.dto.CpuInfoDTO;
import com.monitora.dto.ProcessoCpuDTO;
import com.monitora.model.MetricaCpu;
import com.monitora.model.ProcessoCpu;
import com.monitora.repository.MetricaCpuRepository;
import com.monitora.repository.ProcessoCpuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
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
public class ColetorCpuService {

    private final MetricaCpuRepository cpuRepository;
    private final ProcessoCpuRepository processoRepository;
    private final SystemInfo systemInfo = new SystemInfo();
    
    private long[] ticksPrev = new long[CentralProcessor.TickType.values().length];

    public CpuInfoDTO coletarDadosAtuais() {
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        OperatingSystem os = systemInfo.getOperatingSystem();

        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(ticksPrev) * 100;
        ticksPrev = processor.getSystemCpuLoadTicks();

        if (Double.isNaN(cpuLoad) || cpuLoad < 0.0) {
            cpuLoad = 0.0;
        }

        int qtdNucleos = processor.getLogicalProcessorCount();

        List<OSProcess> processosList = os.getProcesses();
        
        List<ProcessoCpuDTO> todosProcessosCpu = processosList.stream()
            .map(p -> {
                double procLoad = 100d * (p.getProcessCpuLoadCumulative() / qtdNucleos); 
                if (Double.isNaN(procLoad) || procLoad < 0.0) procLoad = 0.0;
                return ProcessoCpuDTO.builder()
                    .nome(p.getName())
                    .pid(p.getProcessID())
                    .percentualCpu(Math.round(procLoad * 100.0) / 100.0)
                    .usuario(p.getUser())
                    .build();
            })
            .collect(Collectors.toList());

        List<ProcessoCpuDTO> maisCpu = todosProcessosCpu.stream()
            .sorted(Comparator.comparingDouble(ProcessoCpuDTO::getPercentualCpu).reversed())
            .limit(10)
            .collect(Collectors.toList());

        List<ProcessoCpuDTO> menosCpu = todosProcessosCpu.stream()
            .filter(p -> p.getPercentualCpu() >= 0.0)
            .sorted(Comparator.comparingDouble(ProcessoCpuDTO::getPercentualCpu))
            .limit(10)
            .collect(Collectors.toList());

        List<String> sugestoes = new ArrayList<>();
        if (cpuLoad > 85) {
            sugestoes.add("🔴 CPU em sobrecarga! Considere reduzir o número de serviços em execução.");
        } else if (cpuLoad > 60) {
            sugestoes.add("🟡 CPU sob carga moderada.");
        } else {
            sugestoes.add("✅ CPU operando com folga.");
        }

        return CpuInfoDTO.builder()
            .percentualUso(Math.round(cpuLoad * 10.0) / 10.0)
            .qtdNucleos(qtdNucleos)
            .topProcessosMaisCPU(maisCpu)
            .topProcessosMenosCPU(menosCpu)
            .sugestoes(sugestoes)
            .build();
    }

    public void coletarEPersistir() {
        try {
            CpuInfoDTO info = coletarDadosAtuais();
            LocalDateTime agora = LocalDateTime.now();
            
            MetricaCpu metrica = MetricaCpu.builder()
                .capturadoEm(agora)
                .percentualUso(info.getPercentualUso())
                .qtdNucleos(info.getQtdNucleos())
                .build();
            cpuRepository.save(metrica);

            info.getTopProcessosMaisCPU().forEach(p -> {
                if (p.getPercentualCpu() > 0) {
                    ProcessoCpu pc = ProcessoCpu.builder()
                        .capturadoEm(agora)
                        .nomeProcesso(p.getNome())
                        .pid(p.getPid())
                        .percentualCpu(p.getPercentualCpu())
                        .usuario(p.getUsuario())
                        .build();
                    processoRepository.save(pc);
                }
            });
        } catch (Exception e) {
            log.error("Erro ao coletar métricas de CPU: {}", e.getMessage());
        }
    }
}
