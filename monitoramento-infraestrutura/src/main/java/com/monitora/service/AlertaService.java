package com.monitora.service;

import com.monitora.model.MetricaCpu;
import com.monitora.model.MetricaDisco;
import com.monitora.model.MetricaMemoria;
import com.monitora.repository.MetricaCpuRepository;
import com.monitora.repository.MetricaDiscoRepository;
import com.monitora.repository.MetricaMemoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaService {

    private final MetricaCpuRepository cpuRepo;
    private final MetricaDiscoRepository discoRepo;
    private final MetricaMemoriaRepository memoriaRepo;
    private final JavaMailSender mailSender;

    @Value("${alerta.cpu.percentual:90}")
    private double alertaCpu;

    @Value("${alerta.disco.percentual:80}")
    private double alertaDisco;

    @Value("${alerta.memoria.percentual:85}")
    private double alertaMemoria;

    @Value("${alerta.email.enabled:false}")
    private boolean alertaEmailEnabled;

    @Value("${alerta.email.to:}")
    private String alertaEmailTo;

    @Value("${alerta.webhook.enabled:false}")
    private boolean alertaWebhookEnabled;

    @Value("${alerta.webhook.url:}")
    private String alertaWebhookUrl;

    @Value("${alerta.cooldown.segundos:300}")
    private long alertaCooldownSegundos;

    private final Map<String, LocalDateTime> ultimoAlertaMap = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();

    @org.springframework.scheduling.annotation.Async
    public void verificarEEnviarAlertas() {
        try {
            StringBuilder mensagem = new StringBuilder();

            MetricaCpu ultimaCpu = cpuRepo.findTop1ByOrderByCapturadoEmDesc();
            if (ultimaCpu != null && ultimaCpu.getPercentualUso() >= alertaCpu) {
                mensagem.append("CPU: ").append(ultimaCpu.getPercentualUso()).append("%\n");
            }

            List<MetricaDisco> discos = discoRepo.findUltimasMedidas();
            if (!discos.isEmpty()) {
                MetricaDisco ultDisco = discos.get(0);
                if (ultDisco.getPercentualUso() >= alertaDisco) {
                    mensagem.append("Disco (" + ultDisco.getParticao() + "): ")
                        .append(ultDisco.getPercentualUso()).append("%\n");
                }
            }

            MetricaMemoria ultimaMem = memoriaRepo.findTopByOrderByCapturadoEmDesc().orElse(null);
            if (ultimaMem != null && ultimaMem.getPercentualUso() >= alertaMemoria) {
                mensagem.append("Memória: ").append(ultimaMem.getPercentualUso()).append("%\n");
            }

            if (mensagem.length() == 0) {
                return;
            }

            String assunto = "Alerta de Infraestrutura - " + LocalDateTime.now();
            String corpo = mensagem.toString();

            String key = "CPU_DISCO_MEMORIA";
            LocalDateTime ultima = ultimoAlertaMap.get(key);
            if (ultima != null && Duration.between(ultima, LocalDateTime.now()).getSeconds() < alertaCooldownSegundos) {
                log.debug("Alerta omitido (cooldown em vigor): {} segundos restantes", 
                    alertaCooldownSegundos - Duration.between(ultima, LocalDateTime.now()).getSeconds());
                return;
            }
            ultimoAlertaMap.put(key, LocalDateTime.now());

            if (alertaEmailEnabled && alertaEmailTo != null && !alertaEmailTo.isEmpty()) {
                enviarEmail(assunto, corpo);
            }

            if (alertaWebhookEnabled && alertaWebhookUrl != null && !alertaWebhookUrl.isBlank()) {
                enviarWebhook(assunto, corpo);
            }

            log.warn("Alerta acionado:\n{}", corpo);
        } catch (Exception e) {
            log.error("Erro ao verificar/enviar alertas: {}", e.getMessage(), e);
        }
    }

    private void enviarEmail(String assunto, String corpo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertaEmailTo.split(","));
            message.setSubject(assunto);
            message.setText(corpo);
            mailSender.send(message);
            log.info("Email de alerta enviado para {}", alertaEmailTo);
        } catch (Exception e) {
            log.error("Falha ao enviar email de alerta: {}", e.getMessage(), e);
        }
    }

    private void enviarWebhook(String assunto, String corpo) {
        try {
            var payload = new java.util.HashMap<String, Object>();
            payload.put("timestamp", LocalDateTime.now().toString());
            payload.put("subject", assunto);
            payload.put("body", corpo);

            restTemplate.postForEntity(alertaWebhookUrl, payload, String.class);
            log.info("Webhook de alerta enviado para {}", alertaWebhookUrl);
        } catch (Exception e) {
            log.error("Falha ao enviar webhook de alerta: {}", e.getMessage(), e);
        }
    }
}
