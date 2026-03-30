package com.monitora.repository;

import com.monitora.model.ProcessoMemoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessoMemoriaRepository extends JpaRepository<ProcessoMemoria, Long> {

    @Query("SELECT p FROM ProcessoMemoria p WHERE p.capturadoEm = " +
           "(SELECT MAX(p2.capturadoEm) FROM ProcessoMemoria p2) " +
           "ORDER BY p.memoriaBytes DESC")
    List<ProcessoMemoria> findUltimosProcessos();

    @Query("SELECT p FROM ProcessoMemoria p WHERE p.capturadoEm = " +
           "(SELECT MAX(p2.capturadoEm) FROM ProcessoMemoria p2) " +
           "AND p.memoriaBytes >= :thresholdBytes " +
           "ORDER BY p.memoriaBytes DESC")
    List<ProcessoMemoria> findProcessosAcimaThreshold(@Param("thresholdBytes") Long thresholdBytes);

    @Modifying
    @Query("DELETE FROM ProcessoMemoria p WHERE p.capturadoEm < :antes")
    void deleteOlderThan(@Param("antes") LocalDateTime antes);
}
