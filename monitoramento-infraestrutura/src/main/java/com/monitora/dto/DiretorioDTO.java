package com.monitora.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiretorioDTO {
    private String caminho;
    private long tamanhoBytes;
    private int nivel;

    public String getTamanhoFormatado() {
        if (tamanhoBytes < 1024) return tamanhoBytes + " B";
        long kb = tamanhoBytes / 1024;
        if (kb < 1024) return kb + " KB";
        long mb = kb / 1024;
        if (mb < 1024) return mb + " MB";
        return String.format("%.1f GB", (double) mb / 1024);
    }
}
