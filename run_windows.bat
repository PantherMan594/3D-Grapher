@echo off

echo Please make sure Java JDK version 8 is installed before continuing.
echo (Check with java -version, make sure it starts with 1.8. If Java is
echo installed but the command doesn't work, edit this file and type its
echo path after JAVA_DIR=)
pause

REM Type the path to the jdk 8 bin directory (e.g. JAVA_DIR=C:\Program Files\Java\jdk1.8.0_151\bin\)
set JAVA_DIR=
set JAVAC=%JAVA_DIR%javac.exe
set JAVA=%JAVA_DIR%java.exe

rd /q /s out
md "out\production\3D Grapher"

echo %JAVAC%
echo %JAVA%

start /wait "" "%JAVAC%" -d "out/production/3D Grapher" -classpath "lib/core.jar" src/com/pantherman594/*
start /wait "" "%JAVA%" -Dfile.encoding=UTF-8 -classpath "out/production/3D Grapher;lib/core.jar;lib/jogl-all.jar;lib/gluegen-rt.jar" com.pantherman594.Grapher
pause