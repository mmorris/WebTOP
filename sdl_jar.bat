@echo off
pushd %1
@echo on

jar cvMf sdl.jar org\sdl

echo Jar'd:
echo org.sdl.*
echo All files placed in %1\sdl.jar.  NOTE: old files have been replaced

@echo off
popd
