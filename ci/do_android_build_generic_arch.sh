#!/bin/bash


set -x

mkdir -p $BUILD_PATH
cd $BUILD_PATH
export PATH=$PATH_BASE_:${_NDK_}/toolchains/${TOOLCHAIN_NAME_}/prebuilt/linux-x86${SUFFIX2}/bin
export LIBGCC_DIR="${_NDK_}/toolchains/${TOOLCHAIN_NAME_}/prebuilt/linux-x86${SUFFIX2}/lib/gcc/${TOOLCHAIN_NAME_2_}/${TC_VER_}/""${LGCC_ADDON}"
export PLATFORM_ROOT=$NDK/platforms/android-${AND_API_LEVEL_C}/arch-${ARCH_4__}


#	mkdir -p ..../build_xxx/navit/obj/local/armeabi-v7a/objs-debug/zanavi/

DEBUG_="-fpic -ffunction-sections $OPTIONS__COMPILE_ADDON_2 -fomit-frame-pointer -fno-strict-aliasing \
	-DHAVE_API_ANDROID \
	-DANDROID  \
	-Wa,--noexecstack \
	-O${OPTIMIZE_LEVEL} \
	-I$_NDK_/platforms/android-"$AND_API_LEVEL_C"/arch-${ARCH_4__}/usr/include \
	-Wl,-rpath-link=$_NDK_/platforms/android-"$AND_API_LEVEL_C"/arch-${ARCH_4__}/usr/lib \
	-L$_NDK_/platforms/android-"$AND_API_LEVEL_C"/arch-${ARCH_4__}/usr/lib"

#	-nostdlib \


${SOURCE_PATH}/configure RANLIB="${TOOLCHAIN_NAME_2_}-ranlib" AR="${TOOLCHAIN_NAME_2_}-ar" LD="${TOOLCHAIN_NAME_2_}-ld" \
	CC="${NDK_CCACHE}${TOOLCHAIN_NAME_2_}-gcc -O${OPTIMIZE_LEVEL} $DEBUG_ -L. -L${LIBGCC_DIR} --sysroot=${_NDK_}/platforms/android-${AND_API_LEVEL_C}/arch-${ARCH_4__} -lgcc -lc -ljnigraphics " \
	CXX="${NDK_CCACHE}${TOOLCHAIN_NAME_2_}-g++ -O${OPTIMIZE_LEVEL} -fno-rtti -fno-exceptions ${OPTIONS__COMPILE_ADDON_3} --sysroot=${_NDK_}/platforms/android-${AND_API_LEVEL_C}/arch-${ARCH_4__} \
	-L$LIBGCC_DIR -lgcc -lc -ljnigraphics " \
	${HOST_PARAM_}${TOOLCHAIN_NAME_3_}${OPTIONS__COMPILE_ADDON_1} \
	--enable-avoid-unaligned \
	--disable-glib \
	--disable-gmodule \
	--disable-vehicle-gpsd "$conf_addon" \
	--enable-vehicle-demo \
	--disable-binding-dbus \
	--disable-speech-cmdline \
	--disable-gui-gtk \
	--disable-font-freetype \
	--disable-fontconfig \
	--disable-graphics-qt-qpainter \
	--disable-graphics-gtk-drawing-area \
	--disable-maptool \
	--enable-cache-size=20971520 \
	--enable-svg2png-scaling=8,16,32,48,64,96,192,384 \
	--enable-svg2png-scaling-nav=48,64,59,96,192,384 \
	--enable-svg2png-scaling-flag=32 \
	--with-xslts=android,plugin_menu \
	--with-saxon=saxonb-xslt \
	--enable-transformation-roll \
	--with-android-project="android-""$AND_API_LEVEL_J"	
	# > /dev/null 2> /dev/null









if [ "$ARCH__""x" == "armx" ]; then
	## --- Coverity ----------------------------
	if [ "$COVERITY_BUILD_""x" == "1x" ]; then
	 export AND_API_LEVEL_C=14 && \
	        export NDK=$_NDK_ && \
	        export DO_RELEASE_BUILD=1 && \
	        export DO_PNG_BUILD=1 && \
	        export NDK_CCACHE="" && \
	 export PATH=/home/ubuntu/cov_scan/cov-analysis-linux64-8.5.0/bin:/usr/local/android-ndk/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64/bin:$PATH
	 cd $BUILD_PATH
	 # cov-configure --comptype gcc --compiler /usr/local/android-ndk/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc
	 cov-configure -co arm-linux-androideabi-gcc -- -march=armv5te -msoft-float -mthumb
	 make clean
	 cov-build --dir cov-int make
	
	 ls -al $BUILD_PATH/navit/.libs/lib_data_data_com.zoffcc.applications.zanavi_lib_navit.so
	 ls -al $BUILD_PATH/navit/.libs/navit2
	 ls -al $BUILD_PATH/navit/navit2
	
	 make clean
	 conf_addon=''
	 sed -i -e 's#ABCDD="aaaabbb"#LIBS="$LIBS -rdynamic"#g' ${SOURCE_PATH}/configure
	 cat ${SOURCE_PATH}/configure | grep 'rdynamic'
	 cat ${SOURCE_PATH}/configure | grep 'aaaabbb'
	 ${SOURCE_PATH}/configure RANLIB=arm-linux-androideabi-ranlib AR=arm-linux-androideabi-ar CC="$NDK_CCACHE arm-linux-androideabi-gcc -O2 $DEBUG_ -L. -L$_NDK_/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86$SUFFIX2/lib/gcc/arm-linux-androideabi/4.8/ -lgcc -ljnigraphics " CXX="$NDK_CCACHE arm-linux-androideabi-g++ -O2 -fno-rtti -fno-exceptions -L$_NDK_/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86$SUFFIX2/lib/gcc/arm-linux-androideabi/4.8/ -lgcc -ljnigraphics " --host=arm-eabi-linux_android --enable-avoid-float --enable-avoid-unaligned --disable-glib --disable-gmodule --disable-vehicle-gpsd $conf_addon --enable-vehicle-demo --disable-binding-dbus --disable-speech-cmdline --disable-gui-gtk --disable-font-freetype --disable-fontconfig --disable-graphics-qt-qpainter --disable-graphics-gtk-drawing-area --disable-maptool --enable-cache-size=20971520 --enable-svg2png-scaling=8,16,32,48,64,96,192,384 --enable-svg2png-scaling-nav=48,64,59,96,192,384 --enable-svg2png-scaling-flag=32 --with-xslts=android,plugin_menu --with-saxon=saxonb-xslt --enable-transformation-roll --with-android-project="android-21"  > /dev/null 2> /dev/null
	
	fi
	## --- Coverity ----------------------------
fi






pushd ./
if [ "$FULL_LOG""x" == "1x" ]; then
        make
else
        make 2>&1 | grep -i error
fi
popd
cd navit


echo "+++++++++++++"
pwd
echo "+++++++++++++"

ls -al $BUILD_PATH/navit/.libs/lib_data_data_com.zoffcc.applications.zanavi_lib_navit.so
ls -al $BUILD_PATH/navit/.libs/navit2
ls -al $BUILD_PATH/navit/navit2

mkdir -p ${OUTPUT_LIBPATH_}/
cp -av $BUILD_PATH/navit/.libs/lib_data_data_com.zoffcc.applications.zanavi_lib_navit.so ${OUTPUT_LIBPATH_}/libnavit.so
echo "before strip:"
ls -al ${OUTPUT_LIBPATH_}/libnavit.so
${TOOLCHAIN_NAME_2_}-strip ${OUTPUT_LIBPATH_}/libnavit.so
echo "after strip:"
ls -al ${OUTPUT_LIBPATH_}/libnavit.so
echo ""

echo "output path=""$OUTPUT_LIBPATH_/"
ls -al $OUTPUT_LIBPATH_/


if [ ! -e "$OUTPUT_LIBPATH_/libnavit.so" ]; then
	echo "ERROR:${ARCH__}:library not built"
	exit 1
fi


