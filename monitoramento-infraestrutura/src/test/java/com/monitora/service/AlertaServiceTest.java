package com.monitora.service;

import com.monitora.model.MetricaCpu;
import com.monitora.model.MetricaDisco;
import com.monitora.model.MetricaMemoria;
import com.monitora.repository.MetricaCpuRepository;
import com.monitora.repository.MetricaDiscoRepository;
import com.monitora.repository.MetricaMemoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AlertaServiceTest {

    @Mock
    private MetricaCpuRepository cpuRepo;

    @Mock
    private MetricaDiscoRepository discoRepo;

    @Mock
    private MetricaMemoriaRepository memoriaRepo;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AlertaService alertaService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void quandoAlertaEnviadoViaEmailEntaoMailSenderEhChamado() {
        MetricaCpu metricaCpu = new MetricaCpu();
        metricaCpu.setPercentualUso(95.0);
        metricaCpu.setCapturadoEm(LocalDateTime.now());

        MetricaDisco disco = new MetricaDisco();
        disco.setPercentualUso(70.0);
        disco.setCapturadoEm(LocalDateTime.now());

        MetricaMemoria memoria = new MetricaMemoria();
        memoria.setPercentualUso(60.0);
        memoria.setCapturadoEm(LocalDateTime.now());

        when(cpuRepo.findTop1ByOrderByCapturadoEmDesc()).thenReturn(metricaCpu);
        when(discoRepo.findUltimasMedidas()).thenReturn(List.of(disco));
        when(memoriaRepo.findTopByOrderByCapturadoEmDesc()).thenReturn(Optional.of(memoria));

        ReflectionTestUtils.setField(alertaService, "alertaCpu", 90.0);
        ReflectionTestUtils.setField(alertaService, "alertaDisco", 80.0);
        ReflectionTestUtils.setField(alertaService, "alertaMemoria", 85.0);
        ReflectionTestUtils.setField(alertaService, "alertaEmailEnabled", true);
        ReflectionTestUtils.setField(alertaService, "alertaEmailTo", "ops-team@empresa.com");
        ReflectionTestUtils.setField(alertaService, "alertaWebhookEnabled", false);

        alertaService.verificarEEnviarAlertas();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains("CPU:"));
        assertTrue(message.getTo()[0].equals("ops-team@empresa.com"));
    }
}
