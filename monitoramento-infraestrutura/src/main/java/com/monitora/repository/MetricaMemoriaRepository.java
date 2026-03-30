package com.monitora.repository;

import com.monitora.model.MetricaMemoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetricaMemoriaRepository extends JpaRepository<MetricaMemoria, Long> {

    List<MetricaMemoria> findByCapturadoEmAfterOrderByCapturadoEmAsc(LocalDateTime after);

    List<MetricaMemoria> findByCapturadoEmBetweenOrderByCapturadoEmAsc(
        LocalDateTime inicio, LocalDateTime fim);

    Optional<MetricaMemoria> findTopByOrderByCapturadoEmDesc();

    @Modifying
    @Query("DELETE FROM MetricaMemoria m WHERE m.capturadoEm < :antes")
    void deleteOlderThan(@Param("antes") LocalDateTime antes);
}
