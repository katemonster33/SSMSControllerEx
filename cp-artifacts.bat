set MODS_PATH=C:\Program Files (x86)\Fractal Softworks\Starsector\mods\SSMSControllerEx
set SSMSC_PATH=.\

del /S /Q "%MODS_PATH%\*"
xcopy /E /Y "%SSMSC_PATH%\jars\" "%MODS_PATH%\jars\"
xcopy /E /Y "%SSMSC_PATH%\graphics\" "%MODS_PATH%\graphics\"
xcopy /E /Y "%SSMSC_PATH%\data\" "%MODS_PATH%\data\"
xcopy /E /Y "%SSMSC_PATH%\src\" "%MODS_PATH%\src\"
xcopy /Y "%SSMSC_PATH%\mod_info.json" "%MODS_PATH%\"
