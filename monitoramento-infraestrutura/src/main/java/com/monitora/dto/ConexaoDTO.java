package com.monitora.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConexaoDTO {
    private String ipLocal;
    private String ipRemoto;
    private int portaLocal;
    private int portaRemota;
    private String protocolo;
    private String estado;
    private String processo;
    private boolean suspeita;
    private String motivoSuspeita;
}
