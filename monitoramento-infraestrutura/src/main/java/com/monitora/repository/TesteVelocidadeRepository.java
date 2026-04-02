package com.monitora.repository;

import com.monitora.model.TesteVelocidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TesteVelocidadeRepository extends JpaRepository<TesteVelocidade, Long> {
    List<TesteVelocidade> findTop50ByOrderByExecutadoEmDesc();
    List<TesteVelocidade> findByExecutadoEmAfterOrderByExecutadoEmAsc(LocalDateTime depois);
    List<TesteVelocidade> findByTipoAndExecutadoEmAfterOrderByExecutadoEmAsc(
        TesteVelocidade.TipoTeste tipo, LocalDateTime depois);

    @Modifying
    @Query("DELETE FROM TesteVelocidade t WHERE t.executadoEm < :antes")
    void deleteOlderThan(@Param("antes") LocalDateTime antes);
}
