#!/bin/bash

set -x

pwd
pushd ./

export START_PATH=$(pwd)
export SOURCE_PATH="$START_PATH""/"${CIRCLE_PROJECT_REPONAME}"/"

export ANDROID_NDK="/usr/local/android-ndk/"
export _NDK_="$ANDROID_NDK"
 
export ANDROID_SDK="/usr/local/android-sdk-linux/"
export _SDK_="$ANDROID_SDK"

export BUILD_PATH="$START_PATH""/android-build"
mkdir -p $BUILD_PATH
export BUILD_PATH_MAIN_ARM="$START_PATH""/android-build"
mkdir -p $BUILD_PATH_MAIN_ARM


echo "================================="
pwd
echo "================================="
ls -al
echo "================================="
echo ANDROID_HOME:$ANDROID_HOME
echo "================================="
echo START_PATH:$START_PATH
echo "================================="
echo SOURCE_PATH:$SOURCE_PATH
echo "================================="
echo BUILD_PATH:"$BUILD_PATH"
echo "================================="
ls -al "$BUILD_PATH"/
echo "================================="
echo CIRCLE_PROJECT_REPONAME:${CIRCLE_PROJECT_REPONAME}
echo "================================="
# type -a ccache
# echo "================================="


rm navit/maptool/poly2tri-c/001/seidel-1.0/triangulate
rm pngout-static


cd ${START_PATH}

## -------- generic build commands --------
. "$SOURCE_PATH"/ci/do_android_build.inc
## -------- generic build commands --------


popd
pwd

