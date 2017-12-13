This is a 3D Grapher, created using Java and Processing. It graphs 3D functions that you input, and allows you to move around and view the graphs at different angles.

To get started, run the appropriate script for your operating system. If that doesn’t work:
1. Setup the directories "out/production/3D Grapher" inside the 3D Grapher folder
2. Open terminal in the root directory (the one containing the scripts, "src", and "lib")
3. Run the following commands, in order (the first one compiles the files into the out/production/3D Grapher directory that you just created. The second runs the compiled classes):
    a. `javac -d "out/production/3D Grapher" -classpath "lib/core.jar" src/com/pantherman594/*`
    b. And one of the following, the first for Linux or Mac OSX, the other for Windows:
        * `java -Dfile.encoding=UTF-8 -classpath "out/production/3D Grapher:lib/core.jar:lib/jogl-all.jar:lib/gluegen-rt.jar" com.pantherman594.Grapher`
        * `java -Dfile.encoding=UTF-8 -classpath "out/production/3D Grapher;lib/core.jar;lib/jogl-all.jar;lib/gluegen-rt.jar" com.pantherman594.Grapher`
Once a window opens, see the instructions and information at the top. Try out some functions and play with the settings! (some suggestions are: x^2-y^2, sin(xy))

This project was inspired by my math teacher’s complaints that there isn’t any good 3D grapher (aside from Mathematica, which is too expensive for us students to try) that allows you to freely move around and change angles. I used Processing as a library, rather than running its IDE, and all the required files are in the "lib" directory.
