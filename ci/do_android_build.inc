#
# syntax for bash shell !!


## build libs for these architectures -------------------------
#
unset build_archs__
declare -A build_archs__
#
build_archs__["arm"]=1
build_archs__["armv7"]=1
build_archs__["arm64"]=0
#
build_archs__["x86"]=1
#
build_archs__["mips"]=0
#
## build libs for these architectures -------------------------



if [ `uname -m` == 'x86_64' ]; then
	export SUFFIX2='_64'
else
	export SUFFIX2=''
fi

export PATH=$PATH:$_SDK_/tools:$_SDK_/platform-tools
export PATH_BASE_=$PATH

export NDK=$_NDK_
export DO_RELEASE_BUILD=1
export DO_PNG_BUILD=1
export NDK_CCACHE=""


## --- Coverity ----------------------------
cd $BUILD_PATH
conf_addon=''
if [ "$COVERITY_BUILD_""x" == "1x" ]; then
 sed -i -e 's#LIBS="$LIBS -rdynamic"#ABCDD="aaaabbb"#g' ../zanavi/configure
 cat ../zanavi/configure | grep 'rdynamic'
 cat ../zanavi/configure | grep 'aaaabbb'
 conf_addon=' --disable-shared '
fi
## --- Coverity ----------------------------


cd ${START_PATH}


## --- fix execute bit ---------------------
chmod a+rx "$SOURCE_PATH"/ci/do_android_build_generic_arch.sh
## --- fix execute bit ---------------------



# tweak ------------------------------------
rm -fv "$SOURCE_PATH"/intl/libgnuintl.h
# tweak ------------------------------------



if [ "${build_archs__[armv7]}""x" == "1x" ]; then
## --- prepare for armeabi-v7a build    ----
pushd ./
## -- config -------------------------------
#
export TC_NAME_=arm-linux-androideabi
export TC_VER_=4.9
export TOOLCHAIN_NAME_="$TC_NAME_"-"$TC_VER_"
export TOOLCHAIN_NAME_2_="$TC_NAME_"
export TOOLCHAIN_NAME_3_="arm-eabi-linux_android"
export LGCC_ADDON="armv7-a/"
export BUILD_PATH="$START_PATH""/android-bld-armv7"
export AND_API_LEVEL_C=14
export AND_API_LEVEL_J=21
export ARCH__="armv7"
export ARCH_2__="arm"
export ARCH_3__="armeabi-v7a"
export ARCH_4__="arm"
export OUTPUT_LIBPATH_="$START_PATH""/libs-build/armeabi-v7a/"

OPTIONS__COMPILE_ADDON_1=""
OPTIONS__COMPILE_ADDON_2=" -funwind-tables -fstack-protector -march=armv7-a -mfpu=vfpv3-d16 -mtune=arm7 -marm \
-mfloat-abi=softfp -finline-limit=64 -Wno-psabi "
OPTIONS__COMPILE_ADDON_3=" -march=armv7-a -Wl,--fix-cortex-a8 -Wl,--no-merge-exidx-entries "
OPTIMIZE_LEVEL=2
HOST_PARAM_='--host='
export OPTIONS__COMPILE_ADDON_1 OPTIONS__COMPILE_ADDON_2 OPTIONS__COMPILE_ADDON_3 OPTIMIZE_LEVEL HOST_PARAM_
#
## -- config -------------------------------
"$SOURCE_PATH"/ci/do_android_build_generic_arch.sh
err_code=$?
if [ $err_code != 0 ]; then
	echo "ERROR!"
	exit $err_code
fi
popd
## --- prepare for armeabi-v7a build    ----
fi

if [ "${build_archs__[x86]}""x" == "1x" ]; then
## --- prepare for x86 build    ------------
pushd ./
## -- config -------------------------------
#
export TC_NAME_=x86
export TC_VER_=4.9
export TOOLCHAIN_NAME_="$TC_NAME_"-"$TC_VER_"
export TOOLCHAIN_NAME_2_="i686-linux-android"
export TOOLCHAIN_NAME_3_="x86-linux_android"
export LGCC_ADDON=""
export BUILD_PATH="$START_PATH""/android-bld-x86"
export AND_API_LEVEL_C=14
export AND_API_LEVEL_J=21
export ARCH__="x86"
export ARCH_2__="$ARCH__"
export ARCH_3__="x86"
export ARCH_4__="x86"
export OUTPUT_LIBPATH_="$START_PATH""/libs-build/x86/"

OPTIONS__COMPILE_ADDON_1=""
OPTIONS__COMPILE_ADDON_2=" -funwind-tables -funswitch-loops -fno-stack-protector -finline-limit=300 -Ijni -DHAVE_ANDROID_X86 "
OPTIONS__COMPILE_ADDON_3=""
OPTIMIZE_LEVEL=3
HOST_PARAM_='--host='
export OPTIONS__COMPILE_ADDON_1 OPTIONS__COMPILE_ADDON_2 OPTIONS__COMPILE_ADDON_3 OPTIMIZE_LEVEL HOST_PARAM_
#
## -- config -------------------------------
"$SOURCE_PATH"/ci/do_android_build_generic_arch.sh
err_code=$?
if [ $err_code != 0 ]; then
	echo "ERROR!"
	exit $err_code
fi
popd
## --- prepare for x86 build    ------------
fi

if [ "${build_archs__[mips]}""x" == "1x" ]; then
## --- prepare for mips build    -----------
pushd ./
## -- config -------------------------------
#
# mipsel-linux-android-4.9
export TC_NAME_=mipsel-linux-android
export TC_VER_=4.9
export TOOLCHAIN_NAME_="$TC_NAME_"-"$TC_VER_"
export TOOLCHAIN_NAME_2_="mipsel-linux-android"
export TOOLCHAIN_NAME_3_="mips"
export LGCC_ADDON=""
export BUILD_PATH="$START_PATH""/android-bld-mips"
export AND_API_LEVEL_C=14
export AND_API_LEVEL_J=21
export ARCH__="mips"
export ARCH_2__="$ARCH__"
export ARCH_3__="mips"
export ARCH_4__="mips"
export OUTPUT_LIBPATH_="$START_PATH""/libs-build/mips/"

OPTIONS__COMPILE_ADDON_1=" --enable-avoid-float "
OPTIONS__COMPILE_ADDON_2=" -funwind-tables -fstack-protector -finline-limit=64 \
-Wno-psabi "
OPTIONS__COMPILE_ADDON_3=" "
OPTIMIZE_LEVEL=2
HOST_PARAM_='--host='
export OPTIONS__COMPILE_ADDON_1 OPTIONS__COMPILE_ADDON_2 OPTIONS__COMPILE_ADDON_3 OPTIMIZE_LEVEL HOST_PARAM_
#
## -- config -------------------------------
"$SOURCE_PATH"/ci/do_android_build_generic_arch.sh
err_code=$?
if [ $err_code != 0 ]; then
	echo "ERROR!"
	exit $err_code
fi
popd
## --- prepare for mips build    -----------
fi

if [ "${build_archs__[arm64]}""x" == "1x" ]; then
## --- prepare for ARM64 build    ------------
pushd ./
## -- config -------------------------------
#
export TC_NAME_=aarch64-linux-android
export TC_VER_=4.9
export TOOLCHAIN_NAME_="$TC_NAME_"-"$TC_VER_"
export TOOLCHAIN_NAME_2_="$TC_NAME_"
export TOOLCHAIN_NAME_3_="aarch64" # -linux-android
export LGCC_ADDON=""
export BUILD_PATH="$START_PATH""/android-build"
export AND_API_LEVEL_C=21
export AND_API_LEVEL_J=21
export ARCH__="arm64"
export ARCH_2__="arm64"
export ARCH_3__="arm64-v8a"
export ARCH_4__="arm64"
export OUTPUT_LIBPATH_="$START_PATH""/libs-build/arm64-v8a/"

OPTIONS__COMPILE_ADDON_1=""
OPTIONS__COMPILE_ADDON_2=" -funwind-tables -fstack-protector-strong -no-canonical-prefixes -finline-limit=64 -Wno-psabi -DANDROID_ARM64 "
OPTIONS__COMPILE_ADDON_3=" -Wl,--no-merge-exidx-entries "
OPTIMIZE_LEVEL=2
HOST_PARAM_='--host='
export OPTIONS__COMPILE_ADDON_1 OPTIONS__COMPILE_ADDON_2 OPTIONS__COMPILE_ADDON_3 OPTIMIZE_LEVEL HOST_PARAM_
#
## -- config -------------------------------
"$SOURCE_PATH"/ci/do_android_build_generic_arch.sh
err_code=$?
if [ $err_code != 0 ]; then
	echo "ERROR!"
	exit $err_code
fi
popd
## --- prepare for ARM64 build    ------------
fi

if [ "${build_archs__[arm]}""x" == "1x" ]; then
## --- prepare for ARM build    ------------
pushd ./
## -- config -------------------------------
#
export TC_NAME_=arm-linux-androideabi
export TC_VER_=4.9
export TOOLCHAIN_NAME_="$TC_NAME_"-"$TC_VER_"
export TOOLCHAIN_NAME_2_="$TC_NAME_"
export TOOLCHAIN_NAME_3_="arm-eabi-linux_android"
export LGCC_ADDON=""
export BUILD_PATH="$START_PATH""/android-build"
export AND_API_LEVEL_C=14
export AND_API_LEVEL_J=21
export ARCH__="arm"
export ARCH_2__="$ARCH__"
export ARCH_3__="armeabi"
export ARCH_4__="arm"
export OUTPUT_LIBPATH_="$START_PATH""/libs-build/armeabi/"

OPTIONS__COMPILE_ADDON_1=" --enable-avoid-float "
OPTIONS__COMPILE_ADDON_2=" -funwind-tables -fstack-protector -march=armv5te -msoft-float -mthumb -finline-limit=64 \
-D__ARM_ARCH_5__ -D__ARM_ARCH_5T__ -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__ -Wno-psabi "
OPTIONS__COMPILE_ADDON_3=" -Wl,--no-merge-exidx-entries "
OPTIMIZE_LEVEL=2
HOST_PARAM_='--host='
export OPTIONS__COMPILE_ADDON_1 OPTIONS__COMPILE_ADDON_2 OPTIONS__COMPILE_ADDON_3 OPTIMIZE_LEVEL HOST_PARAM_
#
## -- config -------------------------------
"$SOURCE_PATH"/ci/do_android_build_generic_arch.sh
err_code=$?
if [ $err_code != 0 ]; then
	echo "ERROR!"
	exit $err_code
fi
popd
## --- prepare for ARM build    ------------
fi











## --- prepare for APK generation ----------
export BUILD_PATH="$BUILD_PATH_MAIN_ARM"
cd $BUILD_PATH/navit


if [ "$FULL_LOG""x" == "1x" ]; then
	make apkg-release || pwd
else
	make apkg-release 2>&1 | grep '\[javac\]' || pwd
fi
## --- prepare for APK generation ----------



## --- prepare for Gradle build ------------
export BUILD_PATH="$BUILD_PATH_MAIN_ARM"
cd "$BUILD_PATH"/navit

echo "+++++++++++++"
pwd
echo "+++++++++++++"

mkdir -p $BUILD_PATH/navit/android/nativelibs/
cp -av "$START_PATH"/libs-build/* "$BUILD_PATH"/navit/android/nativelibs/

# mv -v android/libs/*jar android/lib/

cp -v "$SOURCE_PATH"/navit/build.gradle ./
cp -v "$SOURCE_PATH"/navit/gradle.properties ./
cp -v "$SOURCE_PATH"/navit/settings.gradle ./
cp -av "$SOURCE_PATH"/navit/config ./
## -------------------------


## -------------------------
echo "======================="
pwd
echo "======================="
ls -al ./
echo "======================="
ls -al android/
echo "======================="
ls -al android/lib/
echo "======================="
ls -al android/libs/
echo "======================="
ls -alR android/libs/
echo "======================="
## -------------------------

echo "+++++++++++++"
pwd
echo "+++++++++++++"

cd ./android

echo "+++++++++++++"
pwd
echo "+++++++++++++"

sed -i -e 's#android:debuggable="true"#android:debuggable="false"#' AndroidManifest.xml

## --- prepare for Gradle build ------------

