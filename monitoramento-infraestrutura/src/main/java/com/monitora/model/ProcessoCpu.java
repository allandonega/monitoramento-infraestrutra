package com.monitora.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processo_cpu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessoCpu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime capturadoEm;
    private String nomeProcesso;
    private Long pid;
    private double percentualCpu;
    private String usuario;
}
