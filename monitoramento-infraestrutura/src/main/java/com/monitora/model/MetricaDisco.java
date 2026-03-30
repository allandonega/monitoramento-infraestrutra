package com.monitora.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "metrica_disco", indexes = {
    @Index(name = "idx_disco_capturado_em", columnList = "capturado_em"),
    @Index(name = "idx_disco_particao", columnList = "particao")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaDisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime capturadoEm;

    @Column(length = 100)
    private String particao;

    @Column(length = 20)
    private String sistemaOperacional;

    private Long totalBytes;
    private Long usadoBytes;
    private Long livreBytes;
    private Double percentualUso;
}
