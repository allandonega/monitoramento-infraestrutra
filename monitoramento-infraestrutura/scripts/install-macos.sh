#!/bin/bash
# ====================================================================
# Instalação do Monitoramento de Infraestrutura no macOS
# Configura um LaunchAgent (launchd) para rodar no login
# ====================================================================

set -e

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
PROJECT_DIR=$(dirname "$SCRIPT_DIR")
JAR_FILE="$PROJECT_DIR/target/monitoramento-infraestrutura-1.0.0.jar"
PLIST_NAME="com.monitora.infraestrutura"
PLIST_FILE="$HOME/Library/LaunchAgents/$PLIST_NAME.plist"

echo "Verificando compilado..."
if [ ! -f "$JAR_FILE" ]; then
    echo "ERRO: Arquivo JAR não encontrado em: $JAR_FILE"
    echo "Compile o projeto primeiro: ./mvnw clean package"
    exit 1
fi

echo "Criando manifesto Launchd..."

cat > "$PLIST_FILE" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>$PLIST_NAME</string>
    <key>ProgramArguments</key>
    <array>
        <string>/usr/bin/env</string>
        <string>java</string>
        <string>-jar</string>
        <string>$JAR_FILE</string>
    </array>
    <key>WorkingDirectory</key>
    <string>$PROJECT_DIR</string>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <true/>
    <key>StandardOutPath</key>
    <string>$PROJECT_DIR/monitor-stdout.log</string>
    <key>StandardErrorPath</key>
    <string>$PROJECT_DIR/monitor-stderr.log</string>
</dict>
</plist>
EOF

echo "Carregando serviço nativo do macOS..."

# Descarrega se já existir
launchctl unload "$PLIST_FILE" 2>/dev/null || true

# Carrega e inicia
launchctl load -w "$PLIST_FILE"

echo -e "\033[0;32mInstalação concluída com sucesso no macOS!\033[0m"
echo "O painel deve estar disponível em breve em: http://localhost:8080"
echo "Logs de saída padrão em: $PROJECT_DIR/monitor-stdout.log"
