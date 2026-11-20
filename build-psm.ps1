# Construit psm.jar (Spring Boot + Angular) sous Windows PowerShell.
# Usage : .\build-psm.ps1
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

Write-Host "1/3 Build Angular..."
Push-Location "$root\frontend"
npm install --legacy-peer-deps
if ($LASTEXITCODE -ne 0) { throw "npm install a échoué" }
npx ng build --configuration production
if ($LASTEXITCODE -ne 0) { throw "ng build a échoué" }
Pop-Location

Write-Host "2/3 Copie d'Angular dans backend/src/main/resources/static..."
$static = "$root\backend\src\main\resources\static"
if (Test-Path $static) { Remove-Item $static -Recurse -Force }
New-Item -ItemType Directory -Path $static | Out-Null
Copy-Item "$root\frontend\dist\*" $static -Recurse

Write-Host "3/3 Build Spring Boot..."
Push-Location "$root\backend"
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "mvn package a échoué" }
Pop-Location

Write-Host "Terminé : backend\target\psm.jar"
Write-Host "Lancer : java -jar backend\target\psm.jar   puis ouvrir http://localhost:8080"
