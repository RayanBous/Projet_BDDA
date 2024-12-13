@echo off

:: Chemin vers le répertoire contenant les fichiers .java
set src_dir=%~dp0src\main\java

:: Chemin vers le répertoire où les .class seront placés
set bin_dir=%~dp0bin

:: Chemin vers le fichier JAR de la bibliothèque org.json
set lib_dir=%~dp0lib\json-20240303.jar

:: Création du dossier pour les .class si nécessaire
if not exist "%bin_dir%" (
    mkdir "%bin_dir%"
)

:: Compilation des fichiers .java avec le JAR dans le classpath
echo Compilation en cours...
javac -d "%bin_dir%" -classpath "%lib_dir%" "%src_dir%\*.java"

:: Vérification que la compilation a réussi
if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation.
    exit /b 1
)

:: Exécution du programme avec le JAR dans le classpath
echo Exécution du programme...
java -cp "%bin_dir%;%lib_dir%" Main Fait windows
