package com.monitora.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "metrica_cpu", indexes = {
    @Index(name = "idx_cpu_capturado_em", columnList = "capturado_em")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaCpu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime capturadoEm;
    private double percentualUso;
    private int qtdNucleos;
}
