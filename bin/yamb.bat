@echo off

set CLASS_PATH=.;.\classes

for %%i in (.\lib\*.jar) do call cpappend.bat %%i

set MAIN_CLASS_NAME=vlm.Main

REM javaw.exe -cp %CLASS_PATH% %MAIN_CLASS_NAME% %1 > console.out
javaw.exe -cp %CLASS_PATH% %MAIN_CLASS_NAME% %1
