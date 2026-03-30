package com.monitora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitora.dto.DiscoInfoDTO;
import com.monitora.dto.MemoriaInfoDTO;
import com.monitora.dto.RedeInfoDTO;
import com.monitora.service.ColetorDiscoService;
import com.monitora.service.ColetorMemoriaService;
import com.monitora.service.ColetorRedeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class SseController {

    private final ColetorDiscoService coletorDisco;
    private final ColetorMemoriaService coletorMemoria;
    private final ColetorRedeService coletorRede;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                MemoriaInfoDTO memoria = coletorMemoria.coletarDadosAtuais();
                emitter.send(SseEmitter.event()
                    .name("memoria")
                    .data(objectMapper.writeValueAsString(new MemoriaEvent(
                        memoria.getPercentualUso(),
                        memoria.getUsadoFormatado(),
                        memoria.getDisponivelFormatado()
                    ))));

                RedeInfoDTO rede = coletorRede.coletarDadosAtuais();
                emitter.send(SseEmitter.event()
                    .name("rede")
                    .data(objectMapper.writeValueAsString(new RedeEvent(
                        rede.getTotalConexoesAtivas(),
                        rede.getTotalConexoesFechadas()
                    ))));

                List<DiscoInfoDTO> discos = coletorDisco.coletarDadosAtuais();
                if (!discos.isEmpty()) {
                    DiscoInfoDTO disco = discos.get(0);
                    emitter.send(SseEmitter.event()
                        .name("disco")
                        .data(objectMapper.writeValueAsString(new DiscoEvent(
                            disco.getPercentualUso(),
                            disco.getUsadoFormatado(),
                            disco.getLivreFormatado()
                        ))));
                }

            } catch (Exception e) {
                log.warn("SSE emitter error: {}", e.getMessage());
                emitter.completeWithError(e);
                executor.shutdown();
            }
        }, 0, 5, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(() -> {
            executor.shutdown();
            emitter.complete();
        });
        emitter.onError(e -> executor.shutdown());

        return emitter;
    }

    record MemoriaEvent(double percentual, String usado, String disponivel) {}
    record RedeEvent(int conexoesAtivas, int conexoesFechadas) {}
    record DiscoEvent(double percentual, String usado, String livre) {}
}
