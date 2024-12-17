@echo off
setlocal enabledelayedexpansion

:: Chemin vers le répertoire contenant les fichiers .java
set src_dir=%~dp0src

:: Chemin vers le répertoire où les .class seront placés
set bin_dir=%~dp0src\target\classes

:: Chemin vers le fichier JAR de la bibliothèque org.json
set lib_dir=%~dp0lib\json-20240303.jar

:: Création du dossier pour les .class si nécessaire
if not exist "%bin_dir%" (
    mkdir "%bin_dir%"
)

:: Initialisation de la variable pour stocker la liste des fichiers .java
set java_files=

:: Parcours récursif des fichiers .java
for /r "%src_dir%" %%f in (*.java) do (
    set java_files=!java_files! "%%f"
)

:: Compilation des fichiers .java avec le JAR dans le classpath
echo Compilation en cours...
echo Debug...
javac -d "%bin_dir%" -classpath "%lib_dir%" %java_files%

:: Vérification que la compilation a réussi
if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation.
    exit /b 1
)

:: Exécution du programme avec le JAR dans le classpath
echo Exécution du programme...
java -cp "%bin_dir%;%lib_dir%" SGBD

pause
