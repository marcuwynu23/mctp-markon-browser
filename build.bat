
jpackage ^
  --type exe ^
  --input . ^
  --name MarkonBrowser ^
  --main-class MarkonBrowser ^
  --main-jar MarkonBrowser.jar ^
  --java-options "--module-path D:\Executables\javafx\lib --add-modules javafx.controls,javafx.web"
