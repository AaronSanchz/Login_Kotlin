# Parámetro permitido: compilar genera APK; pruebas ejecuta tests/lint; ejecutar instala y abre.
param([ValidateSet('compilar','pruebas','ejecutar')][string]$Accion='ejecutar')
$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
# Usa el JDK configurado por Android Studio; si falta, busca su runtime Java.
$javaConfig=Join-Path $PSScriptRoot '.gradle\config.properties'
if(Test-Path $javaConfig) {
    $line=Get-Content $javaConfig | Where-Object { $_ -like 'java.home=*' } | Select-Object -First 1
    if($line){ $env:JAVA_HOME=$line.Substring(10) }
}
if(!(Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $studioJava='C:\Program Files\Android\Android Studio\jbr'
    if(Test-Path "$studioJava\bin\java.exe") { $env:JAVA_HOME=$studioJava }
    else { throw 'Configura un JDK 17 o 21 en JAVA_HOME.' }
}
# El SDK es una ruta local del equipo y no se publica con el proyecto.
if(!(Test-Path 'local.properties')) { throw 'Abre esta carpeta en Android Studio para configurar el SDK de Android.' }
$sdkLine=Get-Content local.properties | Where-Object { $_ -like 'sdk.dir=*' } | Select-Object -First 1
# Convierte las barras y los dos puntos escapados del formato Java properties.
if(!$sdkLine){ throw 'local.properties no contiene sdk.dir. Configura el SDK en Android Studio.' }
$sdk=$sdkLine.Substring(8).Replace('\:',':').Replace('\\','\')
$env:ANDROID_HOME=$sdk
$env:ANDROID_SDK_ROOT=$sdk
# Ejecuta el wrapper fijado del proyecto y detiene el flujo si Gradle falla.
$gradleTasks=if($Accion -eq 'pruebas') { @(':app:testDebugUnitTest',':app:lintDebug') } else { @(':app:assembleDebug') }
& .\gradlew.bat @gradleTasks --console=plain
if($LASTEXITCODE -ne 0){ throw 'Falló Gradle. Revisa el primer error que aparece arriba.' }
if($Accion -ne 'ejecutar'){ return }
# adb comunica con Android. Compilar y probar terminan antes de este bloque.
$adb=Join-Path $sdk 'platform-tools\adb.exe'
& $adb start-server
$devices=@(& $adb devices | Select-String '^\S+\s+device$')
# Sin teléfono conectado, inicia un AVD existente y espera hasta tres minutos.
if($devices.Count -eq 0){
    $emulator=Join-Path $sdk 'emulator\emulator.exe'
    $avds=@(& $emulator -list-avds)
    $avd=if($avds -contains 'FakeStore_API_35'){ 'FakeStore_API_35' } else { $avds | Select-Object -First 1 }
    if(!$avd){ throw 'Crea un dispositivo en Android Studio > Device Manager o conecta un teléfono con depuración USB.' }
    Start-Process -FilePath $emulator -ArgumentList @('-avd',$avd) -WindowStyle Hidden
    $deadline=(Get-Date).AddMinutes(3)
    do { Start-Sleep -Seconds 2; $devices=@(& $adb devices | Select-String '^\S+\s+device$') } until($devices.Count -gt 0 -or (Get-Date) -gt $deadline)
}
# Exige un único destino para no instalar por accidente en otro dispositivo.
if($devices.Count -ne 1){ throw 'Deja exactamente un dispositivo o emulador conectado y vuelve a ejecutar.' }
$serial=($devices[0].ToString() -split '\s+')[0]
# Antes de instalar, espera a que Android complete su arranque.
$deadline=(Get-Date).AddMinutes(3)
do { $boot=(& $adb -s $serial shell getprop sys.boot_completed 2>$null); if($boot -eq '1'){break}; Start-Sleep -Seconds 2 } until((Get-Date) -gt $deadline)
if($boot -ne '1'){ throw 'El dispositivo todavía no termina de iniciar. Reintenta en un momento.' }
# Instala el APK debug conservando datos existentes y abre LoginActivity.
& $adb -s $serial install -r 'app\build\outputs\apk\debug\app-debug.apk'
if($LASTEXITCODE -ne 0){ throw 'No se pudo instalar el APK.' }
& $adb -s $serial shell am start -n 'com.example.fakestoreroles.kotlin/com.example.fakestoreroles.LoginActivity'
