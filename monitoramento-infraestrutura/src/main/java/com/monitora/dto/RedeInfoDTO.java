package com.monitora.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RedeInfoDTO {
    private List<ConexaoDTO> conexoes;
    private List<InterfaceDTO> interfaces;
    private int totalConexoesAtivas;
    private int totalConexoesFechadas;
    private List<ConexaoDTO> conexoesSuspeitas;
}
