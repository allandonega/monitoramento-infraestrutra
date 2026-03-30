package com.monitora.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "metrica_rede", indexes = {
    @Index(name = "idx_rede_capturado_em", columnList = "capturado_em"),
    @Index(name = "idx_rede_ip_remoto", columnList = "ip_remoto")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaRede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime capturadoEm;

    @Column(name = "interface_nome", length = 100)
    private String interfaceNome;

    // Tráfego por interface
    private Long bytesEnviadosPorSegundo;
    private Long bytesRecebidosPorSegundo;
    private Long totalBytesEnviados;
    private Long totalBytesRecebidos;

    // Conexão individual
    @Column(name = "ip_local", length = 50)
    private String ipLocal;

    @Column(name = "ip_remoto", length = 50)
    private String ipRemoto;

    private Integer portaLocal;
    private Integer portaRemota;

    @Column(length = 10)
    private String protocolo;

    @Column(length = 30)
    private String estado;

    @Column(length = 200)
    private String processo;
}
