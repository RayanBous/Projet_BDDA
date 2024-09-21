@echo off
REM Chemin vers le répertoire contenant les fichiers .java
set src_dir=C:\Users\darkl\IdeaProjects\Projet_BDDA\src\main\java

REM Chemin vers le répertoire où les .class seront placés
set bin_dir=C:\Users\darkl\IdeaProjects\Projet_BDDA\bin

REM Chemin vers le fichier JAR de la bibliothèque org.json
set lib_dir=C:\Users\darkl\IdeaProjects\Projet_BDDA\lib\json-20240303.jar

REM Création du dossier pour les .class si nécessaire
if not exist %bin_dir% (
    mkdir %bin_dir%
)

REM Compilation des fichiers .java avec le JAR dans le classpath
echo Compilation en cours...
javac -d %bin_dir% -classpath %lib_dir% -source 17 -target 17 %src_dir%\*.java

REM Vérification que la compilation a réussi
if %errorlevel% neq 0 (
    echo Erreur lors de la compilation.
    exit /b %errorlevel%
)

REM Exécution du programme avec le JAR dans le classpath
echo Exécution du programme...
cd %bin_dir%
java -classpath %bin_dir%;%lib_dir% Main

pause

