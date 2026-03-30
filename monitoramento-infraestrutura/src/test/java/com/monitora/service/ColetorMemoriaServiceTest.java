package com.monitora.service;

import com.monitora.dto.MemoriaInfoDTO;
import com.monitora.repository.MetricaMemoriaRepository;
import com.monitora.repository.ProcessoMemoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ColetorMemoriaServiceTest {

    @Mock
    private MetricaMemoriaRepository repository;

    @Mock
    private ProcessoMemoriaRepository processoRepository;

    @InjectMocks
    private ColetorMemoriaService service;

    @Test
    void testColetarDadosAtuais_ReturnsMemoriaStats() {
        MemoriaInfoDTO stats = service.coletarDadosAtuais();
        
        assertNotNull(stats);
        assertTrue(stats.getTotalBytes() > 0, "A memória total deve ser maior que zero");
        assertTrue(stats.getDisponivelBytes() >= 0, "Memória disponível pode ser maior ou igual a zero");
        assertNotNull(stats.getTopProcessos());
    }
}
