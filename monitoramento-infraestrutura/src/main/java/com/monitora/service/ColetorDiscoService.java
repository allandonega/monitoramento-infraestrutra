package com.monitora.service;

import com.monitora.dto.DiretorioDTO;
import com.monitora.dto.DiscoInfoDTO;
import com.monitora.model.MetricaDisco;
import com.monitora.repository.MetricaDiscoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColetorDiscoService {

    private final MetricaDiscoRepository repository;
    private final SystemInfo systemInfo = new SystemInfo();

    public List<DiscoInfoDTO> coletarDadosAtuais() {
        OperatingSystem os = systemInfo.getOperatingSystem();
        List<OSFileStore> fileStores = os.getFileSystem().getFileStores();
        String nomeOS = systemInfo.getOperatingSystem().getFamily();

        return fileStores.stream()
            .filter(fs -> fs.getTotalSpace() > 0)
            .map(fs -> {
                long total = fs.getTotalSpace();
                long disponivel = fs.getUsableSpace();
                long usado = total - disponivel;
                double percentual = total > 0 ? (double) usado / total * 100.0 : 0;

                return DiscoInfoDTO.builder()
                    .particao(fs.getMount())
                    .sistemaOperacional(nomeOS)
                    .totalBytes(total)
                    .usadoBytes(usado)
                    .livreBytes(disponivel)
                    .percentualUso(Math.round(percentual * 10.0) / 10.0)
                    .topDiretorios(new ArrayList<>())
                    .build();
            })
            .collect(Collectors.toList());
    }

    public MetricaDisco coletarEPersistir(String particao) {
        OperatingSystem os = systemInfo.getOperatingSystem();
        String nomeOS = systemInfo.getOperatingSystem().getFamily();

        return os.getFileSystem().getFileStores().stream()
            .filter(fs -> fs.getMount().equals(particao))
            .findFirst()
            .map(fs -> {
                long total = fs.getTotalSpace();
                long disponivel = fs.getUsableSpace();
                long usado = total - disponivel;
                double percentual = total > 0 ? (double) usado / total * 100.0 : 0;

                MetricaDisco metrica = MetricaDisco.builder()
                    .capturadoEm(LocalDateTime.now())
                    .particao(particao)
                    .sistemaOperacional(nomeOS)
                    .totalBytes(total)
                    .usadoBytes(usado)
                    .livreBytes(disponivel)
                    .percentualUso(Math.round(percentual * 10.0) / 10.0)
                    .build();

                return repository.save(metrica);
            })
            .orElse(null);
    }

    public List<String> listarParticoes() {
        return systemInfo.getOperatingSystem().getFileSystem().getFileStores()
            .stream()
            .filter(fs -> fs.getTotalSpace() > 0)
            .map(OSFileStore::getMount)
            .collect(Collectors.toList());
    }

    public List<DiretorioDTO> listarTopDiretorios(String raiz, int limite) {
        List<DiretorioDTO> resultado = new ArrayList<>();
        try {
            File dir = new File(raiz);
            File[] subdirs = dir.listFiles(File::isDirectory);
            if (subdirs == null) return resultado;

            List<File> dirs = Arrays.stream(subdirs)
                .filter(f -> !f.isHidden())
                .collect(Collectors.toList());

            Map<File, Long> tamanhos = new HashMap<>();
            for (File subdir : dirs) {
                tamanhos.put(subdir, calcularTamanho(subdir));
            }

            resultado = tamanhos.entrySet().stream()
                .sorted(Map.Entry.<File, Long>comparingByValue().reversed())
                .limit(limite)
                .map(e -> DiretorioDTO.builder()
                    .caminho(e.getKey().getAbsolutePath())
                    .tamanhoBytes(e.getValue())
                    .nivel(1)
                    .build())
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Erro ao listar diretórios de {}: {}", raiz, e.getMessage());
        }
        return resultado;
    }

    private long calcularTamanho(File dir) {
        long tamanho = 0;
        try {
            File[] arquivos = dir.listFiles();
            if (arquivos == null) return 0;
            for (File f : arquivos) {
                if (f.isFile()) {
                    tamanho += f.length();
                } else if (f.isDirectory() && !f.getName().startsWith(".")) {
                    tamanho += calcularTamanho(f);
                }
            }
        } catch (SecurityException e) {
            // Sem permissão — ignora silenciosamente
        }
        return tamanho;
    }
}
