# Implementação de Observabilidade: Elastic Stack (ELK) + Micrometer

Este documento detalha o script técnico de implementação para enviar métricas da aplicação Spring Boot para o Elasticsearch e visualizá-las no Kibana, seguindo as melhores práticas de Observabilidade e os SRE Golden Signals.

## 1. Golden Signals e O que Monitorar
O monitoramento deve focar nos **4 Golden Signals**:
1. **Latency** (Latência): O tempo que leva para atender a uma requisição (ex: acesso às páginas web, `/api/sse/stream`).
2. **Traffic** (Tráfego): Medida de demanda do sistema (número de conexões ativas de rede).
3. **Errors** (Erros): Taxa de requisições que falharam (HTTP 500, falha de leitura de disco).
4. **Saturation** (Saturação): Qualidade do uso dos recursos (percentual de RAM usada, percentil de uso do disco).

## 2. Dependências Necessárias no `pom.xml`
Para exportar métricas diretamente para o Elasticsearch, adicione as seguintes bibliotecas no seu projeto Java:

```xml
        <!-- Observability: Micrometer e Elasticsearch -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-elastic</artifactId>
        </dependency>
```

## 3. Configurações (`application.properties`)
Adicione ou ajuste as seguintes propriedades para conectar a aplicação ao nó do Elasticsearch:

```properties
# Expor os endpoints do Actuator (Prometheus/Elastic Metrics)
management.endpoints.web.exposure.include=health,info,metrics,elastic
management.endpoint.health.show-details=always

# Configuração Elastic Registry
management.metrics.export.elastic.enabled=true
management.metrics.export.elastic.host=http://localhost:9200
management.metrics.export.elastic.index=monitoramento-metricas # Nome do índice
management.metrics.export.elastic.index-date-format=yyyy-MM
management.metrics.export.elastic.step=1m # Frequência de envio
```

## 4. Instância do ELK localmente via Docker
Execute este script técnico no seu terminal para subir a stack de monitoramento:

```bash
# Subir Elasticsearch
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.10.4

# Subir Kibana
docker run -d --name kibana -p 5601:5601 --link elasticsearch:elasticsearch -e "ELASTICSEARCH_HOSTS=http://elasticsearch:9200" docker.elastic.co/kibana/kibana:8.10.4
```

## 5. Passo a Passo: Construção de Dashboards no Kibana

Com a aplicação instrumentada rodando localmente (junto aos containeres do Elastic), abra o Kibana em `http://localhost:5601` e siga o procedimento:

1. **Criar o Data View / Index Pattern**
   - Acesse **Stack Management** (ícone da engrenagem) > **Data Views**.
   - Clique em "Create data view".
   - No campo "Index pattern", digite `monitoramento-metricas*`.
   - Selecione `@timestamp` como formato de tempo e conclua.

2. **Criar a visualização da Saturação (Memória RAM)**
   - Vá para o app **Dashboard** e clique em "Create new dashboard".
   - Clique em "Create visualization".
   - Arraste `system.cpu.usage` ou modifique o campo para métricas customizadas do serviço de memória (`system.memory`).
   - Mude a visualização gráfica para "Gauge" ou "Line Chart" para acompanhar os picos em tempo de execução.

3. **Criar a visualização da Latência e Tráfego Web**
   - Na mesma Dashboard da etapa anterior, acrescente uma visualização de área ("Area Chart").
   - Utilize as métricas `http.server.requests` extraídas diretamente pelo Actuator.
   - Configure o eixo Y como as médias `Avg` e max `Max` para tempo de resposta. O eixo X deverá ser tempo cronológico (`@timestamp`).

4. **Tráfego de Rede / Red flags Segurança**
   - Exiba a quantidade total de conexões suspeitas por hora criando um *Vertical Bar* na métrica `rede.conexoes.suspeitas_count` (uma vez customizada e exposta pelos serviços criados via `MeterRegistry.gauge`).

> [!TIP]
> Expor métricas customizadas:
> Injete o bean `io.micrometer.core.instrument.MeterRegistry` dentro de `ColetorRedeService` e registre instâncias métricas contínuas chamando: `registry.gauge("rede.conexoes.ativas", rede.getTotalConexoesAtivas());`
