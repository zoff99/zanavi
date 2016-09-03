#!/bin/bash

export START_PATH=~/
export SOURCE_PATH="$START_PATH""/"${CIRCLE_PROJECT_REPONAME}"/"

export ANDROID_NDK="/usr/local/android-ndk/"
export _NDK_="$ANDROID_NDK"
 
export ANDROID_SDK="/usr/local/android-sdk-linux/"
export _SDK_="$ANDROID_SDK"

export BUILD_PATH="$START_PATH""/android-build"
mkdir -p $BUILD_PATH

echo "================================="
pwd
echo "================================="
ls -al
echo "================================="
echo $ANDROID_HOME
echo "================================="
echo $START_PATH
echo "================================="
echo $SOURCE_PATH
echo "================================="
echo "$BUILD_PATH"
echo "================================="
ls -al "$BUILD_PATH"/
echo "================================="
echo ${CIRCLE_PROJECT_REPONAME}
echo "================================="
# type -a ccache
# echo "================================="

# patch for circleCI -------------
pwd
ls -al navit/android/src/com/zoffcc/applications/zanavi/Navit.java
sed -i -e 's#static final int CIDEBUG =.*#static final int CIDEBUG = 1;#' navit/android/src/com/zoffcc/applications/zanavi/Navit.java
cat navit/android/src/com/zoffcc/applications/zanavi/Navit.java | grep 'static final int CIDEBUG'
ls -al navit/android/src/com/zoffcc/applications/zanavi/Navit.java
# ============
ls -al navit/debug.h
sed -i -e 'sc// #define _CIDEBUG_BUILD_ 1c#define _CIDEBUG_BUILD_ 1c' navit/debug.h
cat navit/debug.h | grep 'CIDEBUG_BUILD'
ls -al navit/debug.h
# patch for circleCI -------------


rm navit/maptool/poly2tri-c/001/seidel-1.0/triangulate
rm pngout-static
echo '#! /bin/bash' > pngout-static && \
echo 'echo $*' >> pngout-static && \
chmod u+rx pngout-static

if [ `uname -m` == 'x86_64' ] ; then SUFFIX2='_64' ; else SUFFIX2='' ; fi && \
export PATH=$PATH:$_SDK_/tools:$_SDK_/platform-tools:$_NDK_/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86$SUFFIX2/bin

cd $BUILD_PATH

# echo "cleaning ccache cache ..."
# ccache -c
# ccache -C
# echo "... done"
CCACHE=""

conf_addon=''

if [ "$COVERITY_BUILD_""x" == "1x" ]; then
 sed -i -e 's#LIBS="$LIBS -rdynamic"#ABCDD="aaaabbb"#g' ../zanavi/configure
 cat ../zanavi/configure | grep 'rdynamic'
 cat ../zanavi/configure | grep 'aaaabbb'
 conf_addon=' --disable-shared '
fi

DEBUG_="-fpic -ffunction-sections -fstack-protector -fomit-frame-pointer -fno-strict-aliasing -D__ARM_ARCH_5__ -D__ARM_ARCH_5T__ -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__  -Wno-psabi -march=armv5te -msoft-float -mthumb -finline-limit=64 -DHAVE_API_ANDROID -DANDROID  -Wa,--noexecstack -O3 -I$_NDK_/platforms/android-14/arch-arm/usr/include -nostdlib -Wl,-rpath-link=$_NDK_/platforms/android-14/arch-arm/usr/lib -L$_NDK_/platforms/android-14/arch-arm/usr/lib"

../zanavi/configure RANLIB=arm-linux-androideabi-ranlib AR=arm-linux-androideabi-ar CC="$CCACHE arm-linux-androideabi-gcc -O2 $DEBUG_ -L. -L$_NDK_/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86$SUFFIX2/lib/gcc/arm-linux-androideabi/4.8/ -lgcc -ljnigraphics " CXX="$CCACHE arm-linux-androideabi-g++ -O2 -fno-rtti -fno-exceptions -L$_NDK_/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86$SUFFIX2/lib/gcc/arm-linux-androideabi/4.8/ -lgcc -ljnigraphics " --host=arm-eabi-linux_android --enable-avoid-float --enable-avoid-unaligned --disable-glib --disable-gmodule --disable-vehicle-gpsd $conf_addon --enable-vehicle-demo --disable-binding-dbus --disable-speech-cmdline --disable-gui-gtk --disable-font-freetype --disable-fontconfig --disable-graphics-qt-qpainter --disable-graphics-gtk-drawing-area --disable-maptool --enable-cache-size=20971520 --enable-svg2png-scaling=8,16,32,48,64,96,192,384 --enable-svg2png-scaling-nav=48,64,59,96,192,384 --enable-svg2png-scaling-flag=32 --with-xslts=android,plugin_menu --with-saxon=saxonb-xslt --enable-transformation-roll --with-android-project="android-21"  > /dev/null 2> /dev/null

if [ "$COVERITY_BUILD_""x" == "1x" ]; then
 export AND_API_LEVEL_C=14 && \
        export NDK=$_NDK_ && \
        export DO_RELEASE_BUILD=1 && \
        export DO_PNG_BUILD=1 && \
        export NDK_CCACHE="" && \
 export PATH=/home/ubuntu/cov_scan/cov-analysis-linux64-8.5.0/bin:/usr/local/android-ndk/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64/bin:$PATH
 cd ~/android-build/
 # cov-configure --comptype gcc --compiler /usr/local/android-ndk/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc
 cov-configure -co arm-linux-androideabi-gcc -- -march=armv5te -msoft-float -mthumb
 make clean
 cov-build --dir cov-int make

 ls -al /home/ubuntu/android-build/navit/.libs/lib_data_data_com.zoffcc.applications.zanavi_lib_navit.so
 ls -al /home/ubuntu/android-build/navit/.libs/navit2
 ls -al /home/ubuntu/android-build/navit/navit2

 make clean
 conf_addon=''
 sed -i -e 's#ABCDD="aaaabbb"#LIBS="$LIBS -rdynamic"#g' ../zanavi/configure
 cat ../zanavi/configure | grep 'rdynamic'
 cat ../zanavi/configure | grep 'aaaabbb'
 ../zanavi/configure RANLIB=arm-linux-androideabi-ranlib AR=arm-linux-androideabi-ar CC="$CCACHE arm-linux-androideabi-gcc -O2 $DEBUG_ -L. -L$_NDK_/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86$SUFFIX2/lib/gcc/arm-linux-androideabi/4.8/ -lgcc -ljnigraphics " CXX="$CCACHE arm-linux-androideabi-g++ -O2 -fno-rtti -fno-exceptions -L$_NDK_/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86$SUFFIX2/lib/gcc/arm-linux-androideabi/4.8/ -lgcc -ljnigraphics " --host=arm-eabi-linux_android --enable-avoid-float --enable-avoid-unaligned --disable-glib --disable-gmodule --disable-vehicle-gpsd $conf_addon --enable-vehicle-demo --disable-binding-dbus --disable-speech-cmdline --disable-gui-gtk --disable-font-freetype --disable-fontconfig --disable-graphics-qt-qpainter --disable-graphics-gtk-drawing-area --disable-maptool --enable-cache-size=20971520 --enable-svg2png-scaling=8,16,32,48,64,96,192,384 --enable-svg2png-scaling-nav=48,64,59,96,192,384 --enable-svg2png-scaling-flag=32 --with-xslts=android,plugin_menu --with-saxon=saxonb-xslt --enable-transformation-roll --with-android-project="android-21"  > /dev/null 2> /dev/null

fi

export AND_API_LEVEL_C=14 && \
        export NDK=$_NDK_ && \
        export DO_RELEASE_BUILD=1 && \
        export DO_PNG_BUILD=1 && \
        export NDK_CCACHE="" && \
        make 2>&1 | grep -i error

ls -al /home/ubuntu/android-build/navit/.libs/lib_data_data_com.zoffcc.applications.zanavi_lib_navit.so
ls -al /home/ubuntu/android-build/navit/.libs/navit2
ls -al /home/ubuntu/android-build/navit/navit2

## -------------------------
b_arch = $(if [ "$(DO_X86_BUILD)" == "1" ]; then echo "x86"; elif [ "$(DO_ARMV7_BUILD)" == "1" ]; then echo "armeabi-v7a"; else echo "armeabi"; fi)
make android/AndroidManifest.xml
make android/build.xml

mkdir -p android/libs/$(b_arch)
cp .libs/*.so android/libs/$(b_arch)/libnavit.so
for i in */*/.libs/*.so ; do sed 's&lib_data_data_com\.zoffcc\.applications\.zanavi_lib_&/data/data/com.zoffcc.applications.zanavi/lib/lib&' < "$i" > android/libs/$b_arch/$(basename "$i"); done

        mkdir -p android/res/drawable-ldpi
        mkdir -p android/res/drawable-mdpi
        mkdir -p android/res/drawable-hdpi
        mkdir -p android/res/drawable-xhdpi
        mkdir -p android/res/drawable-xxhdpi

        mkdir -p android/res2/drawable-hdpi
        mkdir -p android/res2/drawable-mdpi
        mkdir -p android/res2/drawable-ldpi



ls -al android/libs/$(b_arch)/lib*.so
rm -f android/libs/$(b_arch)/libgraphics_android.so
rm -f android/libs/$(b_arch)/libgraphics_null.so
rm -f android/libs/$(b_arch)/libgui_internal.so
rm -f android/libs/$(b_arch)/libmap_binfile.so
rm -f android/libs/$(b_arch)/libmap_csv.so
rm -f android/libs/$(b_arch)/libmap_filter.so
rm -f android/libs/$(b_arch)/libmap_mg.so
rm -f android/libs/$(b_arch)/libmap_shapefile.so
rm -f android/libs/$(b_arch)/libmap_textfile.so
rm -f android/libs/$(b_arch)/libosd_core.so
rm -f android/libs/$(b_arch)/libspeech_android.so
rm -f android/libs/$(b_arch)/libvehicle_android.so
rm -f android/libs/$(b_arch)/libvehicle_demo.so
rm -f android/libs/$(b_arch)/libvehicle_file.so
rm -f android/libs/$(b_arch)/libvehicle_pipe.so
rm -f android/libs/$(b_arch)/libvehicle_serial.so
rm -f android/libs/$(b_arch)/libvehicle_socket.so

mkdir -p android/res/raw
for i in $(cd ../po && echo *.mo); do cp ../po/"$i" android/res/raw/$( echo "$i" | tr "[A-Z]" "[a-z]") ; done
cp navit_android_mdpi.xml android/res/raw/navitmdpi.xml
cp navit_android_ldpi.xml android/res/raw/navitldpi.xml
cp navit_android_hdpi.xml android/res/raw/navithdpi.xml
find . -type d -name '\.svn' -exec rm -Rf {} \; ; fi; echo "ignore the find errors!!"

cp -v "$SOURCE_PATH"/navit/build.gradle ./
cp -v "$SOURCE_PATH"/navit/settings.gradle ./
pwd
ls -al ./
ls -al android/
## -------------------------

cd ./android

pwd
cat AndroidManifest.xml | sed -e 's#android:debuggable="true"#android:debuggable="false"#' > l.txt
mv l.txt AndroidManifest.xml


