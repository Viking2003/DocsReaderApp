@echo off
REM Build APK and copy to apks folder for distribution
REM Usage: build-apk.bat [debug|release]

set BUILD_TYPE=%1
if "%BUILD_TYPE%"=="" set BUILD_TYPE=release

echo Building %BUILD_TYPE% APK...

REM Build the APK
call gradlew.bat assemble%BUILD_TYPE%

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo Build successful!

    REM Create apks directory if it doesn't exist
    if not exist apks mkdir apks

    REM Copy the APK to apks folder
    copy app\build\outputs\apk\%BUILD_TYPE%\app-%BUILD_TYPE%.apk apks\

    echo APK copied to apks\app-%BUILD_TYPE%.apk
    echo You can now commit the apks folder to Git
) else (
    echo Build failed!
    exit /b 1
)
