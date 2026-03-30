package com.monitora.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DiscoInfoDTO {
    private String particao;
    private String sistemaOperacional;
    private long totalBytes;
    private long usadoBytes;
    private long livreBytes;
    private double percentualUso;
    private List<DiretorioDTO> topDiretorios;

    public String getTotalFormatado() { return formatarBytes(totalBytes); }
    public String getUsadoFormatado() { return formatarBytes(usadoBytes); }
    public String getLivreFormatado() { return formatarBytes(livreBytes); }

    private String formatarBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        long kb = bytes / 1024;
        if (kb < 1024) return kb + " KB";
        long mb = kb / 1024;
        if (mb < 1024) return mb + " MB";
        long gb = mb / 1024;
        return String.format("%.1f GB", (double) mb / 1024);
    }
}
