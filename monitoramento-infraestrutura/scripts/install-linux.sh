#!/bin/bash
# ====================================================================
# Instalação do Monitoramento de Infraestrutura no Linux
# Configura um SystemD user service para rodar ao on boot/login
# ====================================================================

set -e

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
PROJECT_DIR=$(dirname "$SCRIPT_DIR")
JAR_FILE="$PROJECT_DIR/target/monitoramento-infraestrutura-1.0.0.jar"
SERVICE_NAME="monitoramento-infra"
SERVICE_DIR="$HOME/.config/systemd/user"
SERVICE_FILE="$SERVICE_DIR/$SERVICE_NAME.service"

echo "Verificando compilado..."
if [ ! -f "$JAR_FILE" ]; ]; then
    echo "ERRO: Arquivo JAR não encontrado em: $JAR_FILE"
    echo "Compile o projeto primeiro: ./mvnw clean package"
    exit 1
fi

echo "Configurando SystemD User Service..."
mkdir -p "$SERVICE_DIR"

cat > "$SERVICE_FILE" << EOF
[Unit]
Description=Monitoramento de Infraestrutura (Spring Boot)
After=network.target

[Service]
Type=simple
WorkingDirectory=$PROJECT_DIR
ExecStart=/usr/bin/java -jar $JAR_FILE
Restart=on-failure
RestartSec=5

[Install]
WantedBy=default.target
EOF

echo "Recarregando daemon do SystemD..."
systemctl --user daemon-reload

echo "Habilitando serviço na inicialização..."
systemctl --user enable "$SERVICE_NAME.service"

echo "Iniciando serviço..."
systemctl --user start "$SERVICE_NAME.service"

# Habilita linger para que o serviço rode mesmo se o usuário não logar visualmente
loginctl enable-linger $USER

echo -e "\033[0;32mInstalação concluída com sucesso!\033[0m"
echo "O painel deve estar disponível em: http://localhost:8080"
echo "Para verificar os logs: journalctl --user -u $SERVICE_NAME -f"
