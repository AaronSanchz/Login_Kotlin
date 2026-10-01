@echo off
REM Abre el lanzador del mismo directorio y permite leer errores antes de cerrar.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0ejecutar.ps1" -Accion ejecutar
pause
