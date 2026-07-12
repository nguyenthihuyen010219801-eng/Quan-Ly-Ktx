@echo off
setlocal EnableExtensions

rem ==================================================
rem Cau hinh moi truong
rem ==================================================
set "JAVA_HOME=C:\Program Files\Java\jdk-23"
set "MAVEN_HOME=C:\netbeans\java\maven"
set "CATALINA_HOME=C:\apache-tomcat-10.1.55"
set "APP_NAME=QuanLyKyTucXa"
set "APP_URL=http://localhost:8080/QuanLyKyTucXa/"

set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo.
echo ==================================================
echo KIEM TRA MOI TRUONG
echo ==================================================

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [LOI] Khong tim thay Java tai:
    echo %JAVA_HOME%
    pause
    exit /b 1
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [LOI] Khong tim thay Maven tai:
    echo %MAVEN_HOME%
    pause
    exit /b 1
)

if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo [LOI] Khong tim thay Tomcat tai:
    echo %CATALINA_HOME%
    pause
    exit /b 1
)

if not exist "pom.xml" (
    echo [LOI] Terminal khong dung tai thu muc chua pom.xml.
    echo Hay mo Terminal trong thu muc goc cua project.
    pause
    exit /b 1
)

echo JAVA_HOME=%JAVA_HOME%
echo MAVEN_HOME=%MAVEN_HOME%
echo CATALINA_HOME=%CATALINA_HOME%

echo.
java -version

echo.
call "%MAVEN_HOME%\bin\mvn.cmd" -version

echo.
echo ==================================================
echo BUILD PROJECT
echo ==================================================

call "%MAVEN_HOME%\bin\mvn.cmd" clean package

if errorlevel 1 (
    echo.
    echo [LOI] MAVEN BUILD THAT BAI.
    echo Hay xem cac dong loi phia tren.
    pause
    exit /b 1
)

if not exist "target\%APP_NAME%.war" (
    echo.
    echo [LOI] Build xong nhung khong tim thay:
    echo target\%APP_NAME%.war
    echo.
    echo Cac file WAR hien co:
    dir /b "target\*.war" 2>nul
    pause
    exit /b 1
)

echo.
echo ==================================================
echo DUNG TOMCAT CU
echo ==================================================

call "%CATALINA_HOME%\bin\shutdown.bat" >nul 2>&1

rem Cho Tomcat dung hoan toan
timeout /t 3 /nobreak >nul

echo.
echo ==================================================
echo XOA BAN DEPLOY CU
echo ==================================================

if exist "%CATALINA_HOME%\webapps\%APP_NAME%" (
    rmdir /s /q "%CATALINA_HOME%\webapps\%APP_NAME%"
)

if exist "%CATALINA_HOME%\webapps\%APP_NAME%.war" (
    del /f /q "%CATALINA_HOME%\webapps\%APP_NAME%.war"
)

echo.
echo ==================================================
echo COPY WAR VAO TOMCAT
echo ==================================================

copy /y "target\%APP_NAME%.war" "%CATALINA_HOME%\webapps\%APP_NAME%.war"

if errorlevel 1 (
    echo.
    echo [LOI] Khong copy duoc WAR vao Tomcat.
    echo Thu mo VS Code bang Run as administrator neu bi Access denied.
    pause
    exit /b 1
)

echo.
echo ==================================================
echo KHOI DONG TOMCAT
echo ==================================================

call "%CATALINA_HOME%\bin\startup.bat"

if errorlevel 1 (
    echo.
    echo [LOI] TOMCAT KHONG KHOI DONG DUOC.
    echo Hay kiem tra:
    echo %CATALINA_HOME%\logs\catalina.log
    pause
    exit /b 1
)

echo.
echo Dang cho Tomcat deploy ung dung...
timeout /t 8 /nobreak >nul

echo.
echo ==================================================
echo KIEM TRA CONG 8080
echo ==================================================

netstat -ano | findstr ":8080"

echo.
echo ==================================================
echo MO WEBSITE
echo ==================================================

start "" "%APP_URL%"

echo.
echo Website:
echo %APP_URL%
echo.
echo Neu trang chua mo duoc, xem log:
echo %CATALINA_HOME%\logs\catalina.log
echo %CATALINA_HOME%\logs\localhost.log
echo ==================================================
echo.

pause
endlocal