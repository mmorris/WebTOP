@echo off
pushd classes
@echo on
jar -cvmf ..\manifests\manifest_circular.mf ..\module_jars\circular.jar org\webtop\module\circular
@echo off
popd

