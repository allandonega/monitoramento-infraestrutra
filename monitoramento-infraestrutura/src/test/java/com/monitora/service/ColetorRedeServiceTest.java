package com.monitora.service;

import com.monitora.dto.RedeInfoDTO;
import com.monitora.repository.MetricaRedeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ColetorRedeServiceTest {

    @Mock
    private MetricaRedeRepository repository;

    @InjectMocks
    private ColetorRedeService service;

    @Test
    void testColetarDadosAtuais_ReturnsInterfacesAndConexoes() {
        RedeInfoDTO info = service.coletarDadosAtuais();
        
        assertNotNull(info);
        assertNotNull(info.getInterfaces());
        assertNotNull(info.getConexoes());
        assertNotNull(info.getConexoesSuspeitas(), "A lista de suspeitas não deve ser nula");
        
        assertTrue(info.getTotalConexoesAtivas() >= 0);
        assertTrue(info.getTotalConexoesFechadas() >= 0);
    }
}
