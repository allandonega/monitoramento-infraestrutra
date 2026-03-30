package com.monitora.service;

import com.monitora.dto.ConexaoDTO;
import com.monitora.dto.InterfaceDTO;
import com.monitora.dto.RedeInfoDTO;
import com.monitora.model.MetricaRede;
import com.monitora.repository.MetricaRedeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.InternetProtocolStats;
import oshi.software.os.InternetProtocolStats.IPConnection;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSProcess;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColetorRedeService {

    private final MetricaRedeRepository repository;
    private final SystemInfo systemInfo = new SystemInfo();

    // Cache para cálculo de velocidade (bytes por segundo)
    private final Map<String, long[]> cacheAnterior = new HashMap<>();
    private long ultimaColeta = System.currentTimeMillis();

    public RedeInfoDTO coletarDadosAtuais() {
        List<InterfaceDTO> interfaces = coletarInterfaces();
        List<ConexaoDTO> conexoes = coletarConexoes();

        long totalAtivas = conexoes.stream()
            .filter(c -> "ESTABLISHED".equalsIgnoreCase(c.getEstado()))
            .count();
        long totalFechadas = conexoes.stream()
            .filter(c -> "TIME_WAIT".equalsIgnoreCase(c.getEstado()) ||
                         "CLOSE_WAIT".equalsIgnoreCase(c.getEstado()))
            .count();

        List<ConexaoDTO> suspeitas = conexoes.stream()
            .filter(ConexaoDTO::isSuspeita)
            .collect(Collectors.toList());

        return RedeInfoDTO.builder()
            .conexoes(conexoes)
            .interfaces(interfaces)
            .totalConexoesAtivas((int) totalAtivas)
            .totalConexoesFechadas((int) totalFechadas)
            .conexoesSuspeitas(suspeitas)
            .build();
    }

    public void coletarEPersistir() {
        LocalDateTime agora = LocalDateTime.now();

        // Persistir interfaces
        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
        long agora_ms = System.currentTimeMillis();
        long deltaMs = agora_ms - ultimaColeta;
        if (deltaMs <= 0) deltaMs = 1000;

        for (NetworkIF nif : networkIFs) {
            nif.updateAttributes();
            String nome = nif.getName();
            long[] anterior = cacheAnterior.getOrDefault(nome, new long[]{nif.getBytesSent(), nif.getBytesRecv()});

            long bpsEnviado = (nif.getBytesSent() - anterior[0]) * 1000L / deltaMs;
            long bpsRecebido = (nif.getBytesRecv() - anterior[1]) * 1000L / deltaMs;
            bpsEnviado = Math.max(0, bpsEnviado);
            bpsRecebido = Math.max(0, bpsRecebido);

            cacheAnterior.put(nome, new long[]{nif.getBytesSent(), nif.getBytesRecv()});

            String[] ips = nif.getIPv4addr();
            String ip = (ips != null && ips.length > 0) ? ips[0] : "";

            MetricaRede m = MetricaRede.builder()
                .capturadoEm(agora)
                .interfaceNome(nome)
                .ipLocal(ip)
                .bytesEnviadosPorSegundo(bpsEnviado)
                .bytesRecebidosPorSegundo(bpsRecebido)
                .totalBytesEnviados(nif.getBytesSent())
                .totalBytesRecebidos(nif.getBytesRecv())
                .build();
            repository.save(m);
        }
        ultimaColeta = agora_ms;

        // Persistir conexões
        coletarConexoes().forEach(c -> {
            MetricaRede m = MetricaRede.builder()
                .capturadoEm(agora)
                .ipLocal(c.getIpLocal())
                .ipRemoto(c.getIpRemoto())
                .portaLocal(c.getPortaLocal())
                .portaRemota(c.getPortaRemota())
                .protocolo(c.getProtocolo())
                .estado(c.getEstado())
                .processo(c.getProcesso())
                .build();
            repository.save(m);
        });
    }

    private List<InterfaceDTO> coletarInterfaces() {
        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
        long agora_ms = System.currentTimeMillis();
        long deltaMs = agora_ms - ultimaColeta;
        if (deltaMs <= 0) deltaMs = 1000;

        List<InterfaceDTO> resultado = new ArrayList<>();
        for (NetworkIF nif : networkIFs) {
            nif.updateAttributes();
            if (nif.getBytesSent() == 0 && nif.getBytesRecv() == 0) continue;

            String nome = nif.getName();
            long[] anterior = cacheAnterior.getOrDefault(nome, new long[]{nif.getBytesSent(), nif.getBytesRecv()});

            long bpsEnviado = Math.max(0, (nif.getBytesSent() - anterior[0]) * 1000L / deltaMs);
            long bpsRecebido = Math.max(0, (nif.getBytesRecv() - anterior[1]) * 1000L / deltaMs);

            String[] ips = nif.getIPv4addr();
            String ip = (ips != null && ips.length > 0) ? ips[0] : "N/A";

            resultado.add(InterfaceDTO.builder()
                .nome(nome)
                .enderecoIp(ip)
                .bytesEnviadosPorSegundo(bpsEnviado)
                .bytesRecebidosPorSegundo(bpsRecebido)
                .totalEnviados(nif.getBytesSent())
                .totalRecebidos(nif.getBytesRecv())
                .build());
        }
        return resultado;
    }

    private List<ConexaoDTO> coletarConexoes() {
        OperatingSystem os = systemInfo.getOperatingSystem();
        InternetProtocolStats ipStats = os.getInternetProtocolStats();
        List<ConexaoDTO> conexoes = new ArrayList<>();

        for (IPConnection c : ipStats.getConnections()) {
            if (c.getType().startsWith("tcp")) {
                String ipRemoto = formatarIp(c.getForeignAddress());
                int portaLocal = c.getLocalPort();
                String estado = c.getState().name();
                boolean suspeita = isConexaoSuspeita(ipRemoto, portaLocal, estado);
                String motivo = suspeita ? "Acesso externo sensível (Porta " + portaLocal + ") ou estado " + estado : null;

                conexoes.add(ConexaoDTO.builder()
                    .ipLocal(formatarIp(c.getLocalAddress()))
                    .ipRemoto(ipRemoto)
                    .portaLocal(portaLocal)
                    .portaRemota(c.getForeignPort())
                    .protocolo(c.getType().toUpperCase())
                    .estado(estado)
                    .processo(resolverProcesso(c.getowningProcessId()))
                    .suspeita(suspeita)
                    .motivoSuspeita(motivo)
                    .build());
            }
        }

        return conexoes.stream()
            .filter(c -> c.getIpRemoto() != null && !c.getIpRemoto().isEmpty() && !c.getIpRemoto().equals("0.0.0.0") && !c.getIpRemoto().equals("::0") && !c.getIpRemoto().equals("::"))
            .sorted(Comparator.comparing(ConexaoDTO::getEstado))
            .collect(Collectors.toList());
    }

    private String formatarIp(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        try {
            return java.net.InetAddress.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "Desconhecido";
        }
    }

    private boolean isConexaoSuspeita(String ipRemoto, int portaLocal, String estado) {
        if (ipRemoto == null || ipRemoto.isEmpty() || ipRemoto.equals("0.0.0.0") || ipRemoto.startsWith("127.") 
                || ipRemoto.startsWith("192.168.") || ipRemoto.startsWith("10.") || ipRemoto.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*") 
                || ipRemoto.equals("::1") || ipRemoto.equals("::") || ipRemoto.equals("Desconhecido")) {
            return false; // Ignorar IPs locais/privados da heurística principal
        }
        
        // Portas sensíveis (SSH, Telnet, RDP, MySQL, Postgres, MSSQL, Redis, Mongo, Memcached)
        List<Integer> portasSensiveis = Arrays.asList(22, 23, 3389, 3306, 5432, 1433, 6379, 27017, 11211);
        if (portasSensiveis.contains(portaLocal)) {
            return true;
        }
        
        // Possível SYN Flood / Port Scan scan meio aberto
        if ("SYN_RECV".equalsIgnoreCase(estado)) {
            return true;
        }

        return false;
    }

    private String resolverProcesso(int pid) {
        if (pid <= 0) return "Sistema";
        try {
            OSProcess p = systemInfo.getOperatingSystem().getProcess(pid);
            return p != null ? p.getName() + " (PID " + pid + ")" : "PID " + pid;
        } catch (Exception e) {
            return "PID " + pid;
        }
    }
}
