package com.monitora.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teste_velocidade", indexes = {
    @Index(name = "idx_teste_vel_executado_em", columnList = "executado_em")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TesteVelocidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTeste tipo;

    private double velocidadeMbps;
    private long bytesTransferidos;
    private long duracaoMs;
    private double latenciaMs;

    @Column(name = "executado_em", nullable = false)
    private LocalDateTime executadoEm;

    public enum TipoTeste {
        DOWNLOAD, UPLOAD, PING
    }
}
