package com.monitora.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MemoriaInfoDTO {
    private long totalBytes;
    private long usadoBytes;
    private long disponivelBytes;
    private double percentualUso;
    private long swapTotalBytes;
    private long swapUsadoBytes;
    private List<ProcessoDTO> topProcessos;
    private List<String> sugestoes;

    public String getTotalFormatado() { return formatarBytes(totalBytes); }
    public String getUsadoFormatado() { return formatarBytes(usadoBytes); }
    public String getDisponivelFormatado() { return formatarBytes(disponivelBytes); }
    public String getSwapTotalFormatado() { return formatarBytes(swapTotalBytes); }
    public String getSwapUsadoFormatado() { return formatarBytes(swapUsadoBytes); }

    private String formatarBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        long kb = bytes / 1024;
        if (kb < 1024) return kb + " KB";
        long mb = kb / 1024;
        if (mb < 1024) return mb + " MB";
        return String.format("%.1f GB", (double) mb / 1024);
    }
}
