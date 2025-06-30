@echo off
REM Script pour générer l'installateur Windows de GestionRestaurant

REM Vérifier que le JAR existe
if not exist target\original-Gestion_Restaurant-1.0-SNAPSHOT.jar (
    echo Le fichier target\Gestion_Restaurant-1.0-SNAPSHOT.jar est introuvable. Compile d'abord le projet !
    pause
    exit /b
)

REM Créer le dossier dist si besoin
if not exist dist mkdir dist

REM Vérifier que l'icône existe
if not exist src\main\resources\Images\close.ico (

    echo L'ic\^one src\main\resources\Images\close.ico est introuvable. Place une ic\^one .ico a cet emplacement !
    echo Pour convertir une image PNG en ICO, utilise ce site : https://convertio.co/fr/png-ico/
    pause
    exit /b
)

REM Générer l'installateur avec jpackage
jpackage --type exe --input target/ --name GestionRestaurant --main-jar Gestion_Restaurant-1.0-SNAPSHOT.jar --main-class org.Main --icon src/main/resources/Images/close.ico --java-options "-Xmx512m --module-path C:\javafx-sdk-21.0.7\lib --add-modules javafx.controls,javafx.fxml" --dest dist/

if %errorlevel% neq 0 (
    echo Erreur lors de la génération de l'installateur.
    pause
    exit /b
)

echo Installateur généré dans le dossier dist/ !
pause
