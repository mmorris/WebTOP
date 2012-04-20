@echo off
if X%1==X (
     echo usage: wtcore_jar.bat [path_to_source_root]
     exit /b 2
)

@echo off
pushd %1
@echo on

jar cvMf webtopcore.jar org\webtop\component org\webtop\util org\webtop\x3d org\webtop\x3dlib

echo Jar'd:
org.webtop.component.*
org.webtop.util.*
org.webtop.x3d.*
org.webtop.x3dlib.*
All files placed in %1\webtopcore.jar.  NOTE: old files have been replaced

@echo off
popd
