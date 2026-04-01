# Instalador de serviço Windows (WinSW)
# Requer winSW (Microsoft Windows Service Wrapper) e Java 21 instalado.
# Ajuste o path do jar e da pasta de trabalho para seu ambiente.

param(
    [string]$ServiceName = "MonitoramentoInfra",
    [string]$JarPath = "C:\\workspace\\Antigravity\\monitoramento-infraestrutura\\target\\monitoramento-infraestrutura-1.0.0.jar",
    [string]$InstallDir = "C:\\monitoramento-infraestrutura"
)

if (-Not (Test-Path $InstallDir)) {
    New-Item -ItemType Directory -Path $InstallDir | Out-Null
}

$serviceExe = Join-Path $InstallDir "$ServiceName.exe"
$xmlFile = Join-Path $InstallDir "$ServiceName.xml"

# Baixe WinSW de https://github.com/winsw/winsw/releases e coloque no InstallDir
# Exemplo simplificado salva configurações de serviço.

@"
<service>
  <id>$ServiceName</id>
  <name>Monitoramento de Infraestrutura</name>
  <description>Sistema de monitoramento de infra com Spring Boot</description>
  <executable>java</executable>
  <arguments>-jar "$JarPath"</arguments>
  <logpath>logs</logpath>
  <depend>H2</depend>
</service>
"@ | Out-File -FilePath $xmlFile -Encoding UTF8

Write-Host "Arquivo de serviço criado em $xmlFile. Copie o binário winSW correspondente para $serviceExe e execute:"
Write-Host "$serviceExe install"
Write-Host "$serviceExe start"