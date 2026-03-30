package com.monitora.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessoCpuDTO {
    private String nome;
    private long pid;
    private double percentualCpu;
    private String usuario;
}
