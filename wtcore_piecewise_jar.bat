@echo off
if X%1==X (
     echo usage: wtcore_piecewise_jar.bat path_to_classes_root path_to_jar_destination
     exit /b 2
)

if X%2==X (
     echo usage: wtcore_piecewise_jar.bat path_to_classes_root path_to_jar_destination
     exit /b 2
)

@echo off
pushd %1
@echo on

jar cvMf webtop_component.jar org\webtop\component 
jar cvMf webtop_util.jar org\webtop\util 
jar cvMf webtop_x3d.jar org\webtop\x3d 
jar cvMf webtop_x3dlib.jar org\webtop\x3dlib

echo Jar'd:
echo org.webtop.component.*
echo org.webtop.util.*
echo org.webtop.x3d.*
echo org.webtop.x3dlib.*
echo Files placed in: 
echo %1\webtop_component.jar
echo %1\webtop_util.jarecho %1\webtop_x3d.jar
echo %1\webtop_x3dlib.jar
@echo off
popd

move /Y %1\webtop_component.jar %2
move /Y %1\webtop_util.jar %2
move /Y %1\webtop_x3d.jar %2
move /Y %1\webtop_x3dlib.jar %2

@echo on
