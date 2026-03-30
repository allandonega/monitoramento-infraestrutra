package com.monitora.repository;

import com.monitora.model.MetricaDisco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricaDiscoRepository extends JpaRepository<MetricaDisco, Long> {

    List<MetricaDisco> findByCapturadoEmAfterOrderByCapturadoEmAsc(LocalDateTime after);

    List<MetricaDisco> findByParticaoAndCapturadoEmBetweenOrderByCapturadoEmAsc(
        String particao, LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT m FROM MetricaDisco m WHERE m.capturadoEm = " +
           "(SELECT MAX(m2.capturadoEm) FROM MetricaDisco m2)")
    List<MetricaDisco> findUltimasMedidas();

    @Modifying
    @Query("DELETE FROM MetricaDisco m WHERE m.capturadoEm < :antes")
    void deleteOlderThan(@Param("antes") LocalDateTime antes);
}
