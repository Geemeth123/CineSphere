@echo off
echo   Starting CineSphere...
echo.

if not exist bin mkdir bin

echo Compiling...
javac -cp "lib/*" -d bin -sourcepath src src\Main.java src\models\*.java src\views\*.java src\views\components\*.java src\controllers\*.java src\utils\*.java

if %errorlevel% neq 0 (
    echo.
    echo Compilation FAILED!
    pause
    exit /b 1
)

echo Compilation successful.
echo.
echo Starting CineSphere...
echo.

java --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" Main

pause
