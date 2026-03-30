package com.monitora.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "processo_memoria", indexes = {
    @Index(name = "idx_proc_mem_capturado_em", columnList = "capturado_em"),
    @Index(name = "idx_proc_mem_nome", columnList = "nome_processo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessoMemoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime capturadoEm;

    @Column(name = "nome_processo", length = 200)
    private String nomeProcesso;

    private Long pid;
    private Long memoriaBytes;
    private Double percentualMemoria;
    private String usuario;
}
