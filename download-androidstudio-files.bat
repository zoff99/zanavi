@echo off

REM get directory of this script
SET mypath=%~dp0
echo %mypath:~0,-1%

DEL /F /Q navit\mvt_tiles.h
DEL /F /Q navit\s_index.h

REM windows android studio does not compile with "git links" :-(
COPY /Y navit\mvt_tiles_full_zvt.h navit\mvt_tiles.h
COPY /Y navit\s_index.h_full navit\s_index.h

REM debug
REM https://circleci.com/api/v1/project/zoff99/zanavi/latest/artifacts/0/$CIRCLE_ARTIFACTS/android-studio-project.zip?filter=successful&branch=master

echo Downloading latest version ...
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://circleci.com/api/v1/project/zoff99/zanavi/latest/artifacts/0/$CIRCLE_ARTIFACTS/android-studio-project.zip?filter=successful&branch=master', 'android-studio-project.zip')"

echo unzipping
unzip -o android-studio-project.zip

pause

@echo ON
