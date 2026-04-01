package com.monitora.repository;

import com.monitora.model.MetricaRede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricaRedeRepository extends JpaRepository<MetricaRede, Long> {

    @Query("SELECT m FROM MetricaRede m WHERE m.capturadoEm = " +
           "(SELECT MAX(m2.capturadoEm) FROM MetricaRede m2) " +
           "AND m.ipRemoto IS NOT NULL " +
           "ORDER BY m.capturadoEm DESC")
    List<MetricaRede> findConexoesAtuais();

    @Query("SELECT m FROM MetricaRede m WHERE m.capturadoEm = " +
           "(SELECT MAX(m2.capturadoEm) FROM MetricaRede m2) " +
           "AND m.ipRemoto IS NULL " +
           "ORDER BY m.interfaceNome ASC")
    List<MetricaRede> findTrafegoInterfaces();

    List<MetricaRede> findByCapturadoEmAfterAndIpRemotoIsNotNullOrderByCapturadoEmDesc(
        LocalDateTime after);

    List<MetricaRede> findBySuspeitaTrueAndCapturadoEmAfterOrderByCapturadoEmDesc(LocalDateTime after);

    @Modifying
    @Query("DELETE FROM MetricaRede m WHERE m.capturadoEm < :antes")
    void deleteOlderThan(@Param("antes") LocalDateTime antes);
}
