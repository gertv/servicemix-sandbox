@echo off

set CURRENT_DIR=%~dp0
set PLATFORM=windows
set GWT_HOME_DIR=D:\opensource-projects\gwt-windows-1.5.0

if not exist "%GWT_HOME_DIR%" goto gwt_home

@echo on
java -Xmx256M -cp "%CURRENT_DIR%\src;%CURRENT_DIR%\bin;%CURRENT_DIR%\target\classes;%GWT_HOME_DIR%\gwt-user.jar;%GWT_HOME_DIR%\gwt-dev-%PLATFORM%.jar" com.google.gwt.dev.GWTCompiler -out "%CURRENT_DIR%\www" "$@" org.apache.servicemix.gshellweb.WebConsole

@echo off
goto end

:gwt_home
echo ==================================
echo GWT_HOME_DIR not set correctly!
echo please set within this batch file
echo ==================================

:end

