package com.monitora.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CpuInfoDTO {
    private double percentualUso;
    private int qtdNucleos;
    private List<ProcessoCpuDTO> topProcessosMaisCPU;
    private List<ProcessoCpuDTO> topProcessosMenosCPU;
    private List<String> sugestoes;
}
