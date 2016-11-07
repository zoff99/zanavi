#! /bin/bash

echo "########### set verbose output ###########"
set -x
export FULL_LOG=1
echo "########### set verbose output ###########"

echo "########### INFO: ###########"
echo "_SDK_=""$_SDK_"
echo "_NDK_=""$_NDK_"
echo "########### INFO: ###########"

pwd
pushd ./

echo "########### moving down to navit dir ###########"
mkdir nav2
for i in `ls -1`; do
	if [ "$i" != "nav2" ]; then
		mv $i nav2/
	fi
done
mv nav2 navit
echo "########### moving down to navit dir ###########"


export START_PATH=$(pwd)
export SOURCE_PATH="$START_PATH""/navit/"

export ANDROID_NDK="$_NDK_"
export _NDK_="$ANDROID_NDK"
 
export ANDROID_SDK="$_SDK_"
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

cd ${START_PATH}


## -------- generic build commands --------
. "$SOURCE_PATH"/ci/do_android_build.inc
## -------- generic build commands --------


# -- set flag for FDROID build --
sed -i -e 's#static final boolean FDBL = false;#static final boolean FDBL = true;#' src/com/zoffcc/applications/zanavi/Navit.java
# -- set flag for FDROID build --



## --- gradle build ---
popd && pwd && \
cd ${BUILD_PATH}/navit/ && pwd && \
export GRADLE_OPTS='-Dorg.gradle.jvmargs="-Xmx1600m -XX:+HeapDumpOnOutOfMemoryError"'
gradle wrapper --gradle-version "3.1" --info && \
./gradlew :android:assembleRelease --stacktrace --info -x lint
## --- gradle build ---

