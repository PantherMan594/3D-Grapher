@echo off

echo "Please make sure Java JDK version 8 is installed before continuing."
echo "(Check with java -version, make sure it starts with 1.8. If Java is"
echo "installed but the command doesn't work, edit this file and type its"
echo "path inside JAVA_DIR=\"\")"
pause

REM Type the path to the jdk 8 bin directory in the quotes (e.g. JAVA_DIR="C:\Program Files (x86)\Java\jdk1.8.0_144\bin\")
set JAVA_DIR=""
set JAVAC=$JAVA_DIR"javac"
set JAVA=$JAVA_DIR"java"

rd /q /s out
md out
md "out\production"
md "out\production\3D Grapher"

javac -d "out/production/3D Grapher" -classpath "lib/core.jar" src/com/pantherman594/*
java -Dfile.encoding=UTF-8 -classpath "out/production/3D Grapher:lib/core.jar:lib/jogl-all.jar:lib/gluegen-rt.jar" com.pantherman594.Grapher
echo "Press any key to exit."
pause