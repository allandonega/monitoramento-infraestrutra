#!/bin/bash
# Script de exemplo para criar serviço systemd
# Requer Java 21 e arquivo jar disponível.

SERVICE_NAME=monitoramento-infraestrutura
JAR_PATH=/opt/monitoramento-infraestrutura/monitoramento-infraestrutura-1.0.0.jar
USER=www-data
WORKDIR=/opt/monitoramento-infraestrutura
SYSTEMD_FILE=/etc/systemd/system/${SERVICE_NAME}.service

sudo mkdir -p $WORKDIR

cat <<EOF | sudo tee $SYSTEMD_FILE
[Unit]
Description=Monitoramento de Infraestrutura
After=network.target

[Service]
Type=simple
User=${USER}
WorkingDirectory=${WORKDIR}
ExecStart=/usr/bin/java -jar ${JAR_PATH}
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable $SERVICE_NAME
sudo systemctl start $SERVICE_NAME
sudo systemctl status $SERVICE_NAME
