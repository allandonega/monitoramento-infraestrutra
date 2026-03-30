package com.monitora.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessoDTO {
    private String nome;
    private long pid;
    private long memoriaBytes;
    private double percentualMemoria;
    private String usuario;
    private boolean acimaDeLimite;

    public String getMemoriaFormatada() {
        long mb = memoriaBytes / (1024 * 1024);
        if (mb < 1024) return mb + " MB";
        return String.format("%.1f GB", mb / 1024.0);
    }
}
