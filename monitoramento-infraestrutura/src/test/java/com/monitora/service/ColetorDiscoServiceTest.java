package com.monitora.service;

import com.monitora.dto.DiscoInfoDTO;
import com.monitora.repository.MetricaDiscoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ColetorDiscoServiceTest {

    @Mock
    private MetricaDiscoRepository repository;

    @InjectMocks
    private ColetorDiscoService service;

    @Test
    void testColetarDadosAtuais_ReturnsDiscos() {
        List<DiscoInfoDTO> discos = service.coletarDadosAtuais();
        
        assertNotNull(discos);
        assertFalse(discos.isEmpty(), "Deve retornar ao menos um disco");
        
        DiscoInfoDTO primeiro = discos.get(0);
        assertNotNull(primeiro.getParticao());
        assertTrue(primeiro.getTotalBytes() > 0, "O tamanho total do disco deve ser maior que zero");
    }
}
