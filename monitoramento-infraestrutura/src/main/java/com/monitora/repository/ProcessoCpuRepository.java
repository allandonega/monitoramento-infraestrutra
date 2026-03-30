package com.monitora.repository;

import com.monitora.model.ProcessoCpu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ProcessoCpuRepository extends JpaRepository<ProcessoCpu, Long> {
    @Modifying
    @Query("DELETE FROM ProcessoCpu p WHERE p.capturadoEm < :dataLimite")
    void deleteOlderThan(@Param("dataLimite") LocalDateTime dataLimite);
}
