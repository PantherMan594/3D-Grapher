#/bin/bash

JAVA_DIR="" # Type the path to the jdk 8 bin directory in the quotes (e.g. JAVA_DIR="/usr/lib/jvm/java-8-openjdk/bin/")
JAVAC=$JAVA_DIR"javac"
JAVA=$JAVA_DIR"java"
DIR=`pwd`

JAVA_VER=$(java -version 2>&1 | grep version | sed 's/.* version //' | sed 's/"//g')
if [[ $JAVA_VER != 1.8*  && -z "$JAVA_DIR" ]]
then
    echo "Please install Java JDK version 8 and rerun this script."
    echo "(Check with java -version, make sure it starts with 1.8."
    echo "If JDK 8 is already installed, edit this file and type its"
    echo "path inside JAVA_DIR=\"\")"
    echo ""
    read -p "Press [Enter] to quit"
    exit
fi

rm -rf out
mkdir -p "out/production/3D Grapher"

$JAVAC -d "out/production/3D Grapher" -classpath "lib/core.jar" src/com/pantherman594/*
$JAVA -Dfile.encoding=UTF-8 -classpath "out/production/3D Grapher:lib/core.jar:lib/jogl-all.jar:lib/gluegen-rt.jar" com.pantherman594.Grapher
read -p "Press [Enter] to quit."