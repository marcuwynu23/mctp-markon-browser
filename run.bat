@echo off
set JAVA_FX_LIB=D://Executables//javafx//lib
set JSON_JAR=libs/json.jar
set FLEXMARK_JAR=libs/flexmark-all-0.64.8.jar
set OUT_JAR=MarkonBrowser.jar

REM Compile Java source in current directory (output .class files here)
javac --module-path %JAVA_FX_LIB% --add-modules javafx.controls,javafx.web -cp %FLEXMARK_JAR%;%JSON_JAR% MarkonBrowser.java
if errorlevel 1 goto :eof

REM Create manifest file for JAR
echo Main-Class: MarkonBrowser > manifest.txt

REM Create JAR file including .class files and resources in current directory
jar --create --file %OUT_JAR% --manifest manifest.txt *.class resources\* 

if errorlevel 1 goto :eof

REM Run the application from the created JAR with dependencies on classpath
java --module-path %JAVA_FX_LIB% --add-modules javafx.controls,javafx.web -cp %OUT_JAR%;%FLEXMARK_JAR%;%JSON_JAR% MarkonBrowser
