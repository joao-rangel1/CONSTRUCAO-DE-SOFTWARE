@echo off
cd /d "%~dp0"
echo Parando todos os servicos...
docker compose down
echo Servicos encerrados.
pause