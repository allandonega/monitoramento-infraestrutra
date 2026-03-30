---
name: monitoramento-infraestrutura
description: Criar uma aplicação web para monitoramento em tempo real de disco, memória, rede e instalar como serviço no SO a fim de sempre estar executando
---

# Objetivo
- Criar uma aplicação web para monitoramento em tempo real de disco, memória, rede e instalar como serviço no SO a fim de sempre estar executando 

# Disco
	- Identificar diretórios com maior uso de disco.

# Rede
	- Monitorar acessos e tentativas de acessos da rede mostrando usuários, IP origem, Ip Destino
	- Identificar tentativas de invasão na minha rede

# Memória
	- Monitorar quantidade de memória utlizada e livre. 
	- Mostrar quais aplicações estão utilizando e/ou subutilizando muita memória e sugerir otimização.

# CPU
	- Faça monitoramento de CPU
	- Mostre quais aplicações estão utilizando mais CPU
	- Mostre quais aplicações estão utilizando menos CPU
	- Mostre quais aplicações estão utilizando mais CPU
	
# Stack
	- Java
		- Utilizar 100% de stack java para executar esse projeto.
		- Desenvolver a aplicação a fim de ser executada ao iniciar o SO.
	- Spring-boot
	- Banco de Dados em memória
	
# Testes	
	- Utilize TDD como framework de testes
	- Garanta que testes de acesso, performance, execução, funional, técnica estejam funcionando antes de entregar o projeto
	- Desenvolva testes unitários
	- Usar Playwright para realizar testes de tela
	
# Agents
	- Tomar cuidado com alucionações
	- Memorizar os acertos para economia de tokens
	- Criar um Agent de TDD
	- Criar um Agent de validação funcional
	- Os Agents precisam se falar para deixar o sistema funcional antes de entregar para o usuario

# Observabilidade	
	- Sugerir observabilidade para Elastic / Kibana
	- Verificar possibilidades de aplicar conceitos de Golden Signal, tracing, métricas e logs
	- Montar script técnico de implementação
	- Montar passo a passo de construção de dashboards no Kibana
	
# Resultado Esperado
	- Usuário vai acessar a url para monitorar o uso da máquina durante seu uso.
	- Usuário não precisa instalar um pacote.
	- Após todo o desenvolvimento for concluído e validado, o software deverá estar pronto para ser acessado assim que reiniciar o SO.
	- Para cada nova solicitação de implementação do usuário, avaliar a criação de novas seções específicas no sistema
	
	