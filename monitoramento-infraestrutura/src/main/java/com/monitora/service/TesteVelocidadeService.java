package com.monitora.service;

import com.monitora.model.TesteVelocidade;
import com.monitora.repository.TesteVelocidadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TesteVelocidadeService {

    private final TesteVelocidadeRepository repo;

    private static final String CLOUDFLARE_DOWN = "https://speed.cloudflare.com/__down?bytes=10000000";
    private static final String CLOUDFLARE_UP   = "https://speed.cloudflare.com/__up";
    private static final int    UPLOAD_BYTES     = 5_000_000;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public TesteVelocidade testarDownload() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(CLOUDFLARE_DOWN))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

            long inicio = System.currentTimeMillis();
            HttpResponse<byte[]> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            long duracao = System.currentTimeMillis() - inicio;

            long bytes = resp.body().length;
            double mbps = (bytes * 8.0) / (duracao / 1000.0) / 1_000_000.0;

            TesteVelocidade teste = TesteVelocidade.builder()
                    .tipo(TesteVelocidade.TipoTeste.DOWNLOAD)
                    .velocidadeMbps(Math.round(mbps * 100.0) / 100.0)
                    .bytesTransferidos(bytes)
                    .duracaoMs(duracao)
                    .latenciaMs(0)
                    .executadoEm(LocalDateTime.now())
                    .build();

            return repo.save(teste);
        } catch (Exception e) {
            log.error("Erro no teste de download: {}", e.getMessage());
            throw new RuntimeException("Falha no teste de download: " + e.getMessage(), e);
        }
    }

    public TesteVelocidade testarUpload() {
        try {
            byte[] payload = new byte[UPLOAD_BYTES];
            new Random().nextBytes(payload);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(CLOUDFLARE_UP))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                    .build();

            long inicio = System.currentTimeMillis();
            httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            long duracao = System.currentTimeMillis() - inicio;

            double mbps = (UPLOAD_BYTES * 8.0) / (duracao / 1000.0) / 1_000_000.0;

            TesteVelocidade teste = TesteVelocidade.builder()
                    .tipo(TesteVelocidade.TipoTeste.UPLOAD)
                    .velocidadeMbps(Math.round(mbps * 100.0) / 100.0)
                    .bytesTransferidos(UPLOAD_BYTES)
                    .duracaoMs(duracao)
                    .latenciaMs(0)
                    .executadoEm(LocalDateTime.now())
                    .build();

            return repo.save(teste);
        } catch (Exception e) {
            log.error("Erro no teste de upload: {}", e.getMessage());
            throw new RuntimeException("Falha no teste de upload: " + e.getMessage(), e);
        }
    }

    public TesteVelocidade testarPing() {
        try {
            String url = "https://speed.cloudflare.com/__down?bytes=1";
            double totalMs = 0;
            int tentativas = 5;

            for (int i = 0; i < tentativas; i++) {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();
                long inicio = System.currentTimeMillis();
                httpClient.send(req, HttpResponse.BodyHandlers.discarding());
                totalMs += (System.currentTimeMillis() - inicio);
            }

            double latenciaMedia = totalMs / tentativas;

            TesteVelocidade teste = TesteVelocidade.builder()
                    .tipo(TesteVelocidade.TipoTeste.PING)
                    .velocidadeMbps(0)
                    .bytesTransferidos(0)
                    .duracaoMs((long) latenciaMedia)
                    .latenciaMs(Math.round(latenciaMedia * 100.0) / 100.0)
                    .executadoEm(LocalDateTime.now())
                    .build();

            return repo.save(teste);
        } catch (Exception e) {
            log.error("Erro no teste de ping: {}", e.getMessage());
            throw new RuntimeException("Falha no teste de ping: " + e.getMessage(), e);
        }
    }
}
