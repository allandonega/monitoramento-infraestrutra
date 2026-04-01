package com.monitora.repository;

import com.monitora.model.MetricaCpu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MetricaCpuRepository extends JpaRepository<MetricaCpu, Long> {
    List<MetricaCpu> findTop100ByOrderByCapturadoEmAsc();

    List<MetricaCpu> findByCapturadoEmAfterOrderByCapturadoEmAsc(LocalDateTime after);
    List<MetricaCpu> findByCapturadoEmBetweenOrderByCapturadoEmAsc(LocalDateTime inicio, LocalDateTime fim);

    MetricaCpu findTop1ByOrderByCapturadoEmDesc();

    @Modifying
    @Query("DELETE FROM MetricaCpu m WHERE m.capturadoEm < :dataLimite")
    void deleteOlderThan(@Param("dataLimite") LocalDateTime dataLimite);
}
