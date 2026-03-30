# ====================================================================
# Instalação do Monitoramento de Infraestrutura no Windows
# Configura uma Tarefa Agendada (Task Scheduler) para rodar no logon
# ====================================================================

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Resolve-Path (Join-Path $scriptDir "..")
$jarFile = Join-Path $projectDir "\target\monitoramento-infraestrutura-1.0.0.jar"
$taskName = "MonitoramentoInfraestruturaService"

Write-Host "Verificando dependências..."
if (-not (Test-Path $jarFile)) {
    Write-Warning "Arquivo JAR não encontrado em: $jarFile"
    Write-Warning "Compile o projeto primeiro com: mvn clean package"
    exit 1
}

Write-Host "Criando Tarefa Agendada: $taskName"
$action = New-ScheduledTaskAction -Execute "javaw.exe" -Argument "-jar `"$jarFile`"" -WorkingDirectory $projectDir
$trigger = New-ScheduledTaskTrigger -AtLogOn
$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable -RunOnlyIfNetworkAvailable:$false
$principal = New-ScheduledTaskPrincipal -UserId $env:USERNAME -LogonType Interactive

# Se a tarefa existir, remove primeiro
$existingTask = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
if ($existingTask) {
    Write-Host "Tarefa antiga encontrada. Removendo..."
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
}

Register-ScheduledTask -TaskName $taskName `
                       -Action $action `
                       -Trigger $trigger `
                       -Settings $settings `
                       -Principal $principal `
                       -Description "Inicia o servio de monitoramento de infraestrutura em segundo plano (Thymeleaf/Spring Boot)" | Out-Null

Write-Host "Instalação concluída com sucesso!" -ForegroundColor Green
Write-Host "A aplicação será iniciada automaticamente no próximo logon."
Write-Host "Para iniciar agora, execute: Start-ScheduledTask -TaskName '$taskName'"
