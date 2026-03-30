package com.monitora.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "metrica_memoria", indexes = {
    @Index(name = "idx_memoria_capturado_em", columnList = "capturado_em")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaMemoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime capturadoEm;

    private Long totalBytes;
    private Long usadoBytes;
    private Long disponivelBytes;
    private Double percentualUso;

    // Swap
    private Long swapTotalBytes;
    private Long swapUsadoBytes;
}
