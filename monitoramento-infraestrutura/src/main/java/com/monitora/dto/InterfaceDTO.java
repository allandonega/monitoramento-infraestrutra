package com.monitora.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterfaceDTO {
    private String nome;
    private String enderecoIp;
    private long bytesEnviadosPorSegundo;
    private long bytesRecebidosPorSegundo;
    private long totalEnviados;
    private long totalRecebidos;

    public String getEnvioFormatado() { return formatarVelocidade(bytesEnviadosPorSegundo); }
    public String getRecebimentoFormatado() { return formatarVelocidade(bytesRecebidosPorSegundo); }

    private String formatarVelocidade(long bps) {
        if (bps < 1024) return bps + " B/s";
        long kbps = bps / 1024;
        if (kbps < 1024) return kbps + " KB/s";
        return String.format("%.1f MB/s", kbps / 1024.0);
    }
}
