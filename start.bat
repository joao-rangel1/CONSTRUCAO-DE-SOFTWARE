@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ============================================
echo  SARC - Sistema de Alocacao de Recursos
echo ============================================
echo.

where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker nao encontrado!
    pause
    exit /b 1
)

docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker Desktop nao esta rodando.
    pause
    exit /b 1
)

if not exist ".env" (
    echo Criando .env com valores padrao...
    copy ".env.example" ".env" >nul
)

echo Iniciando todos os servicos...
docker compose up --build -d

if %errorlevel% neq 0 (
    echo [ERRO] Falha ao iniciar. Veja os logs acima.
    pause & exit /b 1
)

start http://localhost:3000

echo.
echo Sistema rodando! Para parar, execute stop.bat
pause