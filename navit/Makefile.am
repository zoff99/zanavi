include $(top_srcdir)/Makefile.inc
comma=,
SUBDIRS=fib-1.1 support
if PLUGINS
  SUBDIRS += .
endif
DIST_SUBDIRS=autoload binding map maptool fib-1.1 font fonts gui graphics osd plugin speech vehicle support maps
SUBDIRS+=autoload binding map font gui graphics osd plugin speech vehicle

MODULES = $(wildcard $(top_builddir)/navit/binding/*/*.la $(top_builddir)/navit/font/*/*.la $(top_builddir)/navit/graphics/*/*.la $(top_builddir)/navit/gui/*/*.la $(top_builddir)/navit/map/*/*.la $(top_builddir)/navit/osd/*/*.la $(top_builddir)/navit/speech/*/*.la $(top_builddir)/navit/vehicle/*/*.la)

if FONTS
  SUBDIRS += fonts
endif
if !PLUGINS
  SUBDIRS += .
endif

if MAPTOOL
  SUBDIRS += maptool
endif

if BUILD_SAMPLEMAP
  SUBDIRS += maps
endif


AM_CPPFLAGS = -I$(top_srcdir)/navit/fib-1.1 @NAVIT_CFLAGS@ @ZLIB_CFLAGS@ -DPREFIX=\"@prefix@\" -DLIBDIR=\"@libdir@\" -DMODULE=navit
BUILT_SOURCES = version.h navit_config.h

if BIN_NAVIT
  bin_PROGRAMS = navit
endif

if SHARED_LIBNAVIT
  lib_LTLIBRARIES        = lib@LIBNAVIT@.la
  lib@LIBNAVIT@_la_LDFLAGS = -avoid-version @MODULE_LDFLAGS@ @NAVIT_SOCKET_LDFLAGS@ -no-undefined -Wl,--no-undefined
else
  noinst_LTLIBRARIES        = lib@LIBNAVIT@.la
endif
lib@LIBNAVIT@_la_LIBADD = @NAVIT_LIBS@ @WORDEXP_LIBS@ @ZLIB_LIBS@ @INTLLIBS@ -Lfib-1.1 -lfib @MODULE_LIBADD@ @CRYPTO_LIBS@

if SUPPORT_WIN32CE
  lib@LIBNAVIT@_la_LIBADD += -Lsupport/libc -lsupport_libc
endif

pkgdata_DATA = navit.xml

EXTRA_DIST = navit_shipped.xml navit.dtd

lib@LIBNAVIT@_la_SOURCES = announcement.c atom.c attr.c cache.c callback.c command.c compass.c config_.c coord.c country.c data_window.c debug.c \
	event.c event_glib.h file.c graphics.c gui.c item.c layout.c log.c main.c map.c \
	linguistics.c mapset.c maptype.c menu.c messages.c bookmarks.c bookmarks.h navit.c navigation.c osd.c param.c phrase.c plugin.c popup.c \
	profile.c projection.c roadprofile.c route.c routech.c search.c speech.c start_real.c transform.c track.c \
	util.c vehicle.c vehicleprofile.c xmlconfig.c announcement.h atom.h attr.h attr_def.h cache.h callback.h color.h command.h compass.h config_.h coord.h country.h \
	android.h data.h data_window.h data_window_int.h debug.h destination.h draw_info.h endianess.h event.h \
	file.h graphics.h gtkext.h gui.h item.h item_def.h keys.h log.h layer.h layout.h linguistics.h main.h map-share.h map.h\
	map_data.h mapset.h maptype.h menu.h messages.h navigation.h navit.h osd.h \
	param.h phrase.h plugin.h point.h plugin_def.h projection.h popup.h route.h profile.h roadprofile.h search.h speech.h start_real.h \
	transform.h track.h types.h util.h vehicle.h vehicleprofile.h window.h xmlconfig.h zipfile.h \
	navit_nls.h sunriset.c sunriset.h glib_slice.h maptool/miniz.c \
	coffeecatch.c coffeejni.c \
	snprintf.c snprintf.h


if SUPPORT_ANDROID
  lib@LIBNAVIT@_la_SOURCES += vehicle/android/vehicle_android.c \
	vehicle/demo/vehicle_demo.c \
	gui/internal/gui_internal.c \
	graphics/android/graphics_android.c \
	map/binfile/binfile.c \
	map/csv/csv.c \
	map/csv/quadtree.c \
	map/textfile/textfile.c \
	osd/core/osd_core.c \
	speech/android/speech_android.c
endif


XSLTS=@XSLTS@
navit.xml: navit_shipped.xml  $(foreach xslt, $(subst $(comma), ,$(XSLTS)), $(addsuffix .xslt,$(addprefix xslt/,$(xslt)))) navit_android_xml
	cp $< navit.xml.new
	if [ ! -f navit.dtd ]; then cp -f $(top_srcdir)/navit/navit.dtd .; fi
	for i in $^; do if [ "$${i%.xslt}" != "$$i" ]; then echo "Applying $$i" ; @SAXON@ -snone navit.xml.new $$i >navit.xml.tmp || exit ; mv navit.xml.tmp navit.xml.new || exit ; fi ; done
	mv navit.xml.new navit.xml
	rm -f navit.xml.tmp

navit_android_xml: navit_shipped.xml
if SUPPORT_ANDROID
#	new android xml files for (mdpi,ldpi,hdpi)
	cp $(srcdir)/navit_shipped.xml navit_android.xml.new
	if [ ! -f navit.dtd ]; then cp -f $(top_srcdir)/navit/navit.dtd .; fi

#	# now apply any other xslt files (this would allow for pedestrian plugin)
#	for i in $(foreach xslt, $(subst $(comma), ,$(XSLTS)), $(addsuffix .xslt,$(addprefix xslt/,$(xslt)))); do if [ "$${i%.xslt}" != "$$i" ]; then j=`basename $$i` ; if [ "$$j" != "android.xslt" ] ; then echo "Android: Applying $$i" ; @SAXON@ -snone navit_android.xml.new $(srcdir)/"$$i" >navit_android.xml.new.tmp || exit ; mv navit_android.xml.new.tmp navit_android.xml.new || exit ; fi ; fi ; done
#	# for now disable pedestrian plugin (zoff99)

	@SAXON@ -snone navit_android.xml.new $(srcdir)/xslt/android_mdpi.xslt >navit_android_mdpi.xml.tmp
	@SAXON@ -snone navit_android.xml.new $(srcdir)/xslt/android_ldpi.xslt >navit_android_ldpi.xml.tmp
	@SAXON@ -snone navit_android.xml.new $(srcdir)/xslt/android_hdpi.xslt >navit_android_hdpi.xml.tmp
	rm -f navit_android.xml.new
	rm -f navit_android.xml.new.tmp
	mv navit_android_mdpi.xml.tmp navit_android_mdpi.xml
	mv navit_android_ldpi.xml.tmp navit_android_ldpi.xml
	mv navit_android_hdpi.xml.tmp navit_android_hdpi.xml
	rm -f navit_android_mdpi.xml.tmp
	rm -f navit_android_ldpi.xml.tmp
	rm -f navit_android_hdpi.xml.tmp
else
	echo "no Android support"
endif

if SUPPORT_ANDROID
  lib@LIBNAVIT@_la_SOURCES += android.c
  navit_SOURCES =
  navit_LDADD =

v_int = $$(cat $(srcdir)/android/Android_Market_Version_int.txt|grep v_int|cut -d ":" -f 2)
# v_name = "1.0.0-"$$(LANG=C svnversion $(top_srcdir) 2> /dev/null|tr -d ":A-Z" 2> /dev/null)
v_name = $$(cat $(srcdir)/android/Android_Market_Version_int.txt|grep v_name|cut -d ":" -f 2)
b_arch = $(shell if [ "$(DO_X86_BUILD)" == "1" ]; then echo "x86"; elif [ "$(DO_ARMV7_BUILD)" == "1" ]; then echo "armeabi-v7a"; else echo "armeabi"; fi)


android/AndroidManifest.xml:
	echo "making::android/AndroidManifest.xml"
	mkdir -p $(builddir)/android
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -v $(srcdir)/android/AndroidManifest.xml.in $(builddir)/android/AndroidManifest.xml.in ; fi
	sed -e "s/@ANDROID_VERSION_INT@/$(v_int)/" <android/AndroidManifest.xml.in >android/AndroidManifest.xml.tmp
	mv android/AndroidManifest.xml.tmp android/AndroidManifest.xml.in
	sed -e "s/@ANDROID_VERSION_NAME@/$(v_name)/" <android/AndroidManifest.xml.in >android/AndroidManifest.xml.tmp
	mv android/AndroidManifest.xml.tmp android/AndroidManifest.xml

android/build.xml: android/AndroidManifest.xml android/dummy.txt
#	this is needed, otherwise somehow the files cant be deleted

	echo "making::android/build.xml"
	touch $(builddir)/android/build.xml

	mkdir -p $(builddir)/android/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android/* $(builddir)/android; fi

#	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android/lib/; fi
#	mkdir -p $(builddir)/android/lib/
#	if [ "$(builddir)" != "$(srcdir)" ]; then cp -pf $(srcdir)/android/tagsoup-1.2.1.jar $(builddir)/android/lib/; fi

	touch $(srcdir)/android/dummy.txt
	touch $(builddir)/android/dummy.txt


######## --------------------------
	mkdir -p $(builddir)/android-about-page/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-about-page/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-about-page/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-about-page/* $(builddir)/android-about-page/; fi
	touch $(srcdir)/android-about-page/dummy.txt
	touch $(builddir)/android-about-page/dummy.txt
######## --------------------------


######## --------------------------
	mkdir -p $(builddir)/android-support-animated-vector-drawable/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-animated-vector-drawable/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-animated-vector-drawable/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-animated-vector-drawable/* $(builddir)/android-support-animated-vector-drawable/; fi
	touch $(srcdir)/android-support-animated-vector-drawable/dummy.txt
	touch $(builddir)/android-support-animated-vector-drawable/dummy.txt
######## --------------------------


######## --------------------------
	mkdir -p $(builddir)/android-support-annotations/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-annotations/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-annotations/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-annotations/* $(builddir)/android-support-annotations/; fi
	touch $(srcdir)/android-support-annotations/dummy.txt
	touch $(builddir)/android-support-annotations/dummy.txt
######## --------------------------


######## --------------------------
	mkdir -p $(builddir)/android-support-compat/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-compat/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-compat/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-compat/* $(builddir)/android-support-compat/; fi
	touch $(srcdir)/android-support-compat/dummy.txt
	touch $(builddir)/android-support-compat/dummy.txt
######## --------------------------

######## --------------------------
	mkdir -p $(builddir)/android-support-core-ui/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-core-ui/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-core-ui/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-core-ui/* $(builddir)/android-support-core-ui/; fi
	touch $(srcdir)/android-support-core-ui/dummy.txt
	touch $(builddir)/android-support-core-ui/dummy.txt
######## --------------------------

######## --------------------------
	mkdir -p $(builddir)/android-support-core-utils/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-core-utils/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-core-utils/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-core-utils/* $(builddir)/android-support-core-utils/; fi
	touch $(srcdir)/android-support-core-utils/dummy.txt
	touch $(builddir)/android-support-core-utils/dummy.txt
######## --------------------------

######## --------------------------
	mkdir -p $(builddir)/android-support-design/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-design/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-design/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-design/* $(builddir)/android-support-design/; fi
	touch $(srcdir)/android-support-design/dummy.txt
	touch $(builddir)/android-support-design/dummy.txt
######## --------------------------


######## --------------------------
	mkdir -p $(builddir)/android-support-fragment/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-fragment/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-fragment/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-fragment/* $(builddir)/android-support-fragment/; fi
	touch $(srcdir)/android-support-fragment/dummy.txt
	touch $(builddir)/android-support-fragment/dummy.txt
######## --------------------------

######## --------------------------
	mkdir -p $(builddir)/android-support-media-compat/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-media-compat/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-media-compat/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-media-compat/* $(builddir)/android-support-media-compat/; fi
	touch $(srcdir)/android-support-media-compat/dummy.txt
	touch $(builddir)/android-support-media-compat/dummy.txt
######## --------------------------

######## --------------------------
	mkdir -p $(builddir)/android-support-v4/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-v4/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-v4/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-v4/* $(builddir)/android-support-v4/; fi
	touch $(srcdir)/android-support-v4/dummy.txt
	touch $(builddir)/android-support-v4/dummy.txt
######## --------------------------


######## --------------------------
	mkdir -p $(builddir)/android-support-v7-appcompat/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-v7-appcompat/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-v7-appcompat/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-v7-appcompat/* $(builddir)/android-support-v7-appcompat/; fi
	touch $(srcdir)/android-support-v7-appcompat/dummy.txt
	touch $(builddir)/android-support-v7-appcompat/dummy.txt
######## --------------------------


######## --------------------------
	mkdir -p $(builddir)/android-support-v7-recyclerview/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-v7-recyclerview/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-v7-recyclerview/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-v7-recyclerview/* $(builddir)/android-support-v7-recyclerview/; fi
	touch $(srcdir)/android-support-v7-recyclerview/dummy.txt
	touch $(builddir)/android-support-v7-recyclerview/dummy.txt
######## --------------------------


######## --------------------------
	mkdir -p $(builddir)/android-support-vector-drawable/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-vector-drawable/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-vector-drawable/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-vector-drawable/* $(builddir)/android-support-vector-drawable/; fi
	touch $(srcdir)/android-support-vector-drawable/dummy.txt
	touch $(builddir)/android-support-vector-drawable/dummy.txt
######## --------------------------

	if [ "$(builddir)" != "$(srcdir)" ]; then mkdir -p $(builddir)/android/src/com/zoffcc/applications/zanavi_msg/ ; fi
	echo "+++ move aidl files +++"
	echo "+++ move aidl files +++"
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -pf $(builddir)/android/aidl/com/zoffcc/applications/zanavi_msg/*.aidl $(builddir)/android/src/com/zoffcc/applications/zanavi_msg/ ; fi


apkg-all: all android/build.xml navit.xml

	echo "making::apkg-all"

	mkdir -p android/nativelibs/$(b_arch)

#	remove old libs
# ls -al android/libs/$(b_arch)/lib*.so
	rm -f android/nativelibs/$(b_arch)/libgraphics_android.so
	rm -f android/nativelibs/$(b_arch)/libgraphics_null.so
	rm -f android/nativelibs/$(b_arch)/libgui_internal.so
	rm -f android/nativelibs/$(b_arch)/libmap_binfile.so
	rm -f android/nativelibs/$(b_arch)/libmap_csv.so
	rm -f android/nativelibs/$(b_arch)/libmap_filter.so
	rm -f android/nativelibs/$(b_arch)/libmap_mg.so
	rm -f android/nativelibs/$(b_arch)/libmap_shapefile.so
	rm -f android/nativelibs/$(b_arch)/libmap_textfile.so
	rm -f android/nativelibs/$(b_arch)/libosd_core.so
	rm -f android/nativelibs/$(b_arch)/libspeech_android.so
	rm -f android/nativelibs/$(b_arch)/libvehicle_android.so
	rm -f android/nativelibs/$(b_arch)/libvehicle_demo.so
	rm -f android/nativelibs/$(b_arch)/libvehicle_file.so
	rm -f android/nativelibs/$(b_arch)/libvehicle_pipe.so
	rm -f android/nativelibs/$(b_arch)/libvehicle_serial.so
	rm -f android/nativelibs/$(b_arch)/libvehicle_socket.so

	mkdir -p android/res/raw
	for i in $(shell cd ../po && echo *.mo); do cp ../po/$$i android/res/raw/$$( echo $$i | tr "[A-Z]" "[a-z]") ; done
	cp navit_android_mdpi.xml android/res/raw/navitmdpi.xml
	cp navit_android_ldpi.xml android/res/raw/navitldpi.xml
	cp navit_android_hdpi.xml android/res/raw/navithdpi.xml

#	cleanup svn dirs, they should never be in build dir
	if [ "$(builddir)" != "$(srcdir)" ]; then find $(builddir) -type d -name '\.svn' -exec rm -Rf {} \; ; fi; echo "ignore the find errors!!"

	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then mkdir -p android/nativelibs/$(b_arch) ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then install -p $(NDK)/prebuilt/android-arm/gdbserver/gdbserver android/nativelibs/$(b_arch)/gdbserver ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then mkdir -p android/nativelibs/$(b_arch) ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then mkdir -p android/obj/local/$(b_arch) ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then cp android/nativelibs/$(b_arch)/*.so android/obj/local/$(b_arch)/ ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then echo "set solib-search-path $(builddir)/obj/local/$(b_arch)" > android/nativelibs/$(b_arch)/gdb.setup ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then echo "directory $(NDK)/platforms/android-""$(AND_API_LEVEL_C)""/arch-arm/usr/include $(NDK)/sources/cxx-stl/system" >> android/nativelibs/$(b_arch)/gdb.setup ; fi


apkg: apkg-all
	echo "making::apkg"
	touch $(srcdir)/android/dummy.txt
	touch $(builddir)/android/dummy.txt
	cd android && echo "debug"

apkg-release: apkg-all
	echo "making::apkg-release"
	touch $(srcdir)/android/dummy.txt
	touch $(builddir)/android/dummy.txt
	cd android && echo "release"
else
navit_SOURCES = start.c
navit_LDADD = lib@LIBNAVIT@.la

endif


if EVENT_GLIB
  lib@LIBNAVIT@_la_SOURCES += event_glib.c
endif

if !PLUGINS
  lib@LIBNAVIT@_la_SOURCES += builtin.c
  lib@LIBNAVIT@_la_LIBADD += $(MODULES)
endif

navit_LDFLAGS = -export-dynamic

navit_LDADD += @NAVIT_LIBS@ @WORDEXP_LIBS@ @ZLIB_LIBS@ @CRYPTO_LIBS@ @INTLLIBS@

if SUPPORT_WIN32
  navit_LDADD += resource.rsc
if SUPPORT_WIN32CE
  navit_LDADD += -lcommctrl -Lsupport/libc
else
  navit_LDADD += -lgdi32 -lcomctl32 -lwinmm
endif


resource.rsc: $(top_srcdir)/navit/gui/win32/resources/resource.rc
	$(WINDRES) -I $(top_srcdir)/navit/gui/win32/resources $(top_srcdir)/navit/gui/win32/resources/resource.rc resource.rsc
endif

.PHONY: version.h.tmp

version.h.tmp:
	echo "#include \"config.h\"" >version.h.tmp
if SOURCE_MODE_SVN
	echo "#define SVN_VERSION \"$$(LANG=C svnversion $(top_srcdir) 2>/dev/null)\"" >>version.h.tmp
else
	echo "#define SVN_VERSION \"@SOURCE_MODE@\"" >>version.h.tmp
endif
	echo "#define NAVIT_VARIANT \"@NAVIT_VARIANT@\"" >>version.h.tmp
	if ! diff version.h.tmp version.h >/dev/null 2>/dev/null; \
	then \
		mv version.h.tmp version.h; \
	fi

version.h: version.h.tmp

navit_config.h: ../config.h
	cp ../config.h navit_config.h.tmp
	if ! diff navit_config.h.tmp navit_config.h >/dev/null 2>/dev/null; \
	then \
		mv navit_config.h.tmp navit_config.h; \
	fi


distclean-local:
	rm -f version.h version.h.tmp navit_config.h navit_config.h.tmp builtin.c support-builtin.c navit.xml
	if [ "$(top_builddir)" != "$(top_srcdir)" ]; then rm -f navit.dtd; fi

builtin.c: $(top_builddir)/config.h
	ls $(top_builddir)/navit/*/*/*.la | sed -e "s/\.la/_init(void);/" -e "s/.*lib/extern void module_/" >builtin.c
	echo "extern void builtin_init(void);" >>builtin.c
	echo "void builtin_init(void) {" >>builtin.c
	ls $(top_builddir)/navit/*/*/*.la | sed -e "s/\.la/_init();/" -e "s/.*\\/lib/	module_/" >>builtin.c
	echo "}" >>builtin.c
support-builtin.c: $(top_builddir)/config.h
	ls $(top_builddir)/navit/support/*/*.la | sed -e "s/\.la/_init(void);/" -e "s/.*lib/extern void module_/" >support-builtin.c
	echo "extern void builtin_init(void);" >>support-builtin.c
	echo "void builtin_init(void) {" >>support-builtin.c
	ls $(top_builddir)/navit/support/*/*.la | sed -e "s/\.la/_init();/" -e "s/.*\\/lib/	module_/" >>support-builtin.c
	echo "}" >>support-builtin.c


