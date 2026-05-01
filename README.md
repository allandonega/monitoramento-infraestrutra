# 📡 Monitor de Infraestrutura

Um poderoso painel com interface web para monitoramento contínuo e em tempo real dos recursos de hardware e sistema operacional da sua máquina/servidor. Construído com **Java 21** e **Spring Boot 3**, o monitor exibe métricas detalhadas sobre redes, processadores, discos e memórias, ajudando você a otimizar a performance.

## 🚀 Funcionalidades

- **Monitoramento de CPU:** Acompanhe o percentual de carga global do processador em tempo real, identifique os núcleos e avalie rankings de quais processos do S.O. estão consumindo mais ou menos recursos computacionais.
- **Armazenamento / Discos:** Entenda os volumes formatados, mapeando espaço livre, espaço alocado e capacidade total.
- **Memória RAM virtual e Swap:** Carga de processos complexos que gastam excesso de limites pré-estabelecidos e visão completa do sistema de cache de hardware.
- **Rede e Conectividade:** Dashboard contínuo informando tráfegos de intrusos e aberturas de conexões ativas/fechadas.
- **Teste de Velocidade de Internet:** Meça download, upload e latência diretamente pelo painel. Cada execução é gravada no banco com data/hora e tipo do teste, gerando um histórico visual com gráficos de evolução ao longo do tempo.
- **Painel Assíncrono em Tempo Real:** Todo o Frontend atualiza "Sozinho"! Desenvolvido com uma abordagem limpa através de **Thymeleaf**, **SSE (Server-Sent Events) via Streams do Spring** e **HTMX**. Nem o usuário nem o navegador precisam forçar novas requisições. 
- **Limpeza Inteligente de Histórico (Auto-Vacuum):** Agendadores cuidando de seu espaço em disco, removendo snapshots do banco de dados embargados pelo uso muito duradouro, com tolerância configurável.

## 🛠️ Tecnologias Utilizadas

- **Base de Backend:** Java 21, Spring Boot 3 (MVC, Actuator, Data JPA), Lombok
- **Camada de Sistema & Sensores:** Bibliotecas OSHI (_Operations System Hardware Information_)
- **Database:** Banco Embutido H2 Database Local (para alta portabilidade sem infraestrutura adicional)
- **Frontend & Interfaces:** HTML5/CSS, Thymeleaf Layouts, HTMX (JS Assíncrono), Chart.js
- **Segurança da Qualidade:** JUnit 5 (Unitários), Playwright UI (E2E Automáticos)
- **Build System:** Maven

## ⚙️ Pré-requisitos

Para rodar este monitoramento em qualquer máquina local ou nuvem com tela, precisa apenas de:
- Java 21 (JDK) ou superior instalado
- Git (opcional para clonagem)
- 10 segundos! A Inicialização se ajusta ao ambiente.

## 🏃 Como Rodar a Aplicação

1. Clone ou entre no repositório do projeto:
```bash
git clone https://github.com/seu-usuario/monitoramento-infraestrutra.git
cd monitoramento-infraestrutura
```

2. Caso seja seu primeiro uso, basta rodar através do empacotador Maven (nenhuma chave, lib C++ ou compilação global é necessária):
```bash
# Num terminal próprio (CMD/Powershell) ou terminal de IDE:
mvn spring-boot:run
```

3. Abra o navegador para visualizar os resultados ao vivo:
- **Painel de Controle Principal:** [http://localhost:8080](http://localhost:8080)
- **Teste de Velocidade:** [http://localhost:8080/velocidade](http://localhost:8080/velocidade)

## 🛠️ Rodando como serviço do sistema

### Linux (systemd)

```bash
sudo bash scripts/install-systemd.sh
```

### Windows (WinSW)

```powershell
powershell -ExecutionPolicy Bypass -File scripts/install-windows-service.ps1
```
- **Visualizador em Tabela de Banco H2:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Métricas de Diagnósticos e Verificação:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## 🧪 Executando Testes

Todo os subsistemas e coletores de dados foram cobertos com métodos padronizados de simulação Mock/Mockito, e UI em Playwright. 

Para disparar os testes integrados digite:
```bash
mvn test
```

## 📁 Estrutura Explicada do Software

* `src/main/java.../controller`: Rotas de tráfego, Exibições do Thymeleaf e o complexo WebEmitters (`SseController`) que empurra eventos.
* `src/main/java.../service`: Contém o coração da extração de Hardware da máquina via Oshi e do Scheduler do Histórico.
* `src/main/java.../repository`: Implementam o acesso persistido sem codificação pesada.
* `src/main/resources/templates`: Interface amigável, templates globais de navegação, classes HTMX injetadas.
* `.github`: Notas, diagramas, e as documentações instrutivas da ideia orgânica original.

---
Desenvolvido com Antigraviy - Claude Code e Gemini. ✨
